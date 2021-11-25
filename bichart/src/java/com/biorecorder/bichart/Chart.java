package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.*;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.CategoryScale;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.datalyb.series.StringSeries;
import com.sun.istack.internal.Nullable;

import java.util.*;
import java.util.List;


public class Chart {
    private ChartConfig config;
    private boolean isLegendEnabled = true;
    private boolean isLegendAttachedToStacks = true;
    private Insets margin;
    private boolean isMarginFixed = false;
    private Insets spacing = new Insets(5);
    private int defaultStackWeight = 2;
    private Orientation defaultXOrientation = Orientation.BOTTOM;
    private Orientation defaultYOrientation = Orientation.RIGHT;
    private int stackGap = 0; //px

    /*
     * 2 X-axis: 0(even) - BOTTOM and 1(odd) - TOP
     * 2 Y-axis for every section(stack): even - LEFT and odd - RIGHT;
     * All LEFT and RIGHT Y-axis are stacked.
     * Stacks are calculated from top to bottom
     * If there is no trace associated with some axis... this axis is invisible.
     **/
    private List<AxisWrapper> xAxisList = new ArrayList<>(2);
    private List<AxisWrapper> yAxisList = new ArrayList<>();

    private ArrayList<Integer> stackWeights = new ArrayList<Integer>();
    private TraceList traceList = new TraceList();
    private Legend legend;
    private Title title;
    private Tooltip tooltip;
    private int x;
    private int y;
    private int width = 100;
    private int height = 100;
    private boolean isValid = false;

    private Map<Integer, List<Integer>> xAxisNumberToTraceNumbers = new HashMap<>();
    private Map<Integer, List<Integer>> yAxisNumberToTraceNumbers = new HashMap<>();
    private Map<Integer, Set<Integer>> xAxisNumberToyAxisNumbers = new HashMap<>(2);
    private Map<Integer, Integer> traceNumberToXAxisNumber = new HashMap<>();
    private Map<Integer, Integer> traceNumberToYAxisNumber = new HashMap<>();



    public Chart(ChartConfig config, Scale xScale) {
        this.config = new ChartConfig(config);
        AxisWrapper bottomAxis = new AxisWrapper(new Axis(xScale.copy(), config.getXAxisConfig(), Orientation.BOTTOM));
        AxisWrapper topAxis = new AxisWrapper(new Axis(xScale.copy(), config.getXAxisConfig(), Orientation.TOP));
        xAxisList.add(bottomAxis);
        xAxisList.add(topAxis);
        addStack();
        tooltip = new Tooltip(config.getTooltipConfig());
        traceList.addChangeListener(new ChangeListener() {
            @Override
            public void onChange() {
                if (isLegendEnabled && !isLegendAttachedToStacks) {
                    invalidate();
                }
            }
        });
    }

    public void setDefaultXOrientation(Orientation defaultXOrientation) {
        this.defaultXOrientation = defaultXOrientation;
    }

    public void setDefaultYOrientation(Orientation defaultYOrientation) {
        this.defaultYOrientation = defaultYOrientation;
    }

