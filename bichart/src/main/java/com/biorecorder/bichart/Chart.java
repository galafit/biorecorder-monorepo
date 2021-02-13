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
public class Chart implements SizeChangeListener {
    private ChartConfig config = new ChartConfig();
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

    private BRectangle graphArea = new BRectangle(0, 0, 0, 0);
    private int width;
    private int height;

    private Tooltip tooltip;

    private boolean isValid = true;

    public Chart() {
        this(DarkTheme.getChartConfig());
    }

    public Chart(ChartConfig config) {
        this.config = new ChartConfig(config);

        AxisWrapper bottomAxis = new AxisWrapper(new Axis(new LinearScale(), config.getXAxisConfig(), XAxisPosition.BOTTOM));
        AxisWrapper topAxis = new AxisWrapper(new Axis(new LinearScale(), config.getXAxisConfig(), XAxisPosition.TOP));
        bottomAxis.addSizeChangeListener(this);
        topAxis.addSizeChangeListener(this);
        xAxisList.add(bottomAxis);
        xAxisList.add(topAxis);
        addStack();

        legend = new Legend(config.getLegendConfig(), traceList, width);

        title = new Title(config.getTitleConfig());
        tooltip = new Tooltip(config.getTooltipConfig());
    }

    @Override
    public void onSizeChanged() {
        invalidate();
    }

    private void invalidate() {
        isValid = false;
    }


    public void revalidate(RenderContext renderContext) {
        if(isValid){
            return;
        }
        graphArea = new BRectangle(0, 0, width, height);
        if (width == 0 || height == 0) {
            return;
        }
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
            return;
        }
        
        int titleHeight = title.getHeight(renderContext, width);
        setXStartEnd(graphArea.x, graphArea.width);
        int top = titleHeight;
        int bottom = 0;
        AxisWrapper topAxis = xAxisList.get(xPositionToIndex(XAxisPosition.TOP));
        AxisWrapper bottomAxis = xAxisList.get(xPositionToIndex(XAxisPosition.BOTTOM));
        if(topAxis.isUsed()) {
            top += topAxis.getWidth(renderContext);
        }
        if(bottomAxis.isUsed()) {
            bottom += bottomAxis.getWidth(renderContext);
        }
        BDimension legendPrefSize = legend.getPrefferedSize(renderContext);
        if(legend.isTop()) {
            legend.moveTo(0, titleHeight);
            top += legendPrefSize.height;
        } else if (legend.isBottom()) {
            legend.moveTo(0, height - legendPrefSize.height);
            bottom += legendPrefSize.height;
        } else {
            legend.moveTo(0, titleHeight + (height - titleHeight)/2);
        }

        setYStartEnd(top, height - top - bottom);

        // recalculate with precise y axis width
        int left = 0;
        int right = 0;
        for (int i = 0; i < yAxisList.size(); i++) {
            AxisWrapper yAxis = yAxisList.get(i);
            if(yAxis.isUsed()) {
                if (i % 2 == 0) {
                    left = Math.max(left, yAxis.getWidth(renderContext));
                } else {
                    right = Math.max(right, yAxis.getWidth(renderContext));
                }
            }
        }

        graphArea = new BRectangle(left, top,
                Math.max(0, width - left - right), Math.max(0, height - top - bottom));


        // adjust XAxis ranges
        setXStartEnd(graphArea.x, graphArea.width);
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
    private YAxisPosition getYPosition(int yIndex) {
        if ((yIndex & 1) == 0) {
            return YAxisPosition.LEFT;
        }

        return YAxisPosition.RIGHT;
    }

    /**
     * X-axis: 0(even) - BOTTOM and 1(odd) - TOP
     */
    private XAxisPosition xPositionToIndex(int xIndex) {
        if ((xIndex & 1) == 0) {
            return XAxisPosition.BOTTOM;
        }
        return XAxisPosition.TOP;
    }


