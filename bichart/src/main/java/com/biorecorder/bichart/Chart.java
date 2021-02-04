package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.*;
import com.biorecorder.bichart.button.ButtonGroup;
import com.biorecorder.bichart.button.SwitchButton;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.CategoryScale;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.bichart.utils.StringUtils;
import com.biorecorder.data.sequence.StringSequence;
import com.sun.istack.internal.Nullable;

import java.util.*;
import java.util.List;

/**
 * Created by hdablin on 24.03.17.
 */
public class Chart {
    private ChartConfig config = new ChartConfig();
    /*
     * 2 X-axis: 0(even) - BOTTOM and 1(odd) - TOP
     * 2 Y-axis for every section(stack): even - LEFT and odd - RIGHT;
     * All LEFT and RIGHT Y-axis are stacked.
     * If there is no trace associated with some axis... this axis is invisible.
     **/
    private List<AxisWrapper> xAxisList = new ArrayList<>(2);
    private List<AxisWrapper> yAxisList = new ArrayList<>();
    private Map<Integer, Range> xAxisToMinMax = new HashMap<>(1);
    private Map<Integer, Range> yAxisToMinMax = new HashMap<>(1);

    private ArrayList<Integer> stackWeights = new ArrayList<Integer>();
    private List<DataPainter> dataPainters = new ArrayList<DataPainter>();
    private Legend legend;
    private Title title;

    private BRectangle graphArea = new BRectangle(0, 0, 0, 0);
    private int width;
    private int height;

    private Tooltip tooltip;

    private DataPainterTracePoint hoverPoint;