    public boolean isXAxisUsedByStack(int xAxisNumber, int stack) {
        List<Integer> xTraces = xAxisNumberToTraceNumbers.get(xAxisNumber);
        if(xTraces != null) {
            for (Integer traceNumber : xTraces) {
                int traceYAxisNumber = traceNumberToYAxisNumber.get(traceNumber);
                if(traceYAxisNumber == stack * 2 || traceYAxisNumber == stack * 2 + 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public Range tracesXMinMax(int xAxisNumber) {
        List<Integer> xTraces = xAxisNumberToTraceNumbers.get(xAxisNumber);
        Range minMax = null;
        if(xTraces != null) {
            for (Integer traceNumber : xTraces) {
                minMax = Range.join(minMax, traceList.xMinMax(traceNumber));
            }
        }
        return minMax;
    }

    public Range tracesYMinMax(int yAxisNumber) {
        List<Integer> yTraces = yAxisNumberToTraceNumbers.get(yAxisNumber);
        Range minMax = null;
        if(yTraces != null) {
            for (Integer traceNumber : yTraces) {
                minMax = Range.join(minMax, traceList.yMinMax(traceNumber));
            }
        }
        return minMax;
    }


    public List<Integer> getTraces(int xAxisNumber) {
        List<Integer> traceNumbers = xAxisNumberToTraceNumbers.get(xAxisNumber);
        if(traceNumbers == null) {
            traceNumbers = new ArrayList<>(0);
            xAxisNumberToTraceNumbers.put(xAxisNumber, traceNumbers);
        }
        return traceNumbers;
    }

    private BRectangle graphArea(Insets margin) {
        int graphAreaWidth = width - margin.left() - margin.right();
        int graphAreaHeight = height - margin.top() - margin.bottom();
        if (graphAreaHeight < 0) {
            graphAreaHeight = 0;
        }
        if (graphAreaWidth < 0) {
            graphAreaWidth = 0;
        }
        return new BRectangle(x + margin.left(), y + margin.top(), graphAreaWidth, graphAreaHeight);
    }

    private void setXStartEnd(int areaX, int areaWidth) {
        for (AxisWrapper axis : xAxisList) {
            axis.setStartEnd(areaX, areaX + areaWidth);
        }
    }

    private void setYStartEnd(int areaY, int areaHeight) {
        int weightSum = getStacksSumWeight();
        int stackCount = yAxisList.size() / 2;
        int gap = Math.abs(stackGap);
        int height = areaHeight - (stackCount - 1) * gap;
        if (height <= 0) {
            height = areaHeight;
            gap = 0;
        }

        int end = areaY;
        for (int stack = 0; stack < stackCount; stack++) {
            int yAxisWeight = stackWeights.get(stack);
            int axisHeight = height * yAxisWeight / weightSum;
            int start = end + axisHeight;
           /* if(stack == stackCount - 1) {
                // for integer calculation sum yAxis intervalLength can be != areaHeight
                // so we fix that
                start = areaY + areaHeight;
            }*/
            AxisWrapper y1 = yAxisList.get(stack * 2);
            AxisWrapper y2 = yAxisList.get(stack * 2 + 1);
            y1.setStartEnd(start, end);
            y2.setStartEnd(start, end);
            end = start + gap;
        }
    }

    private boolean isXAxisUsed(int xAxisNumber) {
        return xAxisNumberToTraceNumbers.get(xAxisNumber) != null;
    }

    private boolean isYAxisUsed(int yAxisNumber) {
        return yAxisNumberToTraceNumbers.get(yAxisNumber) != null;
    }

    private void checkStackNumber(int stack) {
        int stackCount = yAxisList.size() / 2;
        if (stack >= stackCount) {
            String errMsg = "Stack = " + stack + " Number of stacks: " + stackCount;
            throw new IllegalArgumentException(errMsg);
        }
    }

    public static int yAxisOrientationToNumber(int stack, Orientation yPosition) {
        if (yPosition == Orientation.LEFT) {
            return 2 * stack;
        } else {
            return 2 * stack + 1;
        }
    }

    public static int xAxisOrientationToNumber(Orientation xPosition) {
        if (xPosition == Orientation.BOTTOM) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * 2 Y-axis for every section(stack): even - LEFT and odd - RIGHT;
     */
    public static Orientation yAxisNumberToOrientation(int yIndex) {
        if ((yIndex & 1) == 0) {
            return Orientation.LEFT;
        }

        return Orientation.RIGHT;
    }

    /**
     * X-axis: 0(even) - BOTTOM and 1(odd) - TOP
     */
    public static Orientation xAxisNumberToOrientation(int xIndex) {
        if ((xIndex & 1) == 0) {
            return Orientation.BOTTOM;
        }
        return Orientation.TOP;
    }

    private void setAxisMinMax(AxisWrapper axis, double min, double max, boolean isAutoscale) {
        axis.setMinMax(min, max, isAutoscale);
        if (!isMarginFixed && axis.isSizeDependsOnMinMax()) {
            invalidate();
        }
    }

    private @Nullable
    StringSeries getXLabels(ChartData data) {
        if (!data.isNumberColumn(0)) {
            return new StringSeries() {
                @Override
                public int size() {
                    return data.size();
                }

                @Override
                public String get(int index) {
                    return data.label(index, 0);
                }
            };
        }
        return null;
    }

    public Orientation getOppositeXPosition(Orientation Orientation) {
        for (Orientation position : Orientation.values()) {
            if (position != Orientation) {
                return position;
            }
        }
        return Orientation;
    }

    public Orientation getOppositeYPosition(Orientation Orientation) {
        for (Orientation position : Orientation.values()) {
            if (position != Orientation) {
                return position;
            }
        }
        return Orientation;
    }

    /*** ================================================
     * Base methods to interact
     * ==================================================
     */
    public void setSpacing(Insets spacing) {
        if (spacing == null) {
            this.spacing = new Insets(0);
        }
        this.spacing = spacing;
        invalidate();
    }

    Insets calculateMargin(RenderContext renderContext) {
        if (!isValid) {
            revalidate(renderContext);
        }
        return margin;
    }

    public Insets getMargin() {
        return margin;
    }

    public void setMargin(Insets margin) {
        isMarginFixed = true;
        this.margin = margin;
        invalidate();
    }

    public void setAutoMargin() {
        isMarginFixed = false;
        invalidate();
    }


    public BRectangle getBounds() {
        return new BRectangle(x, y, width, height);
    }

    public void setBounds(int x, int y, int width, int height) throws IllegalArgumentException {
        if (width == 0 || height == 0) {
            String errMsg = "Width and height must be > 0";
            throw new IllegalArgumentException(errMsg);
        }
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        legend = null;
        invalidate();
    }

    public void invalidate() {
        isValid = false;
        if (isLegendAttachedToStacks) {
            legend = null;
        }
    }

    public void revalidate(RenderContext renderContext) {
        if (isValid) {
            return;
        }
        int top = spacing.top();
        int bottom = spacing.bottom();
        int left = spacing.left();
        int right = spacing.right();
        int width1 = width - left - right;
        if (title != null) {
            title.setWidth(width1);
            title.moveTo(x + left, y + top);
            top += title.getPrefferedSize(renderContext).height;
            ;
        }

        if (isLegendEnabled) {
            if (legend == null) {
                legend = new Legend(config.getLegendConfig(), traceList, width1, isLegendAttachedToStacks);
            }
            if (!isLegendAttachedToStacks) {
                BDimension legendPrefSize = legend.getPreferredSize(renderContext);
                if (legend.isTop()) {
                    legend.moveTo(x + left, y + top);
                    top += legendPrefSize.height;
                } else if (legend.isBottom()) {
                    legend.moveTo(x + left, y + height - legendPrefSize.height - bottom);
                    bottom += legendPrefSize.height;
                } else {
                    legend.moveTo(x + left, y + top + (height - top - bottom - legendPrefSize.height) / 2);
                }
            }
        }

        if (isMarginFixed) {
            BRectangle graphArea = graphArea(margin);
            setXStartEnd(graphArea.x, graphArea.width);
            setYStartEnd(graphArea.y, graphArea.height);
        } else {
            margin = new Insets(top, right, bottom, left);
            BRectangle graphArea = graphArea(margin);
            setXStartEnd(graphArea.x, graphArea.width);
            int topAxisNumber = xAxisOrientationToNumber(Orientation.TOP);
            int bottomAxisNumber = xAxisOrientationToNumber(Orientation.BOTTOM);
            AxisWrapper topAxis = xAxisList.get(topAxisNumber);
            AxisWrapper bottomAxis = xAxisList.get(bottomAxisNumber);
            if (isXAxisUsed(topAxisNumber)) {
                top += topAxis.getWidth(renderContext);
            }
            if (isXAxisUsed(bottomAxisNumber)) {
                bottom += bottomAxis.getWidth(renderContext);
            }

            margin = new Insets(top, right, bottom, left);
            graphArea = graphArea(margin);
            setYStartEnd(graphArea.y, graphArea.height);

            for (int i = 0; i < yAxisList.size(); i++) {
                AxisWrapper yAxis = yAxisList.get(i);
                if (isYAxisUsed(i)) {
                    if (i % 2 == 0) {
                        left = Math.max(left, yAxis.getWidth(renderContext) + spacing.left());
                    } else {
                        right = Math.max(right, yAxis.getWidth(renderContext) + spacing.right());
                    }
                }
            }
            margin = new Insets(top, right, bottom, left);
            graphArea = graphArea(margin);

            // adjust XAxis ranges
            setXStartEnd(graphArea.x, graphArea.width);
        }

        isValid = true;
    }

    public void draw(BCanvas canvas) {
        revalidate(canvas.getRenderContext());
        BRectangle graphArea = graphArea(margin);
        canvas.enableAntiAliasAndHinting();
        canvas.setColor(config.getMarginColor());
        canvas.fillRect(x, y, width, height);
        //draw title
        if (title != null) {
            title.draw(canvas);
        }

        // fill stacks
        int stackCount = yAxisList.size() / 2;
        canvas.setColor(config.getBackgroundColor());
        for (int i = 0; i < stackCount; i++) {
            AxisWrapper yAxis = yAxisList.get(i * 2);
            BRectangle stackArea = new BRectangle(graphArea.x, (int) yAxis.getEnd(), graphArea.width, (int) yAxis.length());
            canvas.fillRect(stackArea.x, stackArea.y, stackArea.width, stackArea.height);
        }
        // draw X axes grids separately for every stack
        for (int stack = 0; stack < stackCount; stack++) {
            AxisWrapper y1 = yAxisList.get(2 * stack);
            BRectangle stackArea = new BRectangle(graphArea.x, (int) y1.getEnd(), graphArea.width, (int) y1.length());
            int bottomAxisIndex = xAxisOrientationToNumber(Orientation.BOTTOM);
            int topAxisIndex = xAxisOrientationToNumber(Orientation.TOP);
            AxisWrapper bottomAxis = xAxisList.get(bottomAxisIndex);
            AxisWrapper topAxis = xAxisList.get(topAxisIndex);
            boolean isBottomAxesUsed = isXAxisUsedByStack(bottomAxisIndex, stack);
            boolean isTopAxisUsed = isXAxisUsedByStack(topAxisIndex, stack);
            if (!isBottomAxesUsed && !isTopAxisUsed) {
                // do nothing
            } else if (!isTopAxisUsed) {
                bottomAxis.drawGrid(canvas, stackArea);
            } else if (!isBottomAxesUsed) {
                topAxis.drawGrid(canvas, stackArea);
            } else { // both axis used use primary axis
                xAxisList.get(xAxisOrientationToNumber(defaultXOrientation)).drawGrid(canvas, stackArea);
            }
        }
        // draw Y axes grids
        for (int i = 0; i < stackCount; i++) {
            int leftAxisIndex = yAxisOrientationToNumber(i, Orientation.LEFT);
            int rightAxisIndex = yAxisOrientationToNumber(i, Orientation.RIGHT);
            AxisWrapper leftAxis = yAxisList.get(leftAxisIndex);
            AxisWrapper rightAxis = yAxisList.get(rightAxisIndex);
            boolean isLeftAxisUsed = isYAxisUsed(leftAxisIndex);
            boolean isRightAxisUsed = isYAxisUsed(rightAxisIndex);
            if (!isLeftAxisUsed && !isRightAxisUsed) {
                // do nothing
            } else if (!isLeftAxisUsed) {
                rightAxis.drawGrid(canvas, graphArea);
            } else if (!isRightAxisUsed) {
                leftAxis.drawGrid(canvas, graphArea);
            } else { // both axis is used we choose primary axis
                yAxisList.get(yAxisOrientationToNumber(i, defaultYOrientation)).drawGrid(canvas, graphArea);
            }
        }
        // draw X axes
        for (int i = 0; i < xAxisList.size(); i++) {
            if (isXAxisUsed(i)) {
                xAxisList.get(i).drawAxis(canvas, graphArea);
            }
        }

        // draw Y axes
        for (int i = 0; i < yAxisList.size(); i++) {
            if (isYAxisUsed(i)) {
                yAxisList.get(i).drawAxis(canvas, graphArea);
            }
        }
        canvas.save();
        canvas.setClip(graphArea.x, graphArea.y, graphArea.width, graphArea.height);
        traceList.draw(canvas);
        canvas.restore();
        if (legend != null) {
            legend.draw(canvas);
        }
        tooltip.draw(canvas, new BRectangle(x, y, width, height));
    }

    void drawRect(BCanvas canvas, int xAxisNumber, double startValue, double endValue, BColor color, int borderWidth) {
        BRectangle graphArea = graphArea(margin);
        AxisWrapper axis = xAxisList.get(xAxisNumber);
        canvas.setColor(color);
        canvas.setStroke(borderWidth, DashStyle.SOLID);
        int start = (int) axis.scale(startValue) - borderWidth;
        int end = (int) axis.scale(endValue) + borderWidth;
        int width = end - start;
        int height = graphArea.height - 2 * borderWidth;
        canvas.drawRect(start, graphArea.y + borderWidth, width, height);
    }

    public int stackCount() {
        return yAxisList.size() / 2;
    }

    public int xAxesCount() {
        return xAxisList.size();
    }

    public void setTitle(String title) {
        this.title = new Title(title, config.getTitleConfig());
        legend = null;
        invalidate();
    }

    public void setTraceData(int traceIndex, ChartData data) {
        traceList.setData(traceIndex, data);
    }

    public void addStack() {
        addStack(defaultStackWeight);
    }

    public void addStack(int weight) {
        AxisWrapper leftAxis = new AxisWrapper(new Axis(new LinearScale(), config.getYAxisConfig(), Orientation.LEFT));
        AxisWrapper rightAxis = new AxisWrapper(new Axis(new LinearScale(), config.getYAxisConfig(), Orientation.RIGHT));
        leftAxis.setStartEndOnTick(true);
        rightAxis.setStartEndOnTick(true);
        yAxisList.add(leftAxis);
        yAxisList.add(rightAxis);
        stackWeights.add(weight);
        invalidate();
    }


    /**
     * add trace to the last stack
     */
    public void addTrace(String name, ChartData data, TracePainter tracePainter) {
        int stack = Math.max(0, yAxisList.size() / 2 - 1);
        addTrace(name, data, tracePainter, stack);
    }


    /**
     * Add trace to the stack with the given number
     * @param stack number of the stack to add trace
     * @throws IllegalArgumentException if stack number > total number of stacks in the chart
     */
    public void addTrace(String name, ChartData data, TracePainter tracePainter, int stack) throws IllegalArgumentException {
        addTrace(name, data, tracePainter, stack, false, false);
    }

    public void addTrace(String name, ChartData data, TracePainter tracePainter, boolean isXOpposite, boolean isYOpposite) {
        int stack = Math.max(0, yAxisList.size() / 2 - 1);
        addTrace(name, data, tracePainter, stack, isXOpposite, isYOpposite);
    }

    /**
     * Add trace to the stack with the given number
     *
     * @param stack number of the stack to add trace
     * @throws IllegalArgumentException if stack number > total number of stacks in the chart
     */
    public void addTrace(String name, ChartData data, TracePainter tracePainter, int stack, boolean isXOpposite, boolean isYOpposite) throws IllegalArgumentException {
        if (yAxisList.size() == 0) {
            addStack(); // add stack if there is no stack
        }
        checkStackNumber(stack);
        Orientation xPosition = defaultXOrientation;
        Orientation yPosition = defaultYOrientation;
        if (isXOpposite) {
            xPosition = getOppositeXPosition(defaultXOrientation);
        }
        if (isYOpposite) {
            yPosition = getOppositeYPosition(defaultYOrientation);
        }

        int xIndex = xAxisOrientationToNumber(xPosition);
        int yIndex = yAxisOrientationToNumber(stack, yPosition);
        AxisWrapper xAxis = xAxisList.get(xIndex);
        AxisWrapper yAxis = yAxisList.get(yIndex);
        StringSeries dataXLabels = getXLabels(data);
        if (dataXLabels != null) {
            xAxis.setScale(new CategoryScale(dataXLabels));
        }

        BColor[] colors = config.getTraceColors();
        Trace trace = new Trace(name, data, tracePainter, xAxis, yAxis, colors[traceList.traceCount() % colors.length]);
        traceList.add(trace);
        int traceNumber = traceList.traceCount() - 1;
        traceNumberToXAxisNumber.put(traceNumber, xIndex);
        traceNumberToYAxisNumber.put(traceNumber, yIndex);
        List<Integer> traces = xAxisNumberToTraceNumbers.get(xIndex);
        if(traces == null) {
            traces = new ArrayList<>();
            xAxisNumberToTraceNumbers.put(xIndex, traces);
        }
        traces.add(traceNumber);

        traces = yAxisNumberToTraceNumbers.get(yIndex);
        if(traces == null) {
            traces = new ArrayList<>();
            yAxisNumberToTraceNumbers.put(yIndex, traces);
        }
        traces.add(traceNumber);

        Set<Integer> yAxes = xAxisNumberToyAxisNumbers.get(xIndex);
        if(yAxes == null) {
            yAxes = new HashSet<>();
            xAxisNumberToyAxisNumbers.put(xIndex, yAxes);
        }
        yAxes.add(yIndex);
    }

    public void setTraceName(int traceIndex, String name) {
        traceList.setName(traceIndex, name);
    }

    public void setTraceColor(int traceIndex, BColor color) {
        traceList.setColor(traceIndex, color);
    }

    public int traceCount() {
        return traceList.traceCount();
    }

    public void setXPrefixAndSuffix(int xAxisNumber, @Nullable String prefix, @Nullable String suffix) {
        xAxisList.get(xAxisNumber).setTickLabelPrefixAndSuffix(prefix, suffix);
        if (!isMarginFixed) {
            invalidate();
        }
    }

    public void setYPrefixAndSuffix(int yAxisNumber, @Nullable String prefix, @Nullable String suffix) throws IllegalArgumentException {
        yAxisList.get(yAxisNumber).setTickLabelPrefixAndSuffix(prefix, suffix);
        if (!isMarginFixed) {
            invalidate();
        }
    }

    public void setXTitle(int xAxisNumber, @Nullable String title) {
        xAxisList.get(xAxisNumber).setTitle(title);
        if (!isMarginFixed) {
            invalidate();
        }
    }

    public void setYTitle(int yAxisNumber, @Nullable String title) throws IllegalArgumentException {
        yAxisList.get(yAxisNumber).setTitle(title);
        if (!isMarginFixed) {
            invalidate();
        }
    }

    public void setXMinMax(int xAxisNumber, double min, double max) {
        setAxisMinMax(xAxisList.get(xAxisNumber), min, max, false);
    }

    public void autoScaleX(int xAxisNumber) {
        Range xMinMax = tracesXMinMax(xAxisNumber);
        if(xMinMax != null) {
            setAxisMinMax(xAxisList.get(xAxisNumber), xMinMax.getMin(), xMinMax.getMax(), true);
        }
    }

    /**
     * Auto scale all x axes
     */
    public void autoScaleX() {
        for (int i = 0; i < xAxisList.size(); i++) {
            autoScaleX(i);
        }
    }

    public void autoScaleY(int yAxisNumber) {
        Range yMinMax = tracesYMinMax(yAxisNumber);
        if(yMinMax != null) {
            setAxisMinMax(yAxisList.get(yAxisNumber), yMinMax.getMin(), yMinMax.getMax(), true);
        }
    }

    /**
     * Auto scale all y axes
     */
    public void autoScaleY() {
        for (int i = 0; i < yAxisList.size(); i++) {
            autoScaleY(i);
        }
    }

    double valueToPosition(int xAxisNumber, double value) {
        return xAxisList.get(xAxisNumber).scale(value);
    }

    double positionToValue(int xAxisNumber, double position) {
        return xAxisList.get(xAxisNumber).invert(position);
    }

    boolean isValid() {
        return isValid;
    }

    /*** ================================================
     * Base methods for careful use mostly to interact through GUI
     * ==================================================
     */

    List<Integer> getXAxesNumbersUsedByStack(int stack) {
        List<Integer> xAxisNumbers = new ArrayList<>(1);
        for (int i = 0; i < xAxisList.size(); i++) {
            if (isXAxisUsedByStack(i, stack)) {
                xAxisNumbers.add(i);
            }
        }
        return xAxisNumbers;
    }

    List<Integer> getYAxesNumbersUsedByStack(int stack) {
        int yIndex1 = 2 * stack;
        int yIndex2 = 2 * stack + 1;
        List<Integer> yAxisNumbers = new ArrayList<>(1);
        if (isYAxisUsed(yIndex1)) {
            yAxisNumbers.add(yIndex1);
        }
        if (isYAxisUsed(yIndex2)) {
            yAxisNumbers.add(yIndex2);
        }
        return yAxisNumbers;
    }


    int getTraceXAxisNumber(int traceNumber) {
        return traceNumberToXAxisNumber.get(traceNumber);
    }

    int getTraceYAxisNumber(int traceNumber) {
        return traceNumberToYAxisNumber.get(traceNumber);
    }

    int getTraceMarkSize(int traceNumber) {
        return traceList.getMarkSize(traceNumber);
    }

    double getXMin(int xAxisNumber) {
        return xAxisList.get(xAxisNumber).getMin();
    }

    double getXMax(int xAxisNumber) {
        return xAxisList.get(xAxisNumber).getMax();
    }

    int getStacksSumWeight() {
        int weightSum = 0;
        for (Integer weight : stackWeights) {
            weightSum += weight;
        }
        return weightSum;
    }

    /**
     * @return -1 if no stack contains point x, y
     */
    public int getStackContaining(int x, int y) {
        // find point stack
        int stackCount = yAxisList.size() / 2;
        for (int i = 0; i < stackCount; i++) {
            if (yAxisList.get(2 * i).contain(x, y)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return traceNumber of the corresponding button containing the point x, y
     * or -1 if no legend button contains the point
     */
    public int getLegendButtonContaining(int x, int y) {
        return legend.getTraceLegendButtonContaining(x, y);
    }

    public void selectTrace(int traceNumber) {
        traceList.setSelection(traceNumber);
    }

    void removeTraceSelection() {
        traceList.setSelection(-1);
    }

    /**
     * @return -1 if there is no selection
     */
    public int getSelectedTrace() {
        return traceList.getSelection();
    }

    public boolean hoverOff() {
        return tooltip.removeHoverPoint();
    }

    public boolean hoverOn(int traceNumber, int pointIndex) {
        return tooltip.setHoverPoint(traceNumber, pointIndex, traceList.getTooltipData(traceNumber, pointIndex));
    }

    /**
     * @return @Null if there is no trace or point
     */
    public TracePoint getNearestPoint(int x, int y) {
        return traceList.getNearest(x, y);
    }

    public boolean translateY(int yAisNumber, int translation) {
        if (translation == 0) {
            return false;
        }
        AxisWrapper axis = yAxisList.get(yAisNumber);
        Range minMax = axis.translatedMinMax(translation);
        setAxisMinMax(axis,minMax.getMin(), minMax.getMax() , false);
        return true;
    }

    public boolean translateX(int xAisNumber, int translation) {
        if (translation == 0) {
            return false;
        }
        AxisWrapper axis = xAxisList.get(xAisNumber);
        Range minMax = axis.translatedMinMax(translation);
        setAxisMinMax(axis,minMax.getMin(), minMax.getMax() , false);
        return true;
    }

    public boolean zoomY(int yAxisNumber, double zoomFactor, int anchorPoint) {
        if (zoomFactor == 0 || zoomFactor == 1) {
            return false;
        }
        AxisWrapper axis = yAxisList.get(yAxisNumber);
        Range minMax = axis.zoomedMinMax(zoomFactor, anchorPoint);
        setAxisMinMax(axis,minMax.getMin(), minMax.getMax() , false);
        return true;
    }

    boolean zoomX(int xAxisNumber, double zoomFactor, int anchorPoint) {
        if (zoomFactor == 0 || zoomFactor == 1) {
            return false;
        }
        AxisWrapper axis = xAxisList.get(xAxisNumber);
        Range minMax = axis.zoomedMinMax(zoomFactor, anchorPoint);
        setAxisMinMax(axis,minMax.getMin(), minMax.getMax() , false);
        return true;
    }

}