    /**
     * =============================================================*
     * Protected method for careful use                            *
     * ==============================================================
     */

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
            if (xAxisList.get(xPositionToIndex(position)).isUsed()) {
                positions.add(position);
            }

        }
        return positions.toArray(new XAxisPosition[positions.size()]);
    }

    YAxisPosition[] getYAxes(int stack) {
        List<YAxisPosition> positions = new ArrayList<>();
        for (YAxisPosition position : YAxisPosition.values()) {
            if (yAxisList.get(yPositionToIndex(stack, position)).isUsed()) {
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

    XAxisPosition getXAxisPosition(BPoint point) {
        return xPositionToIndex(xAxisToIndex(getXAxis(point)));
    }

    AxisWrapper getXAxis(BPoint point) {
        if (new BRectangle(0, 0, width, height).contains(point.getX(), point.getY())) {
            int bottomAxisIndex = xPositionToIndex(XAxisPosition.BOTTOM);
            int topAxisIndex = xPositionToIndex(XAxisPosition.TOP);
            AxisWrapper bottomAxis = xAxisList.get(bottomAxisIndex);
            AxisWrapper topAxis = xAxisList.get(topAxisIndex);
            if (!bottomAxis.isUsed() && !topAxis.isUsed()) {
                return null;
            } else if (!topAxis.isUsed()) {
                return bottomAxis;
            } else if (!bottomAxis.isUsed()) {
                return topAxis;
            } else { // both axis is used
                // find point stack
                int stackCount = yAxisList.size() / 2;
                for (int stack = 0; stack < stackCount; stack++) {
                    AxisWrapper axisLeft = yAxisList.get(2 * stack);
                    if (axisLeft.getEnd() <= point.getY() && axisLeft.getStart() >= point.getY()) {
                        return traceList.getUsedXAxis(yAxisList.get(2 * stack), yAxisList.get(2 * stack + 1));
                    }
                }
            }
        }
        return null;
    }

    boolean hoverOff() {
       return tooltip.setHoverPoint(null);
    }

    boolean hoverOn(int x, int y) {
        if (!graphArea.contains(x, y)) {
            return hoverOff();
        }
        return tooltip.setHoverPoint(traceList.getNearest(x, y));
    }

    /**
     * =================================================*
     * Base methods to interact               *
     * ==================================================
     */
    public void draw(BCanvas canvas) {
        if(!isValid) {
            revalidate(canvas.getRenderContext());
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
            int bottomAxisIndex = xPositionToIndex(XAxisPosition.BOTTOM);
            int topAxisIndex = xPositionToIndex(XAxisPosition.TOP);
            AxisWrapper bottomAxis = xAxisList.get(bottomAxisIndex);
            AxisWrapper topAxis = xAxisList.get(topAxisIndex);
            if (!bottomAxis.isUsed() && !topAxis.isUsed()) {
                // do nothing
            } else if (!bottomAxis.isUsed()) {
                topAxis.drawGrid(canvas, stackArea);
            } else if (!topAxis.isUsed()) {
                bottomAxis.drawGrid(canvas, stackArea);
            } else { // both axis used
                AxisWrapper xAxisWithGrid = traceList.getUsedXAxis(yAxisList.get(stack * 2), yAxisList.get(stack * 2 + 1));
                if (xAxisWithGrid.isUsed()) {
                    xAxisWithGrid.drawGrid(canvas, stackArea);
                }
            }
        }
        // draw Y axes grids
        for (int i = 0; i < stackCount; i++) {
            AxisWrapper leftAxis = yAxisList.get(yPositionToIndex(i, YAxisPosition.LEFT));
            AxisWrapper rightAxis = yAxisList.get(yPositionToIndex(i, YAxisPosition.RIGHT));
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
        traceList.draw(canvas);
        canvas.restore();
        legend.draw(canvas);
        tooltip.draw(canvas, new BRectangle(0, 0, width, height));
    }

    public void setXConfig(XAxisPosition xPosition, AxisConfig axisConfig) {
        xAxisList.get(xPositionToIndex(xPosition)).setConfig(axisConfig);
    }

    public void setYConfig(int stack, YAxisPosition yPosition, AxisConfig axisConfig) {
        yAxisList.get(yPositionToIndex(stack, yPosition)).setConfig(axisConfig);
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
        tooltip.setConfig(config.getTooltipConfig());

        BColor[] colors = this.config.getTraceColors();

        for (int i = 0; i < traceList.size(); i++) {
            traceList.setColor(i, colors[i % colors.length]);
        }
        invalidate();
    }

    public void setTitle(String title) {
        this.title.setTitle(title);
        invalidate();
    }

    public void setTraceColor(int traceIndex, BColor color) {
        traceList.setColor(traceIndex, color);
    }

    public void setTraceName(int traceIndex, String name) {
        traceList.setName(traceIndex, name);
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
        leftAxis.addSizeChangeListener(this);
        rightAxis.addSizeChangeListener(this);
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
        if(traceList.isStackUsed(yAxisList.get(stack * 2), yAxisList.get(stack * 2 + 1))) {
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
        addTrace(data, tracePainter,  stack, xPosition, yPosition);
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

    public @Nullable StringSequence getXLabels(ChartData data) {
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
        legend.setWidth(width);
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
    }

    public void setYPrefixAndSuffix(int stack, YAxisPosition yPosition, @Nullable String prefix, @Nullable String suffix) {
        AxisConfig axisConfig = getYConfig(stack, yPosition);
        axisConfig.setTickLabelPrefixAndSuffix(prefix, suffix);
        yAxisList.get(yPositionToIndex(stack, yPosition)).setConfig(axisConfig);

    }

    public void setXTitle(XAxisPosition xPosition, @Nullable String title) {
        xAxisList.get(xPositionToIndex(xPosition)).setTitle(title);
    }

    public void setYTitle(int stack, YAxisPosition yPosition, @Nullable String title) {
        yAxisList.get(yPositionToIndex(stack, yPosition)).setTitle(title);
    }


    public void setXMinMax(XAxisPosition xPosition, double min, double max) {
        yAxisList.get(xPositionToIndex(xPosition)).setMinMax(min, max, false);

    }

    public void setYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        yAxisList.get(yPositionToIndex(stack, yPosition)).setMinMax(min, max, false);
    }

    public void autoScaleX(int xIndex){
        AxisWrapper xAxis = xAxisList.get(xIndex);
        if(!xAxis.isUsed()) {
            return;
        }
        Range tracesXMinMax = traceList.getTracesXMinMax(xAxis);
        if (tracesXMinMax != null) {
            xAxis.setMinMax(tracesXMinMax.getMin(), tracesXMinMax.getMax(), true);
        }
    }

    public void autoScaleX() {
        for (int xIndex = 0; xIndex < xAxisList.size(); xIndex++) {
          autoScaleX(xIndex);
        }
    }

    public void autoScaleX(XAxisPosition xPosition) {
        autoScaleX(xPositionToIndex(xPosition));
    }

    public void autoScaleY(int yIndex) {
        AxisWrapper yAxis = yAxisList.get(yIndex);
        if(!yAxis.isUsed()) {
            return;
        }
        Range tracesYMinMax = traceList.getTracesYMinMax(yAxis);
        if (tracesYMinMax != null) {
            yAxis.setMinMax(tracesYMinMax.getMin(), tracesYMinMax.getMax(), true);
        }
    }

    public void autoScaleY(int stack, YAxisPosition yPosition) {
        autoScaleY(yPositionToIndex(stack, yPosition));
    }

    public void autoScaleY() {
        for (int yIndex = 0; yIndex < yAxisList.size(); yIndex++) {
            autoScaleY(yIndex);
        }
    }

    public void setXScale(XAxisPosition xPosition, Scale scale) {
        xAxisList.get(xPositionToIndex(xPosition)).setScale(scale);
    }

    public void setYScale(int stack, YAxisPosition yPosition, Scale scale) {
        yAxisList.get(yPositionToIndex(stack, yPosition)).setScale(scale);
    }


    public void zoomY(int stack, YAxisPosition yPosition, double zoomFactor) {
        yAxisList.get(yPositionToIndex(stack, yPosition)).zoom(zoomFactor);
    }

    public void zoomX(XAxisPosition xPosition, double zoomFactor) {
        xAxisList.get(xPositionToIndex(xPosition)).zoom(zoomFactor);
    }

    public void translateY(int stack, YAxisPosition yPosition, int translation) {
        yAxisList.get(yPositionToIndex(stack, yPosition)).translate(translation);
    }

    public void translateX(XAxisPosition xPosition, int translation) {
        xAxisList.get(xPositionToIndex(xPosition)).translate(translation);
    }

    public Range getAllTracesFullMinMax() {
        return traceList.getAllTracesFullMinMax();
    }

    public double getTracesBestExtent(XAxisPosition xAxisPosition) {
       return traceList.getTracesBestExtent(xAxisList.get(xPositionToIndex(xAxisPosition)), width);
    }

    public boolean isTraceSelected() {
        return traceList.getSelection() >= 0;
    }

    XAxisPosition getSelectedTraceX() {
        return xPositionToIndex(xAxisToIndex(traceList.getTraceX(traceList.getSelection())));
    }

    int getSelectedTraceStack() {
        return yAxisToIndex(traceList.getTraceY(traceList.getSelection())) / 2;

    }

    YAxisPosition getSelectedTraceY() {
        return getYPosition(yAxisToIndex(traceList.getTraceY(traceList.getSelection())));

    }

    public boolean selectTrace(int x, int y) {
        return legend.selectTrace(x, y);
    }

    private int xAxisToIndex(AxisWrapper x) {
        for (int i = 0; i < xAxisList.size(); i++) {
            if(xAxisList.get(i) == x) {
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

}
