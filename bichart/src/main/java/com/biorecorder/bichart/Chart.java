package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.*;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.CategoryScale;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.data.sequence.StringSequence;
import com.sun.istack.internal.Nullable;

import java.util.*;
import java.util.List;

/**
 * Created by hdablin on 24.03.17.
 */
public class Chart {
    private ChartConfig config;
    private boolean isLegendEnabled = true;
    private boolean isLegendAttachedToStacks = false;
    private Insets fixedMargin;
    private Insets margin;
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
    private int width;
    private int height;
    private boolean isValid = false;

    public Chart() {
        this(DarkTheme.getChartConfig());
    }

    public Chart(ChartConfig config) {
        this.config = new ChartConfig(config);
        AxisWrapper bottomAxis = new AxisWrapper(new Axis(new LinearScale(), config.getXAxisConfig(), XAxisPosition.BOTTOM));
        AxisWrapper topAxis = new AxisWrapper(new Axis(new LinearScale(), config.getXAxisConfig(), XAxisPosition.TOP));
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

    private void invalidate() {
        isValid = false;
        legend = null;
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
        return new BRectangle(margin.left(), margin.top(), graphAreaWidth, graphAreaHeight);
    }

    public void revalidate(RenderContext renderContext) {
        if (isValid) {
            return;
        }
        margin = new Insets(0);
        if (width == 0 || height == 0) {
            return;
        }
        if (fixedMargin != null) { // fixed margin
            margin = fixedMargin;
            BRectangle graphArea = graphArea();
            setXStartEnd(graphArea.x, graphArea.width);
            setYStartEnd(graphArea.y, graphArea.height);
            if (isLegendEnabled) {
                legend = new Legend(config.getLegendConfig(), traceList, width, isLegendAttachedToStacks);
            }
            return;
        }

        Insets spacing = config.getSpacing();
        if (spacing == null) {
            spacing = new Insets(0);
        }

        int top = spacing.top();
        int bottom = spacing.bottom();
        int left = spacing.left();
        int right = spacing.right();
        int width1 = width - left - right;

        if (title != null) {
            title.setWidth(width1);
            int titleHeight = title.getPrefferedSize(renderContext).height;
            title.moveTo(left, top);
            top += titleHeight;
        }

        if (isLegendEnabled && !isLegendAttachedToStacks) {
            legend = new Legend(config.getLegendConfig(), traceList, width1, isLegendAttachedToStacks);
            BDimension legendPrefSize = legend.getPrefferedSize(renderContext);
            if (legend.isTop()) {
                legend.moveTo(left, top);
                top += legendPrefSize.height;
            } else if (legend.isBottom()) {
                legend.moveTo(left, height - legendPrefSize.height - bottom);
                bottom += legendPrefSize.height;
            } else {
                legend.moveTo(left, top + (height - top - bottom - legendPrefSize.height) / 2);
            }
        }
        setXStartEnd(left, width1);
        AxisWrapper topAxis = xAxisList.get(xPositionToIndex(XAxisPosition.TOP));
        AxisWrapper bottomAxis = xAxisList.get(xPositionToIndex(XAxisPosition.BOTTOM));
        if (traceList.isXAxisUsed(topAxis)) {
            top += topAxis.getWidth(renderContext);
        }
        if (traceList.isXAxisUsed(bottomAxis)) {
            bottom += bottomAxis.getWidth(renderContext);
        }

        margin = new Insets(top, right, bottom, left);
        BRectangle graphArea = graphArea();
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
        if (isLegendEnabled && isLegendAttachedToStacks) {
            legend = new Legend(config.getLegendConfig(), traceList, graphArea.width, isLegendAttachedToStacks);
        }
        isValid = true;
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
            AxisWrapper leftAxis = yAxisList.get(stack * 2);
            AxisWrapper rightAxis = yAxisList.get(stack * 2 + 1);
            leftAxis.setStartEnd(start, end);
            rightAxis.setStartEnd(start, end);
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

    private int getYStack(int yIndex) {
        return yIndex / 2;
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

    private int xAxisToIndex(AxisWrapper x) {
        for (int i = 0; i < xAxisList.size(); i++) {
            if (xAxisList.get(i) == x) {
                return i;
            }
        }
        return -1;
    }

    private int yAxisToIndex(AxisWrapper y) {
        for (int i = 0; i < yAxisList.size(); i++) {
            if (yAxisList.get(i) == y) {
                return i;
            }
        }
        return -1;
    }

    /**
     * =============================================================*
     * Protected method for careful use                            *
     * ==============================================================
     */

    Insets getMargin(RenderContext renderContext) {
        if (!isValid) {
            revalidate(renderContext);
        }
        return margin;
    }

    double getBestExtent(XAxisPosition xAxisPosition, RenderContext renderContext) {
        AxisWrapper xAxis = xAxisList.get(xPositionToIndex(xAxisPosition));
        double extent = xAxis.getBestExtent(renderContext, width);
        double tracesExtent = traceList.getTracesBestExtent(xAxis, width);
        if (extent < 0) {
            extent = tracesExtent;
        } else if (tracesExtent > 0) {
            extent = Math.min(extent, tracesExtent);
        }
        return extent;
    }


    int getStacksSumWeight() {
        int weightSum = 0;
        for (Integer weight : stackWeights) {
            weightSum += weight;
        }
        return weightSum;
    }

    double scale(XAxisPosition xAxisPosition, double value) {
        return xAxisList.get(xPositionToIndex(xAxisPosition)).getScale().scale(value);
    }

    double invert(XAxisPosition xAxisPosition, double value) {
        return xAxisList.get(xPositionToIndex(xAxisPosition)).getScale().invert(value);
    }

    Range getYMinMax(int stack, YAxisPosition yAxisPosition, BCanvas canvas) {
        if (!isValid) {
            revalidate(canvas.getRenderContext());
        }
        AxisWrapper yAxis = yAxisList.get(yPositionToIndex(stack, yAxisPosition));
        return new Range(yAxis.getMin(), yAxis.getMax());
    }

    int getStack(BPoint point) {
        if (new BRectangle(0, 0, width, height).contains(point.getX(), point.getY())) {
            // find point stack
            int stackCount = yAxisList.size() / 2;
            for (int i = 0; i < stackCount; i++) {
                int leftYIndex = 2 * i;
                AxisWrapper axisLeft = yAxisList.get(leftYIndex);
                if (axisLeft.getEnd() <= point.getY() && axisLeft.getStart() >= point.getY()) {
                    return i;
                }
            }
        }
        return -1;
    }

    XAxisPosition[] getXAxes() {
        List<XAxisPosition> positions = new ArrayList<>();
        for (XAxisPosition position : XAxisPosition.values()) {
            if (traceList.isXAxisUsed(xAxisList.get(xPositionToIndex(position)))) {
                positions.add(position);
            }

        }
        return positions.toArray(new XAxisPosition[positions.size()]);
    }

    YAxisPosition[] getYAxes(int stack) {
        List<YAxisPosition> positions = new ArrayList<>();
        for (YAxisPosition position : YAxisPosition.values()) {
            if (traceList.isYAxisUsed(yAxisList.get(yPositionToIndex(stack, position)))) {
                positions.add(position);
            }

        }
        return positions.toArray(new YAxisPosition[positions.size()]);
    }


    YAxisPosition getYAxis(int stack, BPoint point) {
        if (new BRectangle(0, 0, width, height).contains(point.getX(), point.getY())) {
            // find axis position
            AxisWrapper axisLeft = yAxisList.get(2 * stack);
            AxisWrapper axisRight = yAxisList.get(2 * stack + 1);
            if (axisLeft.getEnd() <= point.getY() && axisLeft.getStart() >= point.getY()) {
                if (axisLeft.getEnd() <= point.getY() && axisLeft.getStart() >= point.getY()) {
                    if (!traceList.isYAxisUsed(axisLeft) && !traceList.isYAxisUsed(axisRight)) {
                        return null;
                    }
                    if (!traceList.isYAxisUsed(axisLeft)) {
                        return YAxisPosition.RIGHT;
                    }
                    if (!traceList.isYAxisUsed(axisRight)) {
                        return YAxisPosition.LEFT;
                    }
                    if (0 <= point.getX() && point.getX() <= width / 2 && traceList.isYAxisUsed(axisLeft)) { // left half
                        return YAxisPosition.LEFT;
                    } else {
                        return YAxisPosition.RIGHT;
                    }
                }
            }
        }
        return null;
    }

    XAxisPosition getXAxisPosition(BPoint point) {
        return xIndexToPosition(xAxisToIndex(getXAxis(point)));
    }

    AxisWrapper getXAxis(BPoint point) {
        if (new BRectangle(0, 0, width, height).contains(point.getX(), point.getY())) {
            int bottomAxisIndex = xPositionToIndex(XAxisPosition.BOTTOM);
            int topAxisIndex = xPositionToIndex(XAxisPosition.TOP);
            AxisWrapper bottomAxis = xAxisList.get(bottomAxisIndex);
            AxisWrapper topAxis = xAxisList.get(topAxisIndex);
            if (!traceList.isXAxisUsed(bottomAxis) && !traceList.isXAxisUsed(topAxis)) {
                return null;
            } else if (!traceList.isXAxisUsed(topAxis)) {
                return bottomAxis;
            } else if (!traceList.isXAxisUsed(bottomAxis)) {
                return topAxis;
            } else {
                // return primary x Axis;
                return xAxisList.get(xPositionToIndex(config.getPrimaryXPosition()));
            }
        }
        return null;
    }


    /**
     * =================================================*
     * Base methods to interact               *
     * ==================================================
     */
    public boolean hoverOff() {
        return tooltip.setHoverPoint(null);
    }

    public boolean hoverOn(int x, int y) {
        if (!graphArea().contains(x, y)) {
            return hoverOff();
        }
        return tooltip.setHoverPoint(traceList.getNearest(x, y));
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
        canvas.fillRect(0, 0, width, height);
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
            if (!traceList.isXAxisUsedByStack(bottomAxis, y1, y2) && !traceList.isXAxisUsedByStack(topAxis, y1, y2)) {
                // do nothing
            } else if (traceList.isXAxisUsedByStack(bottomAxis, y1, y2)) {
                bottomAxis.drawGrid(canvas, stackArea);
            } else if (traceList.isXAxisUsedByStack(topAxis, y1, y2)) {
                topAxis.drawGrid(canvas, stackArea);
            } else { // both axis used
                AxisWrapper primaryXAxis = xAxisList.get(xPositionToIndex(config.getPrimaryXPosition()));
                primaryXAxis.drawGrid(canvas, stackArea);
            }
        }
        // draw Y axes grids
        for (int i = 0; i < stackCount; i++) {
            AxisWrapper leftAxis = yAxisList.get(yPositionToIndex(i, YAxisPosition.LEFT));
            AxisWrapper rightAxis = yAxisList.get(yPositionToIndex(i, YAxisPosition.RIGHT));
            if (!traceList.isYAxisUsed(rightAxis) && !traceList.isYAxisUsed(leftAxis)) {
                // do nothing
            } else if (!traceList.isYAxisUsed(leftAxis)) {
                rightAxis.drawGrid(canvas, graphArea);
            } else if (!traceList.isYAxisUsed(rightAxis)) {
                leftAxis.drawGrid(canvas, graphArea);
            } else { // both axis is used
                if (config.getPrimaryYPosition() == YAxisPosition.LEFT) {
                    leftAxis.drawGrid(canvas, graphArea);
                } else {
                    rightAxis.drawGrid(canvas, graphArea);
                }
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
        tooltip.draw(canvas, new BRectangle(0, 0, width, height));
    }

    public int stackCount() {
        return yAxisList.size() / 2;
    }

    /**
     * return COPY of chart legendConfig. To change chart legendConfig use setConfig
     */
    public ChartConfig getConfig() {
        return new ChartConfig(config);
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
        invalidate();
    }

    public void setTitle(String title) {
        this.title = new Title(title, config.getTitleConfig());
        invalidate();
    }

    public void setTraceData(int traceIndex, ChartData data) {
        traceList.setData(traceIndex, data);
    }

    public void setStackWeight(int stack, int weight) {
        checkStackNumber(stack);
        stackWeights.set(stack, weight);
        invalidate();
    }

    public void addStack() {
        addStack(config.getDefaultStackWeight());
    }

    public void addStack(int weight) {
        AxisWrapper leftAxis = new AxisWrapper(new Axis(new LinearScale(), config.getYAxisConfig(), YAxisPosition.LEFT));
        AxisWrapper rightAxis = new AxisWrapper(new Axis(new LinearScale(), config.getYAxisConfig(), YAxisPosition.RIGHT));
        yAxisList.add(leftAxis);
        yAxisList.add(rightAxis);
        stackWeights.add(weight);
        invalidate();
    }

    /**
     * @throws IllegalStateException if stack axis are used by some trace traces and
     *                               therefor can not be deleted
     */
    public void removeStack(int stack) throws IllegalStateException {
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
    public void addTrace(ChartData data, TracePainter tracePainter) {
        int stack = Math.max(0, yAxisList.size() / 2 - 1);
        addTrace(data, tracePainter, stack);
    }

    /**
     * add traces to the stack with the given number
     */
    public void addTrace(ChartData data, TracePainter tracePainter, int stack) {
        addTrace(data, tracePainter, stack, config.getPrimaryXPosition(), config.getPrimaryYPosition());
    }

    public void addTrace(ChartData data, TracePainter tracePainter, XAxisPosition xPosition, YAxisPosition yPosition) {
        int stack = Math.max(0, yAxisList.size() / 2 - 1);
        addTrace(data, tracePainter, stack, xPosition, yPosition);
    }

    /**
     * add traces to the stack with the given number
     */
    public void addTrace(ChartData data, TracePainter tracePainter, int stack, XAxisPosition xPosition, YAxisPosition yPosition) throws IllegalArgumentException {
        if (yAxisList.size() == 0) {
            addStack(); // add stack if there is no stack
        }
        checkStackNumber(stack);
        int xIndex = xPositionToIndex(xPosition);
        int yIndex = yPositionToIndex(stack, yPosition);

        AxisWrapper xAxis = xAxisList.get(xIndex);
        AxisWrapper yAxis = yAxisList.get(yIndex);
        StringSequence dataXLabels = getXLabels(data);
        if (dataXLabels != null) {
            xAxis.setScale(new CategoryScale(dataXLabels));
        }

        BColor[] colors = config.getTraceColors();
        Trace trace = new Trace(data, tracePainter, xAxis, yAxis, colors[traceList.size() % colors.length], "Trace " + traceList.size());
        traceList.add(trace);
    }

    public void setTraceName(int traceIndex, String name) {
        traceList.setName(traceIndex, name);
    }

    public void setTraceColor(int traceIndex, BColor color) {
        traceList.setColor(traceIndex, color);
    }

    public @Nullable
    StringSequence getXLabels(ChartData data) {
        if (!data.isNumberColumn(0)) {
            return new StringSequence() {
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

    public void removeTrace(int traceIndex) {
        traceList.remove(traceIndex);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        invalidate();
    }

    public int traceCount() {
        return traceList.size();
    }

    /**
     * return COPY of X axis legendConfig. To change axis legendConfig use setXConfig
     */
    public AxisConfig getXConfig(XAxisPosition xPosition) {
        return xAxisList.get(xPositionToIndex(xPosition)).getConfig();
    }

    /**
     * return COPY of Y axis legendConfig. To change axis legendConfig use setYConfig
     */
    public AxisConfig getYConfig(int stack, YAxisPosition yPosition) {
        return yAxisList.get(yPositionToIndex(stack, yPosition)).getConfig();
    }


    public void setXPrefixAndSuffix(XAxisPosition xPosition, @Nullable String prefix, @Nullable String suffix) {
        AxisConfig axisConfig = getXConfig(xPosition);
        axisConfig.setTickLabelPrefixAndSuffix(prefix, suffix);
        xAxisList.get(xPositionToIndex(xPosition)).setConfig(axisConfig);
        invalidate();
    }

    public void setYPrefixAndSuffix(int stack, YAxisPosition yPosition, @Nullable String prefix, @Nullable String suffix) {
        AxisConfig axisConfig = getYConfig(stack, yPosition);
        axisConfig.setTickLabelPrefixAndSuffix(prefix, suffix);
        yAxisList.get(yPositionToIndex(stack, yPosition)).setConfig(axisConfig);
        invalidate();
    }

    public void setXTitle(XAxisPosition xPosition, @Nullable String title) {
        xAxisList.get(xPositionToIndex(xPosition)).setTitle(title);
        invalidate();
    }

    public void setYTitle(int stack, YAxisPosition yPosition, @Nullable String title) {
        yAxisList.get(yPositionToIndex(stack, yPosition)).setTitle(title);
        invalidate();
    }

    public void setXMinMax(XAxisPosition xPosition, double min, double max) {
        setAxisMinMax(xAxisList.get(xPositionToIndex(xPosition)), min, max, false);

    }

    public void setYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        setAxisMinMax(yAxisList.get(yPositionToIndex(stack, yPosition)), min, max, false);
    }

    public void autoScaleX(int xIndex) {
        AxisWrapper xAxis = xAxisList.get(xIndex);
        Range xMinMax = traceList.getTracesXMinMax(xAxis);
        setAxisMinMax(xAxis, xMinMax, true);
    }

    public void autoScaleX() {
        Range xMinMax = null;
        int selectedTrace = traceList.getSelection();
        if (selectedTrace >= 0) {
            AxisWrapper xAxis = traceList.getTraceX(selectedTrace);
            xMinMax = traceList.getTracesXMinMax(xAxis);
            setAxisMinMax(xAxis, xMinMax, true);
        } else {
            for (AxisWrapper xAxis : xAxisList) {
                xMinMax = traceList.getTracesXMinMax(xAxis);
                setAxisMinMax(xAxis, xMinMax, true);
            }
        }
    }

    public void autoScaleY(int yIndex) {
        AxisWrapper yAxis = yAxisList.get(yIndex);
        Range yMinMax = traceList.getTracesYMinMax(yAxis);
        ;
        setAxisMinMax(yAxis, yMinMax, true);
    }

    public void autoScaleY() {
        Range yMinMax = null;
        int selectedTrace = traceList.getSelection();
        if (selectedTrace >= 0) {
            AxisWrapper yAxis = traceList.getTraceY(selectedTrace);
            yMinMax = traceList.getTracesYMinMax(yAxis);
            setAxisMinMax(yAxis, yMinMax, true);
        } else {
            for (AxisWrapper yAxis : yAxisList) {
                yMinMax = traceList.getTracesYMinMax(yAxis);
                setAxisMinMax(yAxis, yMinMax, true);
            }
        }
    }

    public void autoScaleY(int stack, YAxisPosition yPosition) {
        autoScaleY(yPositionToIndex(stack, yPosition));
    }

    public void autoScaleX(XAxisPosition xPosition) {
        autoScaleX(xPositionToIndex(xPosition));
    }

    public void setXScale(XAxisPosition xPosition, Scale scale) {
        xAxisList.get(xPositionToIndex(xPosition)).setScale(scale);
    }

    public void setYScale(int stack, YAxisPosition yPosition, Scale scale) {
        yAxisList.get(yPositionToIndex(stack, yPosition)).setScale(scale);
        invalidate();
    }

    public void zoomY(int stack, YAxisPosition yPosition, double zoomFactor) {
        AxisWrapper axis = yAxisList.get(yPositionToIndex(stack, yPosition));
        setAxisMinMax(axis, axis.zoomedMinMax(zoomFactor), false);
    }

    public void zoomX(XAxisPosition xPosition, double zoomFactor) {
        AxisWrapper axis = xAxisList.get(xPositionToIndex(xPosition));
        setAxisMinMax(axis, axis.zoomedMinMax(zoomFactor), false);
    }

    public void translateY(int stack, YAxisPosition yPosition, int translation) {
        AxisWrapper axis = yAxisList.get(yPositionToIndex(stack, yPosition));
        setAxisMinMax(axis, axis.translatedMinMax(translation), false);
    }

    public void translateX(XAxisPosition xPosition, int translation) {
        AxisWrapper axis = xAxisList.get(xPositionToIndex(xPosition));
        setAxisMinMax(axis, axis.translatedMinMax(translation), false);

    }

    public Range getAllTracesXMinMax() {
        return traceList.getAllTracesXMinMax();
    }

    public double getTracesBestExtent(XAxisPosition xAxisPosition) {
        return traceList.getTracesBestExtent(xAxisList.get(xPositionToIndex(xAxisPosition)), width);
    }

    public boolean isTraceSelected() {
        return traceList.getSelection() >= 0;
    }

    XAxisPosition getSelectedTraceX() {
        return xIndexToPosition(xAxisToIndex(traceList.getTraceX(traceList.getSelection())));
    }

    int getSelectedTraceStack() {
        return yAxisToIndex(traceList.getTraceY(traceList.getSelection())) / 2;

    }

    YAxisPosition getSelectedTraceY() {
        return yIndexToPosition(yAxisToIndex(traceList.getTraceY(traceList.getSelection())));

    }

    public boolean selectTrace(int x, int y) {
        if (legend != null) {
            return legend.selectTrace(x, y);
        }
        return false;
    }
}
