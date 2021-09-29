package com.biorecorder.bichart;

import com.biorecorder.bichart.configs.ChartConfig;
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
    private XAxisPosition defaultXPosition = XAxisPosition.BOTTOM;
    private YAxisPosition defaultYPosition = YAxisPosition.RIGHT;
    private int stackGap = 0; //px


    /*
     * 2 X-axis: 0(even) - BOTTOM and 1(odd) - TOP
     * 2 Y-axis for every section(stack): even - LEFT and odd - RIGHT;
     * All LEFT and RIGHT Y-axis are stacked.
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

    public Chart(ChartConfig config, Scale xScale) {
        this(config, xScale, XAxisPosition.BOTTOM, YAxisPosition.LEFT);
    }

    public Chart(ChartConfig config, Scale xScale, XAxisPosition defaultXPosition, YAxisPosition defaultYPosition) {
        this.defaultXPosition = defaultXPosition;
        this.defaultYPosition = defaultYPosition;
        this.config = new ChartConfig(config);
        AxisWrapper bottomAxis = new AxisWrapper(new Axis(xScale.copy(), config.getXAxisConfig(), XAxisPosition.BOTTOM));
        AxisWrapper topAxis = new AxisWrapper(new Axis(xScale.copy(), config.getXAxisConfig(), XAxisPosition.TOP));
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

    private void checkStackNumber(int stack) {
        int stackCount = yAxisList.size() / 2;
        if (stack >= stackCount) {
            String errMsg = "Stack = " + stack + " Number of stacks: " + stackCount;
            throw new IllegalArgumentException(errMsg);
        }
    }

    private int yPositionToIndex(int stack, YAxisPosition yPosition) {
        if (yPosition == YAxisPosition.LEFT) {
            return 2 * stack;
        } else {
            return 2 * stack + 1;
        }
    }

    private int xPositionToIndex(XAxisPosition xPosition) {
        if (xPosition == XAxisPosition.BOTTOM) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * 2 Y-axis for every section(stack): even - LEFT and odd - RIGHT;
     */
    private YAxisPosition yIndexToPosition(int yIndex) {
        if ((yIndex & 1) == 0) {
            return YAxisPosition.LEFT;
        }

        return YAxisPosition.RIGHT;
    }

    /**
     * X-axis: 0(even) - BOTTOM and 1(odd) - TOP
     */
    private XAxisPosition xIndexToPosition(int xIndex) {
        if ((xIndex & 1) == 0) {
            return XAxisPosition.BOTTOM;
        }
        return XAxisPosition.TOP;
    }

    private void setAxisMinMax(AxisWrapper axis, Range minMax, boolean isAutoscale) {
        if (minMax != null) {
            setAxisMinMax(axis, minMax.getMin(), minMax.getMax(), isAutoscale);
        }
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

    public XAxisPosition getOppositeXPosition(XAxisPosition xAxisPosition) {
        for (XAxisPosition position : XAxisPosition.values()) {
            if (position != xAxisPosition) {
                return position;
            }
        }
        return xAxisPosition;
    }

    public YAxisPosition getOppositeYPosition(YAxisPosition yAxisPosition) {
        for (YAxisPosition position : YAxisPosition.values()) {
            if (position != yAxisPosition) {
                return position;
            }
        }
        return yAxisPosition;
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

    public Insets getMargin(RenderContext renderContext) {
        if (!isValid) {
            revalidate(renderContext);
        }
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
            AxisWrapper topAxis = xAxisList.get(xPositionToIndex(XAxisPosition.TOP));
            AxisWrapper bottomAxis = xAxisList.get(xPositionToIndex(XAxisPosition.BOTTOM));
            if (traceList.isXAxisUsed(topAxis)) {
                top += topAxis.getWidth(renderContext);
            }
            if (traceList.isXAxisUsed(bottomAxis)) {
                bottom += bottomAxis.getWidth(renderContext);
            }

            margin = new Insets(top, right, bottom, left);
            graphArea = graphArea(margin);
            setYStartEnd(graphArea.y, graphArea.height);

            for (int i = 0; i < yAxisList.size(); i++) {
                AxisWrapper yAxis = yAxisList.get(i);
                if (traceList.isYAxisUsed(yAxis)) {
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
            AxisWrapper y2 = yAxisList.get(2 * stack + 1);
            BRectangle stackArea = new BRectangle(graphArea.x, (int) y1.getEnd(), graphArea.width, (int) y1.length());
            int bottomAxisIndex = xPositionToIndex(XAxisPosition.BOTTOM);
            int topAxisIndex = xPositionToIndex(XAxisPosition.TOP);
            AxisWrapper bottomAxis = xAxisList.get(bottomAxisIndex);
            AxisWrapper topAxis = xAxisList.get(topAxisIndex);
            boolean isBottomAxesUsed = traceList.isXAxisUsedByStack(bottomAxis, y1, y2);
            boolean isTopAxisUsed = traceList.isXAxisUsedByStack(topAxis, y1, y2);
            if (!isBottomAxesUsed && !isTopAxisUsed) {
                // do nothing
            } else if (!isTopAxisUsed) {
                bottomAxis.drawGrid(canvas, stackArea);
            } else if (!isBottomAxesUsed) {
                topAxis.drawGrid(canvas, stackArea);
            } else { // both axis used use primary axis
                xAxisList.get(xPositionToIndex(defaultXPosition)).drawGrid(canvas, stackArea);
                ;
            }
        }
        // draw Y axes grids
        for (int i = 0; i < stackCount; i++) {
            AxisWrapper leftAxis = yAxisList.get(yPositionToIndex(i, YAxisPosition.LEFT));
            AxisWrapper rightAxis = yAxisList.get(yPositionToIndex(i, YAxisPosition.RIGHT));
            boolean isLeftAxisUsed = traceList.isYAxisUsed(leftAxis);
            boolean isRightAxisUsed = traceList.isYAxisUsed(rightAxis);
            if (!isLeftAxisUsed && !isRightAxisUsed) {
                // do nothing
            } else if (!isLeftAxisUsed) {
                rightAxis.drawGrid(canvas, graphArea);
            } else if (!isRightAxisUsed) {
                leftAxis.drawGrid(canvas, graphArea);
            } else { // both axis is used we choose primary axis
                yAxisList.get(yPositionToIndex(i, defaultYPosition)).drawGrid(canvas, graphArea);
            }
        }
        // draw X axes
        for (AxisWrapper axis : xAxisList) {
            if (traceList.isXAxisUsed(axis)) {
                axis.drawAxis(canvas, graphArea);
            }
        }

        // draw Y axes
        for (AxisWrapper axis : yAxisList) {
            if (traceList.isYAxisUsed(axis)) {
                axis.drawAxis(canvas, graphArea);
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

    public int stackCount() {
        return yAxisList.size() / 2;
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
        AxisWrapper leftAxis = new AxisWrapper(new Axis(new LinearScale(), config.getYAxisConfig(), YAxisPosition.LEFT));
        AxisWrapper rightAxis = new AxisWrapper(new Axis(new LinearScale(), config.getYAxisConfig(), YAxisPosition.RIGHT));
        leftAxis.setStartEndOnTick(true);
        rightAxis.setStartEndOnTick(true);
        yAxisList.add(leftAxis);
        yAxisList.add(rightAxis);
        stackWeights.add(weight);
        invalidate();
    }

    /**
     * @param stack number of the stack to delete
     * @throws IllegalArgumentException if stack number > total number of stacks in the chart
     * @throws IllegalStateException    if stack axis are used by some trace traces and
     *                                  therefor can not be deleted
     */
    public void removeStack(int stack) throws IllegalArgumentException, IllegalStateException {
        checkStackNumber(stack);
        // check that no trace use that stack
        if (traceList.isYAxisUsed(yAxisList.get(stack * 2)) || traceList.isYAxisUsed(yAxisList.get(stack * 2 + 1))) {
            String errMsg = "Stack: " + stack + "can not be removed. It is used by trace";
            throw new IllegalStateException(errMsg);
        }

        stackWeights.remove(stack);
        yAxisList.remove(stack * 2 + 1);
        yAxisList.remove(stack * 2);
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
        XAxisPosition xPosition = defaultXPosition;
        YAxisPosition yPosition = defaultYPosition;
        if (isXOpposite) {
            xPosition = getOppositeXPosition(defaultXPosition);
        }
        if (isYOpposite) {
            yPosition = getOppositeYPosition(defaultYPosition);
        }

        int xIndex = xPositionToIndex(xPosition);
        int yIndex = yPositionToIndex(stack, yPosition);

        AxisWrapper xAxis = xAxisList.get(xIndex);
        AxisWrapper yAxis = yAxisList.get(yIndex);
        StringSeries dataXLabels = getXLabels(data);
        if (dataXLabels != null) {
            xAxis.setScale(new CategoryScale(dataXLabels));
        }

        BColor[] colors = config.getTraceColors();
        Trace trace = new Trace(name, data, tracePainter, xIndexToPosition(xIndex), yIndexToPosition(yIndex), xAxis, yAxis, colors[traceList.size() % colors.length]);
        traceList.add(trace);
    }

    public void setTraceName(int traceIndex, String name) {
        traceList.setName(traceIndex, name);
    }

    public void setTraceColor(int traceIndex, BColor color) {
        traceList.setColor(traceIndex, color);
    }

    public void removeTrace(int traceIndex) {
        traceList.remove(traceIndex);
    }

    public int traceCount() {
        return traceList.size();
    }

    public void setXPrefixAndSuffix(XAxisPosition xPosition, @Nullable String prefix, @Nullable String suffix) {
        xAxisList.get(xPositionToIndex(xPosition)).setTickLabelPrefixAndSuffix(prefix, suffix);
        if (!isMarginFixed) {
            invalidate();
        }
    }

    public void setYPrefixAndSuffix(int stack, YAxisPosition yPosition, @Nullable String prefix, @Nullable String suffix) throws IllegalArgumentException {
        checkStackNumber(stack);
        yAxisList.get(yPositionToIndex(stack, yPosition)).setTickLabelPrefixAndSuffix(prefix, suffix);
        if (!isMarginFixed) {
            invalidate();
        }
    }

    public void setXTitle(XAxisPosition xPosition, @Nullable String title) {
        xAxisList.get(xPositionToIndex(xPosition)).setTitle(title);
        if (!isMarginFixed) {
            invalidate();
        }
    }

    public void setYTitle(int stack, YAxisPosition yPosition, @Nullable String title) throws IllegalArgumentException {
        checkStackNumber(stack);
        yAxisList.get(yPositionToIndex(stack, yPosition)).setTitle(title);
        if (!isMarginFixed) {
            invalidate();
        }
    }

    public void setXMinMax(XAxisPosition xPosition, double min, double max) {
        setAxisMinMax(xAxisList.get(xPositionToIndex(xPosition)), min, max, false);
    }

    public void setYMinMax(int stack, YAxisPosition yPosition, double min, double max) throws IllegalArgumentException {
        checkStackNumber(stack);
        setAxisMinMax(yAxisList.get(yPositionToIndex(stack, yPosition)), min, max, false);
    }

    public void autoScaleX(XAxisPosition xPosition) {
        AxisWrapper xAxis = xAxisList.get(xPositionToIndex(xPosition));
        Range xMinMax = traceList.getTracesXMinMax(xAxis);
        setAxisMinMax(xAxis, xMinMax, true);
    }

    /**
     * Auto scale all x axes
     */
    public void autoScaleX() {
        Range xMinMax;
        for (AxisWrapper xAxis : xAxisList) {
            xMinMax = traceList.getTracesXMinMax(xAxis);
            setAxisMinMax(xAxis, xMinMax, true);
        }
    }

    public void autoScaleY(int stack, YAxisPosition yPosition) {
        AxisWrapper yAxis = yAxisList.get(yPositionToIndex(stack, yPosition));
        Range yMinMax = traceList.getTracesYMinMax(yAxis);
        setAxisMinMax(yAxis, yMinMax, true);
    }

    /**
     * Auto scale all y axes
     */
    public void autoScaleY() {
        Range yMinMax;
        for (AxisWrapper yAxis : yAxisList) {
            yMinMax = traceList.getTracesYMinMax(yAxis);
            setAxisMinMax(yAxis, yMinMax, true);
        }
    }

   /* public Range getXRange(XAxisPosition xPosition) {
        AxisWrapper xAxis = xAxisList.get(xPositionToIndex(xPosition));
        return new Range(xAxis.getMin(), xAxis.getMax());
    }*/

    Scale getXScale(XAxisPosition xPosition) {
        return xAxisList.get(xPositionToIndex(xPosition)).getScale();
    }

    boolean isValid() {
        return isValid;
    }

    /*** ================================================
     * Base methods for careful use mostly to interact through GUI
     * ==================================================
     */

    List<Integer> getTraces(XAxisPosition xAxisPosition) {
        return traceList.getTraces(xAxisPosition);
    }

    List<XAxisPosition> getXPositionsUsedByStack(int stack) {
        AxisWrapper y1 = yAxisList.get(2 * stack);
        AxisWrapper y2 = yAxisList.get(2 * stack + 1);
        List<XAxisPosition> xPositions = new ArrayList<>(1);
        for (int i = 0; i < xAxisList.size(); i++) {
            if (traceList.isXAxisUsedByStack(xAxisList.get(i), y1, y2)) {
                xPositions.add(xIndexToPosition(i));
            }
        }
        return xPositions;
    }

    List<YAxisPosition> getYPositionsUsedByStack(int stack) {
        int yIndex1 = 2 * stack;
        int yIndex2 = 2 * stack + 1;
        List<YAxisPosition> yPositions = new ArrayList<>(1);
        if (traceList.isYAxisUsed(yAxisList.get(yIndex1))) {
            yPositions.add(yIndexToPosition(yIndex1));
        }
        if (traceList.isYAxisUsed(yAxisList.get(yIndex2))) {
            yPositions.add(yIndexToPosition(yIndex2));
        }
        return yPositions;
    }


    XAxisPosition getTraceXPosition(int traceNumber) {
        return traceList.getTraceXPosition(traceNumber);
    }

    YAxisPosition getTraceYPosition(int traceNumber) {
        return traceList.getTraceYPosition(traceNumber);
    }

    int getTraceStack(int traceNumber) {
        AxisWrapper yAxis = traceList.getTraceY(traceNumber);
        for (int i = 0; i < yAxisList.size(); i++) {
            if (yAxisList.get(i) == yAxis) {
                return i / 2;
            }
        }
        return -1;
    }

    int getTraceMarkSize(int traceNumber) {
        return traceList.getMarkSize(traceNumber);
    }

    Range getXMinMax(XAxisPosition xPosition) {
        return xAxisList.get(xPositionToIndex(xPosition)).getMinMax();
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
        return tooltip.setHoverPoint(traceList.getTrace(traceNumber), pointIndex);
    }

    /**
     * @return @Null if there is no trace or point
     */
    public TracePoint getNearestPoint(int x, int y) {
        return traceList.getNearest(x, y);
    }

    public boolean translateY(int stack, YAxisPosition yPosition, int translation) {
        if (translation == 0) {
            return false;
        }
        AxisWrapper axis = yAxisList.get(yPositionToIndex(stack, yPosition));
        setAxisMinMax(axis, axis.translatedMinMax(translation), false);
        return true;
    }

    public boolean translateX(XAxisPosition xPosition, int translation) {
        if (translation == 0) {
            return false;
        }
        AxisWrapper axis = xAxisList.get(xPositionToIndex(xPosition));
        setAxisMinMax(axis, axis.translatedMinMax(translation), false);
        return true;
    }

    public boolean zoomY(int stack, YAxisPosition yPosition, double zoomFactor) {
        if (zoomFactor == 0 || zoomFactor == 1) {
            return false;
        }
        AxisWrapper axis = yAxisList.get(yPositionToIndex(stack, yPosition));
        setAxisMinMax(axis, axis.zoomedMinMax(zoomFactor), false);
        return true;
    }

    boolean zoomX(XAxisPosition xPosition, double zoomFactor) {
        if (zoomFactor == 0 || zoomFactor == 1) {
            return false;
        }
        AxisWrapper axis = xAxisList.get(xPositionToIndex(xPosition));
        setAxisMinMax(axis, axis.zoomedMinMax(zoomFactor), false);
        return true;
    }

}