    private boolean isDirty = true;

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
        //legend
        legend = new Legend(config.getLegendConfig());
        //title
        title = new Title(config.getTitleConfig());
    }

    private boolean isLegendEnabled() {
        if (legend == null || !legend.isEnabled()) {
            return false;
        }
        return true;
    }

    private void setXMinMax(RenderContext renderContext) {
        for (int xIndex = 0; xIndex < xAxisList.size(); xIndex++) {
            AxisWrapper xAxis = xAxisList.get(xIndex);
            if (xAxis.isUsed()) {
                Range minMax = xAxisToMinMax.get(xIndex);
                if (minMax != null) {
                    // NO ROUNDING !!! course the X min and max depends data processing
                    xAxis.setMinMax(minMax.getMin(), minMax.getMax());
                } else { // auto scale X
                    Range tracesXMinMax = null;
                    for (DataPainter trace : dataPainters) {
                        if (trace.getXIndex() == xIndex) {
                            tracesXMinMax = Range.join(tracesXMinMax, trace.xMinMax());
                        }
                    }

                    if (tracesXMinMax != null) {
                        if (xAxis.getScale() instanceof CategoryScale) {
                            CategoryScale xScale = (CategoryScale) xAxis.getScale();
                            xAxis.setMinMax(xScale.normalizeMin(tracesXMinMax.getMin()), xScale.normalizeMax(tracesXMinMax.getMax()));
                        } else {
                            xAxis.setMinMax(tracesXMinMax.getMin(), tracesXMinMax.getMax());

                        }
                    }
                    // rounding only in the case of auto scale when no data processing
                    if (xAxis.isRoundingEnabled()) {
                        // xAxis.roundMinMax(canvas);
                    }
                }
                xAxis.update(renderContext);
            }
        }
    }

    private void setYMinMax(RenderContext renderContext) {
        for (int yIndex = 0; yIndex < yAxisList.size(); yIndex++) {
            AxisWrapper yAxis = yAxisList.get(yIndex);
            if (yAxis.isUsed()) {
                Range minMax = yAxisToMinMax.get(yIndex);
                if (minMax != null) {
                    yAxis.setMinMax(minMax.getMin(), minMax.getMax());
                } else { // auto scale Y
                    Range tracesYMinMax = null;
                    for (DataPainter dataPainter : dataPainters) {
                        int traceCount = dataPainter.traceCount();
                        for (int trace = 0; trace < traceCount; trace++) {
                            if (getTraceYIndex(dataPainter, trace) == yIndex) {
                                tracesYMinMax = Range.join(tracesYMinMax, dataPainter.traceYMinMax(trace));
                            }
                        }
                    }
                    if (tracesYMinMax != null) {
                        if (yAxis.getScale() instanceof CategoryScale) {
                            CategoryScale yScale = (CategoryScale) yAxis.getScale();
                            yAxis.setMinMax(yScale.normalizeMin(tracesYMinMax.getMin()), yScale.normalizeMax(tracesYMinMax.getMax()));
                        } else {
                            yAxis.setMinMax(tracesYMinMax.getMin(), tracesYMinMax.getMax());
                            // rounding only in the case of auto scale
                            if (yAxis.isRoundingEnabled()) {
                                //  yAxis.roundMinMax(canvas);
                            }
                        }
                    }
                }
                yAxis.update(renderContext);
            }
        }
    }

    public void update(RenderContext renderContext) {
        graphArea = new BRectangle(0, 0, width, height);
        if (width == 0 || height == 0) {
            return;
        }
        // all calculation with x axes must be done always first course data processing depends on it!!!
        legend.invalidate();
        if (config.getMargin() != null) { // fixed margin
            Insets margin = config.getMargin();
            int graphAreaWidth = width - margin.left() - margin.right();
            int graphAreaHeight = height - margin.top() - margin.bottom();
            if (graphAreaHeight < 0) {
                graphAreaHeight = 0;
            }
            if (graphAreaWidth < 0) {
                graphAreaWidth = 0;
            }
            graphArea = new BRectangle(margin.left(), margin.top(), graphAreaWidth, graphAreaHeight);
            setXStartEnd(graphArea.x, graphArea.width);
            setYStartEnd(graphArea.y, graphArea.height);
            setXMinMax(renderContext);
            setYMinMax(renderContext);
            return;
        }
        
        int titleHeight = title.getHeight(renderContext, width);

        setXStartEnd(graphArea.x, graphArea.width);
        setXMinMax(renderContext);

        int top = titleHeight;
        int bottom = 0;
        AxisWrapper topAxis = xAxisList.get(getXIndex(XAxisPosition.TOP));
        AxisWrapper bottomAxis = xAxisList.get(getXIndex(XAxisPosition.BOTTOM));
        if(topAxis.isUsed()) {
            top += topAxis.getWidth();
        }
        if(bottomAxis.isUsed()) {
            bottom += bottomAxis.getWidth();
        }

        if (isLegendEnabled() ) {
            int legendHeight = legend.getHeight(renderContext);
            if (legend.isTop()) {
                top += legendHeight;
            }
            if (legend.isBottom()) {
                bottom += legendHeight;
            }
        }

        setYStartEnd(top, height - top - bottom);
        setYMinMax(renderContext);

        // recalculate with precise y axis width
        int left = 0;
        int right = 0;
        for (int i = 0; i < yAxisList.size(); i++) {
            AxisWrapper yAxis = yAxisList.get(i);
            if(yAxis.isUsed()) {
                if (i % 2 == 0) {
                    left = Math.max(left, yAxis.getWidth());
                } else {
                    right = Math.max(right, yAxis.getWidth());
                }
            }
        }

        graphArea = new BRectangle(left, top,
                Math.max(0, width - left - right), Math.max(0, height - top - bottom));


        // adjust XAxis ranges
        setXStartEnd(graphArea.x, graphArea.width);
        setXMinMax(renderContext);
        legend.validate(renderContext);
        isDirty = false;
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

    private int chooseXAxisWithGrid(int stack) {
        int primaryAxisIndex = getXIndex(config.getPrimaryXPosition());

        int leftAxisIndex = getYIndex(stack, YAxisPosition.LEFT);
        int rightAxisIndex = getYIndex(stack, YAxisPosition.RIGHT);
        for (DataPainter dataPainter : dataPainters) {
            if (dataPainter.getXIndex() == primaryAxisIndex) {
                for (int trace = 0; trace < dataPainter.traceCount(); trace++) {
                    int traceYIndex = getTraceYIndex(dataPainter, trace);
                    if (traceYIndex == leftAxisIndex || traceYIndex == rightAxisIndex) {
                        return primaryAxisIndex;
                    }
                }
            }
        }
        if (config.getPrimaryXPosition() == XAxisPosition.BOTTOM) {
            return getXIndex(XAxisPosition.TOP);
        } else {
            return getXIndex(XAxisPosition.BOTTOM);
        }
    }

    private void checkStackNumber(int stack) {
        int stackCount = yAxisList.size() / 2;
        if (stack >= stackCount) {
            String errMsg = "Stack = " + stack + " Number of stacks: " + stackCount;
            throw new IllegalArgumentException(errMsg);
        }
    }

    private int getYIndex(int stack, YAxisPosition yPosition) {
        if (yPosition == YAxisPosition.LEFT) {
            return 2 * stack;
        } else {
            return 2 * stack + 1;
        }
    }

    private int getXIndex(XAxisPosition xPosition) {
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
    private YAxisPosition getYPosition(int yIndex) {
        if ((yIndex & 1) == 0) {
            return YAxisPosition.LEFT;
        }

        return YAxisPosition.RIGHT;
    }

    /**
     * X-axis: 0(even) - BOTTOM and 1(odd) - TOP
     */
    private XAxisPosition getXPosition(int xIndex) {
        if ((xIndex & 1) == 0) {
            return XAxisPosition.BOTTOM;
        }
        return XAxisPosition.TOP;
    }

    int getTraceYIndex(DataPainter dataPainter, int trace) {
        if (dataPainter.isSplit()) {
            return dataPainter.getYStartIndex() + trace * 2;
        } else {
            return dataPainter.getYStartIndex();
        }
    }

    private void updateTooltipAndCrosshairs(DataPainterTracePoint hoverPoint) {
        DataPainter dataPainter = hoverPoint.getDataPainter();
        AxisWrapper xAxis = xAxisList.get(dataPainter.getXIndex());
        AxisWrapper[] traceYAxes = new AxisWrapper[dataPainter.traceCount()];
        for (int trace = 0; trace < dataPainter.traceCount(); trace++) {
            traceYAxes[trace] = yAxisList.get(getTraceYIndex(dataPainter, trace));
        }
        Scale[] traceYScales = new Scale[traceYAxes.length];
        for (int i = 0; i < traceYAxes.length; i++) {
            traceYScales[i] = traceYAxes[i].getScale();
        }
        tooltip = dataPainter.createTooltip(config.getTooltipConfig(), hoverPoint.getPointIndex(), hoverPoint.getTrace(), xAxis.getScale(), traceYScales);
    }

    /**
     * =============================================================*
     * Protected method for careful use                            *
     * ==============================================================
     */

    double getBestExtent(XAxisPosition xAxisPosition, RenderContext renderContext) {
        double extent = xAxisList.get(getXIndex(xAxisPosition)).getBestExtent(renderContext, width);

        double tracesExtent = getTracesBestExtent(xAxisPosition);
        if (extent < 0) {
            extent = tracesExtent;
        } else if (tracesExtent > 0) {
            extent = Math.min(extent, tracesExtent);
        }
        return extent;
    }

    double getTracesBestExtent(XAxisPosition xAxisPosition) {
        double extent = -1;
        for (DataPainter trace : dataPainters) {
            if (trace.getXIndex() == getXIndex(xAxisPosition)) {
                double traceExtent = trace.getBestExtent(width);
                if (extent < 0) {
                    extent = traceExtent;
                } else if (traceExtent > 0) {
                    extent = Math.min(extent, traceExtent);
                }
            }
        }
        return extent;
    }


    // for all x axis
    Range getAllTracesFullMinMax() {
        Range minMax = null;
        for (DataPainter trace : dataPainters) {
            Range traceXMinMax = trace.xMinMax();
            AxisWrapper traceXAxis = xAxisList.get(trace.getXIndex());
            if (traceXMinMax != null && traceXAxis.getScale() instanceof CategoryScale) {
                CategoryScale xScale = (CategoryScale) traceXAxis.getScale();
                traceXMinMax = new Range(xScale.normalizeMin(traceXMinMax.getMin()), xScale.normalizeMax(traceXMinMax.getMax()));
            }

            minMax = Range.join(minMax, traceXMinMax);
        }
        return minMax;
    }


    int getStacksSumWeight() {
        int weightSum = 0;
        for (Integer weight : stackWeights) {
            weightSum += weight;
        }
        return weightSum;
    }

    double scale(XAxisPosition xAxisPosition, double value) {
        return xAxisList.get(getXIndex(xAxisPosition)).getScale().scale(value);
    }

    double invert(XAxisPosition xAxisPosition, double value) {
        return xAxisList.get(getXIndex(xAxisPosition)).getScale().invert(value);
    }

    Range getYMinMax(int stack, YAxisPosition yAxisPosition, BCanvas canvas) {
        if (isDirty) {
            update(canvas.getRenderContext());
        }
        AxisWrapper yAxis = yAxisList.get(getYIndex(stack, yAxisPosition));
        return new Range(yAxis.getMin(), yAxis.getMax());
    }

    private DataPainterTrace getSelectedTrace() {
        if (legend != null) {
            return legend.getSelectedTrace();
        }
        return null;
    }

    public boolean isTraceSelected() {
        return getSelectedTrace() != null;
    }

    XAxisPosition getSelectedTraceX() {
        return getXPosition(getSelectedTrace().getDataPainter().getXIndex());
    }

    int getSelectedTraceStack() {
        DataPainterTrace selectedTrace = getSelectedTrace();
        int yIndex = getTraceYIndex(selectedTrace.getDataPainter(), selectedTrace.getTrace());
        return getYStack(yIndex);
    }


    YAxisPosition getSelectedTraceY() {
        DataPainterTrace selectedTrace = getSelectedTrace();
        int yIndex = getTraceYIndex(selectedTrace.getDataPainter(), selectedTrace.getTrace());
        return getYPosition(yIndex);
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
            if (xAxisList.get(getXIndex(position)).isUsed()) {
                positions.add(position);
            }

        }
        return positions.toArray(new XAxisPosition[positions.size()]);
    }

    YAxisPosition[] getYAxes(int stack) {
        List<YAxisPosition> positions = new ArrayList<>();
        for (YAxisPosition position : YAxisPosition.values()) {
            if (yAxisList.get(getYIndex(stack, position)).isUsed()) {
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
                    if (!axisLeft.isUsed() && !axisRight.isUsed()) {
                        return null;
                    }
                    if (!axisLeft.isUsed()) {
                        return YAxisPosition.RIGHT;
                    }
                    if (!axisRight.isUsed()) {
                        return YAxisPosition.LEFT;
                    }
                    if (0 <= point.getX() && point.getX() <= width / 2 && axisLeft.isUsed()) { // left half
                        return YAxisPosition.LEFT;
                    } else {
                        return YAxisPosition.RIGHT;
                    }
                }
            }
        }
        return null;
    }

    XAxisPosition getXAxis(BPoint point) {
        if (new BRectangle(0, 0, width, height).contains(point.getX(), point.getY())) {
            int bottomAxisIndex = getXIndex(XAxisPosition.BOTTOM);
            int topAxisIndex = getXIndex(XAxisPosition.TOP);
            ;
            AxisWrapper bottomAxis = xAxisList.get(bottomAxisIndex);
            AxisWrapper topAxis = xAxisList.get(topAxisIndex);
            if (!bottomAxis.isUsed() && !topAxis.isUsed()) {
                return null;
            } else if (!topAxis.isUsed()) {
                return XAxisPosition.BOTTOM;
            } else if (!bottomAxis.isUsed()) {
                return XAxisPosition.TOP;
            } else { // both axis is used
                // find point stack
                int stackCount = yAxisList.size() / 2;
                for (int stack = 0; stack < stackCount; stack++) {
                    AxisWrapper axisLeft = yAxisList.get(2 * stack);
                    if (axisLeft.getEnd() <= point.getY() && axisLeft.getStart() >= point.getY()) {
                        return getXPosition(chooseXAxisWithGrid(stack));
                    }
                }
            }
        }
        return null;
    }

    boolean hoverOff() {
        if (hoverPoint != null) {
            hoverPoint = null;
            tooltip = null;
            return true;
        }
        return false;
    }

    boolean hoverOn(int x, int y) {
        if (!graphArea.contains(x, y)) {
            return hoverOff();
        }
        DataPainterTrace selectedTrace = getSelectedTrace();
        if (selectedTrace != null) {
            Scale xScale = xAxisList.get(selectedTrace.getDataPainter().getXIndex()).getScale();
            Scale yScale = yAxisList.get(getTraceYIndex(selectedTrace.getDataPainter(), selectedTrace.getTrace())).getScale();
            NearestTracePoint nearestTracePoint = selectedTrace.getDataPainter().nearest(x, y, selectedTrace.getTrace(), xScale, yScale);
            if (nearestTracePoint != null) {
                if (nearestTracePoint.getTracePoint().equals(hoverPoint)) {
                    return false;
                } else {
                    hoverPoint = nearestTracePoint.getTracePoint();
                    updateTooltipAndCrosshairs(hoverPoint);
                    return true;
                }
            } else if (hoverPoint == null) {
                return false;
            }
            return true;
        }

        if (hoverPoint != null) {
            Scale xScale = xAxisList.get(hoverPoint.getDataPainter().getXIndex()).getScale();
            Scale yScale = yAxisList.get(getTraceYIndex(hoverPoint.getDataPainter(), hoverPoint.getTrace())).getScale();
            NearestTracePoint nearestTracePoint = hoverPoint.getDataPainter().nearest(x, y, hoverPoint.getTrace(), xScale, yScale);
            if (nearestTracePoint != null) {
                if (nearestTracePoint.getTracePoint().equals(hoverPoint)) {
                    return false;
                } else {
                    hoverPoint = nearestTracePoint.getTracePoint();
                    updateTooltipAndCrosshairs(hoverPoint);
                    return true;
                }
            }
        }

        // find nearest trace trace
        NearestTracePoint closestTracePoint = null;
        for (DataPainter dataPainter : dataPainters) {
            Scale[] traceYScales = new Scale[dataPainter.traceCount()];
            for (int trace = 0; trace < dataPainter.traceCount(); trace++) {
                traceYScales[trace] = yAxisList.get(getTraceYIndex(dataPainter, trace)).getScale();
            }
            Scale xScale = xAxisList.get(dataPainter.getXIndex()).getScale();

            NearestTracePoint nearestTracePoint = dataPainter.nearest(x, y, xScale, traceYScales);
            if (nearestTracePoint != null) {
                if (nearestTracePoint.getDistanceSqw() == 0) {
                    closestTracePoint = nearestTracePoint;
                    break;
                } else {
                    if (closestTracePoint == null || closestTracePoint.getDistanceSqw() > nearestTracePoint.getDistanceSqw()) {
                        closestTracePoint = nearestTracePoint;
                    }
                }
            }
        }

        if (closestTracePoint != null) {
            hoverPoint = closestTracePoint.getTracePoint();
            updateTooltipAndCrosshairs(hoverPoint);
            return true;
        }

        return false;
    }

    /**
     * =================================================*
     * Base methods to interact               *
     * ==================================================
     */
    public void draw(BCanvas canvas) {
        if(isDirty) {
            update(canvas.getRenderContext());
        }
        if (width == 0 || height == 0) {
            return;
        }
        canvas.enableAntiAliasAndHinting();
        canvas.setColor(config.getMarginColor());
        canvas.fillRect(0, 0, width, height);
        //draw title
        title.draw(canvas);

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
            AxisWrapper yAxis = yAxisList.get(2 * stack);
            BRectangle stackArea = new BRectangle(graphArea.x, (int) yAxis.getEnd(), graphArea.width, (int) yAxis.length());
            int bottomAxisIndex = getXIndex(XAxisPosition.BOTTOM);
            int topAxisIndex = getXIndex(XAxisPosition.TOP);
            AxisWrapper bottomAxis = xAxisList.get(bottomAxisIndex);
            AxisWrapper topAxis = xAxisList.get(topAxisIndex);
            if (!bottomAxis.isUsed() && !topAxis.isUsed()) {
                // do nothing
            } else if (!bottomAxis.isUsed()) {
                topAxis.drawGrid(canvas, stackArea);
            } else if (!topAxis.isUsed()) {
                bottomAxis.drawGrid(canvas, stackArea);
            } else { // both axis used
                AxisWrapper xAxisWithGrid = xAxisList.get(chooseXAxisWithGrid(stack));
                if (xAxisWithGrid.isUsed()) {
                    xAxisWithGrid.drawGrid(canvas, stackArea);
                }
            }
        }
        // draw Y axes grids
        for (int i = 0; i < stackCount; i++) {
            AxisWrapper leftAxis = yAxisList.get(getYIndex(i, YAxisPosition.LEFT));
            AxisWrapper rightAxis = yAxisList.get(getYIndex(i, YAxisPosition.RIGHT));
            if (!rightAxis.isUsed() && !leftAxis.isUsed()) {
                // do nothing
            } else if (!leftAxis.isUsed()) {
                rightAxis.drawGrid(canvas, graphArea);
            } else if (!rightAxis.isUsed()) {
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
            if (axis.isUsed()) {
                axis.drawAxis(canvas, graphArea);
            }
        }

        // draw Y axes
        for (AxisWrapper axis : yAxisList) {
            if (axis.isUsed()) {
                axis.drawAxis(canvas, graphArea);
            }
        }
        canvas.save();
        canvas.setClip(graphArea.x, graphArea.y, graphArea.width, graphArea.height);
        for (DataPainter dataPainter : dataPainters) {
            for (int trace = 0; trace < dataPainter.traceCount(); trace++) {
                dataPainter.drawTrace(canvas, trace, xAxisList.get(dataPainter.getXIndex()).getScale(), yAxisList.get(getTraceYIndex(dataPainter, trace)).getScale());
            }
        }
        canvas.restore();
        if (isLegendEnabled()) {
            legend.draw(canvas);
        }
        if (tooltip != null) {
            for (Crosshair crosshair : tooltip.getXCrosshairs()) {
                xAxisList.get(crosshair.getAxisIndex()).drawCrosshair(canvas, graphArea, crosshair.getPosition());
            }
            for (Crosshair crosshair : tooltip.getYCrosshairs()) {
                yAxisList.get(crosshair.getAxisIndex()).drawCrosshair(canvas, graphArea, crosshair.getPosition());
            }
            tooltip.draw(canvas, new BRectangle(0, 0, width, height));
        }

    }

    public int stackCount() {
        return yAxisList.size() / 2;
    }


    public String[] getTraceNames() {
        List<String> names = new ArrayList<>();
        for (DataPainter trace : dataPainters) {
            for (int i = 0; i < trace.traceCount(); i++) {
                names.add(trace.getTraceName(i));
            }
        }
        String[] namesArr = new String[names.size()];
        return names.toArray(namesArr);
    }

    private int dataPainterTraceToGeneralTraceNumber(DataPainterTrace dataPainterTrace) throws IllegalArgumentException {
        int traceCount = 0;
        for (DataPainter dataPainter : dataPainters) {
            if (dataPainter == dataPainterTrace.getDataPainter()) {
                traceCount += dataPainterTrace.getTrace();
                return traceCount;
            }
            traceCount += dataPainter.traceCount();
        }
        String errMsg = "Invalid DataPainterTrace. Corresponding DataPainter does not exist";
        throw new IllegalArgumentException(errMsg);
    }

    private DataPainterTrace generalTraceNumberToDataPainterTrace(int trace) throws IllegalArgumentException {
        int traceCount = 0;
        for (DataPainter dataPainter : dataPainters) {
            if (trace < traceCount + dataPainter.traceCount()) {
                return new DataPainterTrace(dataPainter, trace - traceCount);
            }
            traceCount += dataPainter.traceCount();
        }
        String errMsg = "Invalid trace number. No DataPainter corresponds it: " + trace;
        throw new IllegalArgumentException(errMsg);


    }

    public int getTraceNumberByName(String name) {
        for (int i = 0; i < dataPainters.size(); i++) {
            DataPainter dataPainter = dataPainters.get(i);
            for (int trace = 0; trace < dataPainter.traceCount(); trace++) {
                if (dataPainter.getTraceName(trace).equals(name)) {
                    return dataPainterTraceToGeneralTraceNumber(new DataPainterTrace(dataPainter, trace));
                }
            }
        }
        return -1;
    }

    public int getSelectedTraceNumber() {
        DataPainterTrace selectedTrace = getSelectedTrace();
        if (selectedTrace != null) {
            return dataPainterTraceToGeneralTraceNumber(selectedTrace);
        }
        return -1;
    }

    /**
     * return COPY of chart legendConfig. To change chart legendConfig use setConfig
     */
    public ChartConfig getConfig() {
        return new ChartConfig(config);
    }


    public void setConfig(ChartConfig chartConfig) {
        this.config = new ChartConfig(chartConfig);
        title.setConfig(config.getTitleConfig());
        for (int i = 0; i < xAxisList.size(); i++) {
            xAxisList.get(i).setConfig(this.config.getXAxisConfig());
        }
        for (int i = 0; i < yAxisList.size(); i++) {
            yAxisList.get(i).setConfig(this.config.getYAxisConfig());
        }
        legend.setConfig(config.getLegendConfig());

        BColor[] colors = this.config.getTraceColors();
        int trace = 0;
        for (DataPainter dataPainter : dataPainters) {
            for (int i = 0; i < dataPainter.traceCount(); i++) {
                dataPainter.setTraceColor(i, colors[(trace + i) % colors.length]);
                trace++;
            }
        }
        isDirty = true;
    }


    public void setTitle(String title) {
        this.title.setTitle(title);
        isDirty = true;
    }

    public void setTraceColor(int trace, BColor color) {
        DataPainterTrace dataPainterTrace = generalTraceNumberToDataPainterTrace(trace);
        dataPainterTrace.getDataPainter().setTraceColor(dataPainterTrace.getTrace(), color);
    }

    public void setTraceName(int trace, String name) {
        DataPainterTrace dataPainterTrace = generalTraceNumberToDataPainterTrace(trace);
        dataPainterTrace.getDataPainter().setTraceName(dataPainterTrace.getTrace(), name);
        legend.setTraceName(dataPainterTrace, name);
    }

    public void setStackWeight(int stack, int weight) {
        checkStackNumber(stack);
        stackWeights.set(stack, weight);
        isDirty = true;
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

        isDirty = true;
    }

    /**
     * @throws IllegalStateException if stack axis are used by some trace traces and
     *                               therefor can not be deleted
     */
    public void removeStack(int stack) throws IllegalStateException {
        // check that no trace use that stack
        int leftYIndex = stack * 2;
        int rightYIndex = stack * 2 + 1;

        for (DataPainter dataPainter : dataPainters) {
            for (int trace = 0; trace < dataPainter.traceCount(); trace++) {
                int traceYIndex = getTraceYIndex(dataPainter, trace);
                if (traceYIndex == leftYIndex || traceYIndex == rightYIndex) {
                    String errMsg = "Stack: " + stack + "can not be removed. It is used by trace";
                    throw new IllegalStateException(errMsg);
                }
            }

            if (dataPainter.getYStartIndex() > leftYIndex) {
                dataPainter.setYStartIndex(dataPainter.getYStartIndex() - 2);
            }
        }

        stackWeights.remove(stack);
        yAxisList.remove(stack * 2 + 1);
        yAxisList.remove(stack * 2);
        isDirty = true;
    }

    /**
     * add trace to the last stack
     */
    public void addTraces(ChartData data, TracePainter tracePainter) {
        addTraces(data, tracePainter, true);
    }

    /**
     * add trace to the last stack
     */
    public void addTraces(ChartData data, TracePainter tracePainter, boolean isSplit) {
        int stack = Math.max(0, yAxisList.size() / 2 - 1);
        addTraces(data, tracePainter, isSplit, stack);
    }

    /**
     * add trace to the stack with the given number
     */
    public void addTraces(ChartData data, TracePainter tracePainter, boolean isSplit, int stack) {
        addTraces(data, tracePainter, isSplit, stack, config.getPrimaryXPosition(), config.getPrimaryYPosition());
    }

    public void addTraces(ChartData data, TracePainter tracePainter, boolean isSplit, XAxisPosition xPosition, YAxisPosition yPosition) {
        int stack = Math.max(0, yAxisList.size() / 2 - 1);
        addTraces(data, tracePainter, isSplit, stack, xPosition, yPosition);
    }

    /**
     * add trace to the stack with the given number
     */
    public void addTraces(ChartData data, TracePainter tracePainter, boolean isSplit, int stack, XAxisPosition xPosition, YAxisPosition yPosition) throws IllegalArgumentException {
        DataPainter dataPainter = new DataPainter(data, tracePainter, isSplit, getXIndex(xPosition), getYIndex(stack, yPosition));
        if (dataPainter.traceCount() < 1) {
            String errMsg = "Number of trace traces: " + dataPainter.traceCount() + ". Please specify valid trace data";
            throw new IllegalArgumentException(errMsg);
        }

        if (yAxisList.size() == 0) {
            addStack(); // add stack if there is no stack
        }
        checkStackNumber(stack);
        int xIndex = getXIndex(xPosition);
        int yIndex = getYIndex(stack, yPosition);

        AxisWrapper xAxis = xAxisList.get(xIndex);
        StringSequence dataXLabels = dataPainter.getXLabels();
        if (dataXLabels != null) {
            xAxis.setScale(new CategoryScale(dataXLabels));
        }
        xAxis.setUsed(true);

        if (!isSplit) {
            AxisWrapper yAxis = yAxisList.get(yIndex);
            yAxis.setUsed(true);
        } else {
            int stackCount = yAxisList.size() / 2;
            int availableStacks = stackCount - stack;
            if (dataPainter.traceCount() > availableStacks) {
                for (int i = 0; i < dataPainter.traceCount() - availableStacks; i++) {
                    addStack();
                }
            }
            for (int trace = 0; trace < dataPainter.traceCount(); trace++) {
                AxisWrapper yAxis = yAxisList.get(yIndex + trace * 2);
                yAxis.setUsed(true);
            }
        }

        BColor[] colors = config.getTraceColors();
        int totalTraces = 0;
        for (DataPainter trace1 : dataPainters) {
            totalTraces += trace1.traceCount();
        }
        for (int i = 0; i < dataPainter.traceCount(); i++) {
            int trace = totalTraces + i;
            if (dataPainter.getTraceColor(i) == null) {
                dataPainter.setTraceColor(i, colors[trace % colors.length]);
            }
            if (StringUtils.isNullOrBlank(dataPainter.getTraceName(i))) {
                dataPainter.setTraceName(i, "Trace" + trace);
            }
        }

        dataPainters.add(dataPainter);

        if (isLegendEnabled()) {
            for (int i = 0; i < dataPainter.traceCount(); i++) {
                final int traceNumber = i;
                legend.add(new DataPainterTrace(dataPainter, traceNumber));
            }
        }
        isDirty = true;
    }

    public void removeTrace(int trace) {
        DataPainterTrace dataPainterTrace = generalTraceNumberToDataPainterTrace(trace);
        DataPainter dataPainter = dataPainterTrace.getDataPainter();

        if (isLegendEnabled()) {
            legend.remove(dataPainterTrace);
        }
        dataPainter.hideTrace(dataPainterTrace.getTrace());
        // try to remove empty stack
        int traceStartStack = dataPainter.getYStartIndex() / 2;
        try {
            removeStack(traceStartStack + dataPainter.traceCount());
        } catch (IllegalStateException ex) {
            // do nothing;
        }
        // hide unused axis
        for (int i = 0; i < xAxisList.size(); i++) {
            boolean isUsed = false;
            for (DataPainter painter : dataPainters) {
                if (painter.traceCount() > 0 && painter.getXIndex() == i) {
                    isUsed = true;
                    break;
                }
            }
            if (!isUsed) {
                xAxisList.get(i).setUsed(false);
            }
        }
        for (int i = 0; i < yAxisList.size(); i++) {
            boolean isUsed = false;
            for (DataPainter painter : dataPainters) {
                for (int painterTrace = 0; painterTrace < painter.traceCount(); painterTrace++) {
                    if (getTraceYIndex(painter, painterTrace) == i) {
                        isUsed = true;
                        break;
                    }
                }
            }
            if (!isUsed) {
                yAxisList.get(i).setUsed(false);
            }
        }
        isDirty = true;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        isDirty = true;
    }

    public int traceCount() {
        return dataPainters.size();
    }


    /**
     * return COPY of X axis legendConfig. To change axis legendConfig use setXConfig
     */
    public AxisConfig getXConfig(XAxisPosition xPosition) {
        return xAxisList.get(getXIndex(xPosition)).getConfig();
    }

    /**
     * return COPY of Y axis legendConfig. To change axis legendConfig use setYConfig
     */
    public AxisConfig getYConfig(int stack, YAxisPosition yPosition) {
        return yAxisList.get(getYIndex(stack, yPosition)).getConfig();
    }

    public void setXConfig(XAxisPosition xPosition, AxisConfig axisConfig) {
        xAxisList.get(getXIndex(xPosition)).setConfig(axisConfig);
        isDirty = true;
    }

    public void setYConfig(int stack, YAxisPosition yPosition, AxisConfig axisConfig) {
        yAxisList.get(getYIndex(stack, yPosition)).setConfig(axisConfig);
        isDirty = true;
    }

    public void setXPrefixAndSuffix(XAxisPosition xPosition, @Nullable String prefix, @Nullable String suffix) {
        AxisConfig axisConfig = getXConfig(xPosition);
        axisConfig.setTickLabelPrefixAndSuffix(prefix, suffix);
        setXConfig(xPosition, axisConfig);
    }

    public void setYPrefixAndSuffix(int stack, YAxisPosition yPosition, @Nullable String prefix, @Nullable String suffix) {
        AxisConfig axisConfig = getYConfig(stack, yPosition);
        axisConfig.setTickLabelPrefixAndSuffix(prefix, suffix);
        setYConfig(stack, yPosition, axisConfig);
    }

    public void setXTitle(XAxisPosition xPosition, @Nullable String title) {
        xAxisList.get(getXIndex(xPosition)).setTitle(title);
        isDirty = true;
    }

    public void setYTitle(int stack, YAxisPosition yPosition, @Nullable String title) {
        yAxisList.get(getYIndex(stack, yPosition)).setTitle(title);
        isDirty = true;
    }


    public void setXMinMax(XAxisPosition xPosition, double min, double max) {
        xAxisToMinMax.put(getXIndex(xPosition), new Range(min, max));
        isDirty = true;
    }

    public void setYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        yAxisToMinMax.put(getYIndex(stack, yPosition), new Range(min, max));
        isDirty = true;
    }


    public void autoScaleX(XAxisPosition xPosition) {
        xAxisToMinMax.remove(getXIndex(xPosition));
        isDirty = true;
    }

    public void autoScaleY(int stack, YAxisPosition yPosition) {
        yAxisToMinMax.remove(getYIndex(stack, yPosition));
        isDirty = true;
    }

    public void setXScale(XAxisPosition xPosition, Scale scale) {
        xAxisList.get(getXIndex(xPosition)).setScale(scale);
        isDirty = true;
    }

    public void setYScale(int stack, YAxisPosition yPosition, Scale scale) {
        yAxisList.get(getYIndex(stack, yPosition)).setScale(scale);
        isDirty = true;
    }


    public void zoomY(int stack, YAxisPosition yPosition, double zoomFactor) {
        Scale zoomedScale = yAxisList.get(getYIndex(stack, yPosition)).zoom(zoomFactor);
        double zoomedMin = zoomedScale.getMin();
        double zoomedMax = zoomedScale.getMax();
        setYMinMax(stack, yPosition, zoomedMin, zoomedMax);
    }

    public void zoomX(XAxisPosition xPosition, double zoomFactor) {
        Scale zoomedScale = xAxisList.get(getXIndex(xPosition)).zoom(zoomFactor);
        double zoomedMin = zoomedScale.getMin();
        double zoomedMax = zoomedScale.getMax();
        setXMinMax(xPosition, zoomedMin, zoomedMax);
    }

    public void translateY(int stack, YAxisPosition yPosition, int translation) {
        Scale translatedScale = yAxisList.get(getYIndex(stack, yPosition)).translate(translation);
        double translatedMin = translatedScale.getMin();
        double translatedMax = translatedScale.getMax();
        setYMinMax(stack, yPosition, translatedMin, translatedMax);
    }

    public void translateX(XAxisPosition xPosition, int translation) {
        Scale translatedScale = xAxisList.get(getXIndex(xPosition)).translate(translation);
        double translatedMin = translatedScale.getMin();
        double translatedMax = translatedScale.getMax();
        setXMinMax(xPosition, translatedMin, translatedMax);
    }


    public boolean selectTrace(int x, int y) {
        if (isLegendEnabled() && legend.selectItem(x, y)) {
            return true;
        }
        return false;
    }


    class Legend {
        private ButtonGroup buttonGroup;
        // only LinkedHashMap will iterate in the order in which the entries were put into the map
        private Map<SwitchButton, DataPainterTrace> buttonsToDataPainterTraces = new LinkedHashMap<>();
        private LegendConfig legendConfig;
        private int legendHeight;
        private boolean isDirty;

        public Legend(LegendConfig legendConfig) {
            this.legendConfig = legendConfig;
            this.buttonGroup = new ButtonGroup();
        }

        public DataPainterTrace getSelectedTrace() {
            return buttonsToDataPainterTraces.get(buttonGroup.getSelection());
        }

        public boolean isEnabled() {
            return legendConfig.isEnabled();
        }
        
        public int getHeight(RenderContext renderContext) {
            if(legendConfig.isAttachedToStacks()) {
                return 0;
            } else {
                if(isDirty) {
                    validate(renderContext);
                }
                return legendHeight;
            }

        }

        public boolean isTop() {
            if (legendConfig.getVerticalAlign() == VerticalAlign.TOP) {
                return true;
            }
            return false;
        }

        public boolean isBottom() {
            if (legendConfig.getVerticalAlign() == VerticalAlign.BOTTOM) {
                return true;
            }
            return false;
        }

        public boolean selectItem(int x, int y) {
            for (SwitchButton button : buttonsToDataPainterTraces.keySet()) {
                if (button.getBounds().contains(x, y)) {
                    button.switchState();
                    return true;
                }
            }
            return false;
        }

        public void setTraceName(DataPainterTrace dataPainterTrace, String name) {
            for (SwitchButton button : buttonsToDataPainterTraces.keySet()) {
                if (buttonsToDataPainterTraces.get(button).equals(dataPainterTrace)) {
                    button.setLabel(name);
                    return;
                }
            }
            invalidate();
        }

        public void setConfig(LegendConfig legendConfig) {
            this.legendConfig = legendConfig;
            for (SwitchButton button : buttonsToDataPainterTraces.keySet()) {
                button.setBackgroundColor(this.legendConfig.getBackgroundColor());
                button.setTextStyle(this.legendConfig.getTextStyle());
                button.setMargin(this.legendConfig.getButtonsMargin());
                button.setBorderWidth(this.legendConfig.getBorderWidth());
            }
            invalidate();
        }

        public void add(DataPainterTrace dataPainterTrace) {
            // add trace legend button
            SwitchButton traceButton = new SwitchButton(dataPainterTrace.getDataPainter().getTraceName(dataPainterTrace.getTrace()));
            buttonsToDataPainterTraces.put(traceButton, dataPainterTrace);
            buttonGroup.add(traceButton);
            traceButton.setBackgroundColor(legendConfig.getBackgroundColor());
            traceButton.setTextStyle(legendConfig.getTextStyle());
            traceButton.setMargin(legendConfig.getButtonsMargin());
            traceButton.setBorderWidth(legendConfig.getBorderWidth());
            invalidate();
        }

        public void remove(DataPainterTrace dataPainterTrace) {
            SwitchButton buttonToRemove = null;
            for (SwitchButton button : buttonsToDataPainterTraces.keySet()) {
                DataPainterTrace buttonDataPainterTrace = buttonsToDataPainterTraces.get(button);
                if (buttonDataPainterTrace.equals(dataPainterTrace)) {
                    buttonToRemove = button;
                    break;
                }
            }
            if (buttonToRemove != null) {
                buttonGroup.remove(buttonToRemove);
                buttonsToDataPainterTraces.remove(buttonToRemove);
            }
            invalidate();
        }

        private void invalidate() {
            isDirty = true;
        }

        public void validate(RenderContext renderContext) {
            if(!isDirty) {
                return;
            }
            // only LinkedHashMap will iterate in the order in which the entries were put into the map
            Map<BRectangle, List<SwitchButton>> areaToButtons = new LinkedHashMap<>();
            if(legendConfig.isAttachedToStacks()) {
                BRectangle legendArea = graphArea;
                for (SwitchButton button : buttonsToDataPainterTraces.keySet()) {
                    DataPainterTrace dataPainterTrace = buttonsToDataPainterTraces.get(button);

                    Scale yScale = yAxisList.get(getTraceYIndex(dataPainterTrace.getDataPainter(), dataPainterTrace.getTrace())).getScale();
                    BRectangle traceArea = new BRectangle(legendArea.x, (int)yScale.getEnd(), legendArea.width, (int)yScale.getLength());
                    List<SwitchButton> areaButtons = areaToButtons.get(traceArea);
                    if (areaButtons == null) {
                        areaButtons = new ArrayList<>();
                        areaToButtons.put(traceArea, areaButtons);
                    }
                    areaButtons.add(button);
                }
            } else {
                BRectangle legendArea = new BRectangle(0, title.getHeight(renderContext, width), width, height);
                List<SwitchButton> areaButtons = new ArrayList<>();
                areaToButtons.put(legendArea, areaButtons);
                for (SwitchButton button : buttonsToDataPainterTraces.keySet()) {
                    areaButtons.add(button);
                }
            }

            List<SwitchButton> lineButtons = new ArrayList<SwitchButton>();
            for (BRectangle area : areaToButtons.keySet()) {
                List<SwitchButton> areaButtons = areaToButtons.get(area);
                legendHeight = 0;
                int legendWidth = 0;
                int x = area.x;
                int y = area.y;
                for (SwitchButton button : areaButtons) {
                    BDimension btnDimension = button.getPrefferedSize(renderContext);
                    if (legendHeight == 0) {
                        legendHeight = btnDimension.height;
                        lineButtons.clear();
                    }
                    if (lineButtons.size() > 0 && x + legendConfig.getInterItemSpace() + btnDimension.width >= area.x + area.width) {
                        legendWidth += (lineButtons.size() - 1) * legendConfig.getInterItemSpace();
                        if (legendConfig.getHorizontalAlign() == HorizontalAlign.LEFT) {
                            moveButtons(lineButtons, 0, 0);
                        }
                        if (legendConfig.getHorizontalAlign() == HorizontalAlign.RIGHT) {
                            moveButtons(lineButtons, area.width - legendWidth, 0);
                        }
                        if (legendConfig.getHorizontalAlign() == HorizontalAlign.CENTER) {
                            moveButtons(lineButtons, (area.width - legendWidth) / 2, 0);
                        }

                        x = area.x;
                        y += btnDimension.height + legendConfig.getInterLineSpace();
                        button.setBounds(x, y, btnDimension.width, btnDimension.height);

                        x += btnDimension.width + legendConfig.getInterItemSpace();
                        legendHeight += btnDimension.height + legendConfig.getInterLineSpace();
                        legendWidth = btnDimension.width;
                        lineButtons.clear();
                        lineButtons.add(button);
                    } else {
                        button.setBounds(x, y, btnDimension.width, btnDimension.height);
                        x += legendConfig.getInterItemSpace() + btnDimension.width;
                        legendWidth += btnDimension.width;
                        lineButtons.add(button);
                    }
                }

                legendWidth += (lineButtons.size() - 1) * legendConfig.getInterItemSpace();

                if (legendConfig.getHorizontalAlign() == HorizontalAlign.LEFT) {
                    moveButtons(lineButtons,0 , 0);
                }
                if (legendConfig.getHorizontalAlign() == HorizontalAlign.RIGHT) {
                    moveButtons(lineButtons, area.width - legendWidth, 0);
                }
                if (legendConfig.getHorizontalAlign() == HorizontalAlign.CENTER) {
                    moveButtons(lineButtons, (area.width - legendWidth) / 2, 0);
                }

                if (legendConfig.getVerticalAlign() == VerticalAlign.TOP) {
                    moveButtons(lineButtons, 0, 0);
                }
                if (legendConfig.getVerticalAlign() == VerticalAlign.BOTTOM) {
                    moveButtons(lineButtons, 0, area.height - legendHeight - 0);
                }
                if (legendConfig.getVerticalAlign() == VerticalAlign.MIDDLE) {
                    moveButtons(lineButtons, 0, (area.height - legendHeight) / 2);
                }
            }
            isDirty = false;
        }

        private void moveButtons(List<SwitchButton> buttons, int dx, int dy) {
            if (dx != 0 || dy != 0) {
                for (SwitchButton button : buttons) {
                    BRectangle btnBounds = button.getBounds();
                    button.setBounds(btnBounds.x + dx, btnBounds.y + dy, btnBounds.width, btnBounds.height);
                }
            }
        }

        public void draw(BCanvas canvas) {
            if (buttonsToDataPainterTraces.size() == 0) {
                return;
            }
            canvas.setTextStyle(legendConfig.getTextStyle());
            for (SwitchButton button : buttonsToDataPainterTraces.keySet()) {
                DataPainterTrace dataPainterTrace = buttonsToDataPainterTraces.get(button);
                button.setColor(dataPainterTrace.getDataPainter().getTraceColor(dataPainterTrace.getTrace()));
                button.draw(canvas);
            }
        }
    }
}
