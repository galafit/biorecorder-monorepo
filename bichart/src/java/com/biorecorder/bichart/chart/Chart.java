package com.biorecorder.bichart.chart;

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
    private Insets fixedMargin;
    private Insets margin;
    private Insets spacing = new Insets(5);

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
    private int width;
    private int height;
    private boolean isValid = false;

    public Chart(ChartConfig config, Scale xScale) {
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

    private BRectangle graphArea() {
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
        int gap = Math.abs(config.getStackGap());
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
        if (axis.isSizeDependsOnMinMax()) {
            invalidate();
        }
    }

    private @Nullable
    StringSeries getXLabels(ChartData data) {
        if (!data.isNumberColumn(0)) {
            return new StringSeries() {
                @Override
                public int size() {
                    return data.rowCount();
                }

                @Override
                public String get(int index) {
                    return data.label(index, 0);
                }
            };
        }
        return null;
    }

    /*** ================================================
     * Base methods to interact
     * ==================================================
     */

    public List<Integer> getTraces(XAxisPosition xAxisPosition) {
       return  traceList.getTraces(xAxisPosition);
    }

    public XAxisPosition getTraceXAxisPosition(int traceNumber) {
        if(traceList.getTraceX(traceNumber) == xAxisList.get(0)) {
            return xIndexToPosition(0);
        } else {
            return xIndexToPosition(1);
        }
    }

    public int getTraceMarkSize(int traceNumber) {
        return traceList.getMarkSize(traceNumber);
    }

    public void setSpacing(Insets spacing) {
        if(spacing == null) {
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

    /**
     * set fixed margin
     * if margin == null delete fixed margin
     */
    public void setMargin(@Nullable Insets margin) {
        this.fixedMargin = margin;
        invalidate();
    }


    public BRectangle getBounds() {
        return new BRectangle(x, y, width, height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        legend = null;
        invalidate();
    }

    public void invalidate() {
        isValid = false;
        if(isLegendAttachedToStacks) {
            legend = null;
        }
    }

    public void revalidate(RenderContext renderContext) {
        margin = new Insets(0);
        if (width == 0 || height == 0) {
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
            top += title.getPrefferedSize(renderContext).height;;
        }

        if (isLegendEnabled) {
            if (legend == null) {
                legend = new Legend(config.getLegendConfig(), traceList, width1, isLegendAttachedToStacks);
            }
            if (!isLegendAttachedToStacks) {
                BDimension legendPrefSize = legend.getPrefferedSize(renderContext);
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

        if (fixedMargin != null) { // fixed margin
            margin = fixedMargin;
            BRectangle graphArea = graphArea();
            setXStartEnd(graphArea.x, graphArea.width);
            setYStartEnd(graphArea.y, graphArea.height);
            return;
        }
        margin = new Insets(top, right, bottom, left);
        BRectangle graphArea = graphArea();
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
        graphArea = graphArea();
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
        graphArea = graphArea();

        // adjust XAxis ranges
        setXStartEnd(graphArea.x, graphArea.width);
        isValid = true;
    }

    public void draw(BCanvas canvas) {
        if (!isValid) {
            revalidate(canvas.getRenderContext());
        }
        BRectangle graphArea = graphArea();
        if (width == 0 || height == 0) {
            return;
        }
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
                 xAxisList.get(xPositionToIndex(config.getDefaultXPosition())).drawGrid(canvas, stackArea);;
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
                yAxisList.get(yPositionToIndex(i, config.getDefaultYPosition())).drawGrid(canvas, graphArea);
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

    public void setConfig(ChartConfig config) {
        this.config = new ChartConfig(config);
        if (title != null) {
            title.setConfig(this.config.getTitleConfig());
        }
        for (int i = 0; i < xAxisList.size(); i++) {
            xAxisList.get(i).setConfig(this.config.getXAxisConfig());
        }
        for (int i = 0; i < yAxisList.size(); i++) {
            yAxisList.get(i).setConfig(this.config.getYAxisConfig());
        }
        tooltip.setConfig(this.config.getTooltipConfig());
        BColor[] colors = this.config.getTraceColors();
        for (int i = 0; i < traceList.size(); i++) {
            traceList.setColor(i, colors[i % colors.length]);
        }
        legend = null;
        invalidate();
    }

    public void setTitle(String title) {
        this.title = new Title(title, config.getTitleConfig());
        legend = null;
        invalidate();
    }

    public XAxisPosition getDefaultXAxisPosition() {
        return config.getDefaultXPosition();
    }

    public void setTraceData(int traceIndex, ChartData data) {
        traceList.setData(traceIndex, data);
    }

    /*public void setStackWeight(int stack, int weight) {
        checkStackNumber(stack);
        stackWeights.set(stack, weight);
        invalidate();
    }*/

    public void addStack() {
        addStack(config.getDefaultStackWeight());
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
     * @throws IllegalStateException if stack axis are used by some trace traces and
     *                                therefor can not be deleted
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
    public void addTrace(String name,ChartData data, TracePainter tracePainter, int stack) throws IllegalArgumentException{
        addTrace(name, data, tracePainter, stack, config.getDefaultXPosition(), config.getDefaultYPosition());
    }

    public void addTrace(String name, ChartData data, TracePainter tracePainter, XAxisPosition xPosition, YAxisPosition yPosition) {
        int stack = Math.max(0, yAxisList.size() / 2 - 1);
        addTrace(name, data, tracePainter, stack, xPosition, yPosition);
    }


    /**
     * Add trace to the stack with the given number
     * @param stack number of the stack to add trace
     * @throws IllegalArgumentException if stack number > total number of stacks in the chart
     */
    public void addTrace(String name, ChartData data, TracePainter tracePainter, int stack, XAxisPosition xPosition, YAxisPosition yPosition) throws IllegalArgumentException {
        if (yAxisList.size() == 0) {
            addStack(); // add stack if there is no stack
        }
        checkStackNumber(stack);
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
        invalidate();
    }


    public void setYPrefixAndSuffix(int stack, YAxisPosition yPosition, @Nullable String prefix, @Nullable String suffix) throws IllegalArgumentException {
        checkStackNumber(stack);
        yAxisList.get(yPositionToIndex(stack, yPosition)).setTickLabelPrefixAndSuffix(prefix, suffix);
        invalidate();
    }

    public void setXTitle(XAxisPosition xPosition, @Nullable String title) {
        xAxisList.get(xPositionToIndex(xPosition)).setTitle(title);
        invalidate();
    }

    public void setYTitle(int stack, YAxisPosition yPosition, @Nullable String title) throws IllegalArgumentException{
        checkStackNumber(stack);
        yAxisList.get(yPositionToIndex(stack, yPosition)).setTitle(title);
        invalidate();
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

    public void setXScale(XAxisPosition xPosition, Scale scale) {
        xAxisList.get(xPositionToIndex(xPosition)).setScale(scale);
    }

    public Scale getXScale(XAxisPosition xPosition) {
        return xAxisList.get(xPositionToIndex(xPosition)).getScale();
    }

    public Range getXRange(XAxisPosition xPosition) {
        AxisWrapper xAxis = xAxisList.get(xPositionToIndex(xPosition));
        return new Range(xAxis.getMin(), xAxis.getMax());
    }

    public void setYScale(int stack, YAxisPosition yPosition, Scale scale) throws IllegalArgumentException {
        checkStackNumber(stack);
        yAxisList.get(yPositionToIndex(stack, yPosition)).setScale(scale);
        invalidate();
    }

    public Range getAllTracesXMinMax() {
        return traceList.getAllTracesXMinMax();
    }


    /*** ================================================
     * Base methods to interact through GUI
     * ==================================================
     */


    boolean isXAxisUsed(XAxisPosition xPosition) {
        return traceList.isXAxisUsed(xAxisList.get(xPositionToIndex(xPosition)));
    }

    Range getXMinMax(XAxisPosition xPosition) {
        AxisWrapper axis = xAxisList.get(xPositionToIndex(xPosition));
        return new Range(axis.getMin(), axis.getMax());
    }


    int getStacksSumWeight() {
        int weightSum = 0;
        for (Integer weight : stackWeights) {
            weightSum += weight;
        }
        return weightSum;
    }

    int getStack(BPoint point) {
        if (new BRectangle(x, y, width, height).contain(point.getX(), point.getY())) {
            // find point stack
            int stackCount = yAxisList.size() / 2;
            for (int i = 0; i < stackCount; i++) {
                if(yAxisList.get(2 * i).contain(point)) {
                    return i;
                }
            }
        }
        return -1;
    }

    YAxisPosition getYAxisPosition(int stack, BPoint point) {
        if (new BRectangle(x, y, width, height).contain(point.getX(), point.getY())) {
            // find axis position
            AxisWrapper leftAxis = yAxisList.get(yPositionToIndex(stack, YAxisPosition.LEFT));
            AxisWrapper rightAxis = yAxisList.get(yPositionToIndex(stack, YAxisPosition.RIGHT));
            boolean isLeftAxisUsed = traceList.isYAxisUsed(leftAxis);
            boolean isRightAxisUsed = traceList.isYAxisUsed(rightAxis);

            if (!isRightAxisUsed && !isLeftAxisUsed) {
                return null;
            }
            if (!isLeftAxisUsed) {
                return YAxisPosition.RIGHT;
            }
            if (!isRightAxisUsed) {
                return YAxisPosition.LEFT;
            }
            // if both axis used
            if(point.getX() < width/2) {
                return YAxisPosition.LEFT;
            } else {
                return YAxisPosition.RIGHT;
            }
        }
        return null;
    }

    XAxisPosition getXAxisPosition(BPoint point) {
        if (new BRectangle(x, y, width, height).contain(point.getX(), point.getY())) {
            AxisWrapper bottomAxis = xAxisList.get(xPositionToIndex(XAxisPosition.BOTTOM));
            AxisWrapper topAxis = xAxisList.get(xPositionToIndex(XAxisPosition.TOP));
            int stack = getStack(point);
            AxisWrapper y1 = yAxisList.get(stack * 2);
            AxisWrapper y2 = yAxisList.get(stack * 2 + 1);
            boolean isBottomAxesUsed = traceList.isXAxisUsedByStack(bottomAxis, y1, y2);
            boolean isTopAxisUsed = traceList.isXAxisUsedByStack(topAxis, y1, y2);
            if (!isBottomAxesUsed && !isTopAxisUsed) {
                // do nothing
            } else if (!isTopAxisUsed) {
                return XAxisPosition.BOTTOM;
            } else if (!isBottomAxesUsed) {
                return XAxisPosition.TOP;
            } else { // both axis used
                int stackStart = (int)Math.min(y1.getStart(), y1.getEnd());
                if(point.getY() < stackStart + (int) y1.length()/2) {
                    return XAxisPosition.TOP;
                } else {
                    return XAxisPosition.BOTTOM;
                }
            }
        }
        return null;
    }

    boolean isTraceSelected() {
        return traceList.getSelection() >= 0;
    }

    XAxisPosition getSelectedTraceX() {
        return traceList.getTraceXPosition(traceList.getSelection());
    }

    YAxisPosition getSelectedTraceY() {
        return traceList.getTraceYPosition(traceList.getSelection());
    }

    int getSelectedTraceStack() {
        AxisWrapper yAxis = traceList.getTraceY(traceList.getSelection());
        for (int i = 0; i < yAxisList.size(); i++) {
            if(yAxisList.get(i) == yAxis) {
                return i/2;
            }
        }
       return -1;
    }

    boolean selectTrace(int x, int y) {
        if (legend != null) {
            return legend.selectTrace(x, y);
        }
        return false;
    }

    boolean hoverOff() {
        return tooltip.setHoverPoint(null);
    }

    boolean hoverOn(int x, int y) {
        if (!graphArea().contain(x, y)) {
            return hoverOff();
        }
        return tooltip.setHoverPoint(traceList.getNearest(x, y));
    }

    boolean translateY(int stack, YAxisPosition yPosition, int translation) {
        if(translation == 0) {
            return false;
        }
        AxisWrapper axis = yAxisList.get(yPositionToIndex(stack, yPosition));
        setAxisMinMax(axis, axis.translatedMinMax(translation), false);
        return true;
    }

    boolean translateX(XAxisPosition xPosition, int translation) {
        if(translation == 0) {
            return false;
        }
        AxisWrapper axis = xAxisList.get(xPositionToIndex(xPosition));
        setAxisMinMax(axis, axis.translatedMinMax(translation), false);
        return true;
    }

    boolean zoomY(int stack, YAxisPosition yPosition, double zoomFactor) {
        if(zoomFactor == 0 || zoomFactor == 1) {
            return false;
        }
        AxisWrapper axis = yAxisList.get(yPositionToIndex(stack, yPosition));
        setAxisMinMax(axis, axis.zoomedMinMax(zoomFactor), false);
        return true;
    }

    boolean zoomX(XAxisPosition xPosition, double zoomFactor) {
        if(zoomFactor == 0 || zoomFactor == 1) {
            return false;
        }
        AxisWrapper axis = xAxisList.get(xPositionToIndex(xPosition));
        setAxisMinMax(axis, axis.zoomedMinMax(zoomFactor), false);
        return true;
    }

}
