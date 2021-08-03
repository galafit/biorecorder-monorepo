package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scroll.Scroll;
import com.biorecorder.bichart.scroll.ScrollListener;
import com.biorecorder.bichart.scroll.ScrollScale;
import com.biorecorder.bichart.traces.TracePainter;
import com.sun.istack.internal.Nullable;

import java.util.HashMap;
import java.util.Map;

public class NavigableChart1 {
    private int gap = 0; // between Chart and Preview px
    private Insets spacing = new Insets(5);
    private int navigatorHeightMin = 16; // px
    private XAxisPosition navigatorXPosition = XAxisPosition.BOTTOM;
    private Chart chart;
    private Chart navigator;
    private NavigableChartConfig config;
    private int width;
    private int height;
    private Map<XAxisPosition, Scroll> axisToScrolls = new HashMap<>(2);

    private boolean isValid = false;
    private boolean isChanged = false;

    public NavigableChart1(NavigableChartConfig config, Scale xScale) {
        this.config = new NavigableChartConfig(config);
        chart = new Chart(config.getChartConfig(), xScale);
        navigator = new Chart(config.getNavigatorConfig(), xScale);
        chart.setSpacing(new Insets(0));
        navigator.setSpacing(new Insets(0));
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            ScrollScale scrollScale = new ScrollScale() {
                @Override
                public double positionToValue(double x) {
                    return navigator.invert(navigatorXPosition, x);

                }

                @Override
                public double valueToPosition(double value) {
                    return navigator.scale(navigatorXPosition, value);
                }
            };

            Scroll scroll = new Scroll(new ScrollModelActive(chart, navigator, xPosition), scrollScale, config.getScrollConfig());
            Range navigatorRange = navigator.getXMinMax(navigatorXPosition);
            double scrollExtent = navigatorRange.length()/10;
            scroll.setValues(navigatorRange.getMin(),scrollExtent, navigatorRange.getMin(), navigatorRange.getMax());
            scroll.addListener(new ScrollListener() {
                @Override
                public void onScrollChanged() {
                    isChanged = true;
                }
            });
            axisToScrolls.put(xPosition, scroll);
        }
    }

    public void invalidate() {
        isValid = false;
    }

    public void revalidate(RenderContext renderContext) {
        int top = spacing.top();
        int bottom = spacing.bottom();
        int left = spacing.left();
        int right = spacing.right();

        int width1 = width - left - right;
        int height1 = height - top - bottom;
        if (height1 > gap) {
            height1 -= gap;
        }

        int navigatorHeight;
        if (navigator.traceCount() == 0) {
            navigatorHeight = Math.min(navigatorHeightMin, height1 / 2);
        } else {
            int chartWeight = chart.getStacksSumWeight();
            int navigatorWeight = navigator.getStacksSumWeight();
            navigatorHeight = height1 * navigatorWeight / (chartWeight + navigatorWeight);
        }

        int chartHeight = height1 - navigatorHeight;

        BRectangle chartArea = new BRectangle(left, top, width1, chartHeight);
        BRectangle navigatorArea = new BRectangle(left, height - navigatorHeight, width1, navigatorHeight);
        chart.setBounds(chartArea.x, chartArea.y, chartArea.width, chartArea.height);
        navigator.setBounds(navigatorArea.x, navigatorArea.y, navigatorArea.width, navigatorArea.height);
        //  зануляем маргины чтобы они считались автоматом
        chart.setMargin(null);
        navigator.setMargin(null);
        //  выравниваем маргины
        Insets chartMargin = chart.getMargin(renderContext);
        Insets navigatorMargin = navigator.getMargin(renderContext);
        if (chartMargin.left() != navigatorMargin.left() ||
                chartMargin.right() != navigatorMargin.right()) {
            int leftMargin = Math.max(chartMargin.left(), navigatorMargin.left());
            int rightMargin = Math.max(chartMargin.right(), navigatorMargin.right());
            chart.setMargin(new Insets(chartMargin.top(), rightMargin, chartMargin.bottom(), leftMargin));
            navigator.setMargin(new Insets(navigatorMargin.top(), rightMargin, navigatorMargin.bottom(), leftMargin));
        }
    }

    /**
     * ==================================================*
     * Base methods to interact                          *
     * ==================================================*
     */

    public void draw(BCanvas canvas) {
        if (!isValid) {
            revalidate(canvas.getRenderContext());
        }
        canvas.setColor(config.getBackgroundColor());
        canvas.fillRect(0, 0, width, height);
        chart.draw(canvas);
        canvas.save();
        navigator.draw(canvas);
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).draw(canvas, navigator.getBounds());
        }
    }

    public int getChartTraceMarkSize(int traceNumber) {
        return  chart.getTraceMarkSize(traceNumber);
    }

    public int getNavigatorTraceMarkSize(int traceNumber) {
        return  navigator.getTraceMarkSize(traceNumber);
    }

    public Scale getNavigatorXScale() {
        return navigator.getXScale(navigatorXPosition);
    }

    public Scale getChartXScale(XAxisPosition xAxisPosition) {
        return chart.getXScale(xAxisPosition);
    }

    public XAxisPosition getChartTraceXAxisPosition(int traceNumber) {
        return chart.getTraceXAxisPosition(traceNumber);
    }
    public XAxisPosition getChartDefaultXAxisPosition() {
        return chart.getDefaultXAxisPosition();
    }


    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        invalidate();
    }

    public void setConfig(NavigableChartConfig config) {
        this.config = new NavigableChartConfig(config);
        chart.setConfig(config.getChartConfig());
        navigator.setConfig(config.getNavigatorConfig());
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setConfig(config.getScrollConfig());
        }
        invalidate();
    }

    public void setTitle(String title) {
        chart.setTitle(title);
    }


    /**
     * =======================Base methods to interact with chart ==========================
     **/

    public void autoScaleChartY(int stack, YAxisPosition yPosition) {
        chart.autoScaleY(stack, yPosition);
    }

    public void autoScaleChartY() {
        chart.autoScaleY();
    }

    public void addChartStack(int weight) {
        chart.addStack(weight);
        invalidate();
    }

    public void addChartStack() {
        chart.addStack();
        invalidate();
    }

   /* public void setChartStackWeight(int stack, int weight) throws IllegalArgumentException {
        chart.setStackWeight(stack, weight);
        invalidate();
    }*/

    /**
     * @throws IllegalStateException if stack axis are used by some trace traces and
     *                               therefore can not be deleted
     */
    public void removeChartStack(int stackNumber) throws IllegalStateException, IllegalArgumentException {
        chart.removeStack(stackNumber);
        invalidate();
    }

    public void addChartTrace(String name, ChartData data, TracePainter tracePainter) {
        chart.addTrace(name, data, tracePainter);
        invalidate();
    }


    public void addChartTrace(String name, ChartData data, TracePainter tracePainter, XAxisPosition xPosition, YAxisPosition yPosition) {
        chart.addTrace(name, data, tracePainter, xPosition, yPosition);
        invalidate();
    }

    public void addChartTrace(String name, ChartData data, TracePainter tracePainter, int stackNumber) {
        chart.addTrace(name, data, tracePainter, stackNumber);
        invalidate();
    }

    public void addChartTrace(String name, ChartData data, TracePainter tracePainter, int stackNumber, XAxisPosition xPosition, YAxisPosition yPosition) {
        chart.addTrace(name, data, tracePainter, stackNumber, xPosition, yPosition);
        invalidate();
    }

    public void removeChartTrace(int traceNumber) {
        chart.removeTrace(traceNumber);
        invalidate();
    }

    public int chartTraceCount() {
        return chart.traceCount();
    }

    public void setChartXTitle(XAxisPosition xPosition, String title) {
        chart.setXTitle(xPosition, title);
    }

    public void setChartYTitle(int stack, YAxisPosition yPosition, String title) {
        chart.setYTitle(stack, yPosition, title);
    }

    public void setChartXScale(XAxisPosition xPosition, Scale scale) {
        chart.setXScale(xPosition, scale);
    }

    public void setChartXPrefixAndSuffix(XAxisPosition xPosition, @Nullable String prefix, @Nullable String suffix) {
        navigator.setXPrefixAndSuffix(xPosition, prefix, suffix);
    }

    public void setChartYPrefixAndSuffix(int stack, YAxisPosition yPosition, @Nullable String prefix, @Nullable String suffix) {
        navigator.setYPrefixAndSuffix(stack, yPosition, prefix, suffix);
    }

    public void setChartYScale(int stack, YAxisPosition yPosition, Scale scale) {
        chart.setYScale(stack, yPosition, scale);
    }

    public void setChartTraceColor(int trace, BColor color) {
        chart.setTraceColor(trace, color);
    }

    public void setChartTraceName(int trace, String name) {
        chart.setTraceName(trace, name);
    }


    public void setChartYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        chart.setYMinMax(stack, yPosition, min, max);
    }

    public int chartStackCount() {
        return chart.stackCount();
    }


    /**
     * =======================Base methods to interact with navigator==========================
     **/

    public int navigatorStackCount() {
        return navigator.stackCount();
    }

    public void addNavigatorStack() {
        navigator.addStack();
        invalidate();
    }

    public void addNavigatorStack(int weight) {
        navigator.addStack(weight);
        invalidate();
    }

   /* public void setNavigatorStackWeight(int stack, int weight) {
        navigator.setStackWeight(stack, weight);
        invalidate();
    }*/

    /**
     * @throws IllegalStateException if stack axis are used by some trace traces and
     *                               therefor can not be deleted
     */
    public void removeNavigatorStack(int stack) throws IllegalStateException, IllegalArgumentException {
        navigator.removeStack(stack);
        invalidate();
    }

    public void addNavigatorTrace(String name, ChartData data, TracePainter tracePainter) {
        navigator.addTrace(name, data, tracePainter);
    }

    public void addNavigatorTrace(String name,ChartData data, TracePainter tracePainter, int stack) {
        navigator.addTrace(name, data, tracePainter, stack);
    }

    public void removeNavigatorTrace(int traceNumber) {
        navigator.removeTrace(traceNumber);
    }

    public int navigatorTraceCount() {
        return navigator.traceCount();
    }

    public void setNavigatorTraceColor(int trace, BColor color) {
        navigator.setTraceColor(trace, color);
    }

    public void setNavigatorTraceName(int trace, String name) {
        navigator.setTraceName(trace, name);
    }

    public void setNavigatorXTitle(XAxisPosition xPosition, String title) {
        navigator.setXTitle(xPosition, title);
    }

    public void setNavigatorYTitle(int stack, YAxisPosition yPosition, String title) {
        navigator.setYTitle(stack, yPosition, title);
    }

    public void setNavigatorXScale(XAxisPosition xPosition, Scale scale) {
        navigator.setXScale(xPosition, scale);
    }

    public void setNavigatorYScale(int stack, YAxisPosition yPosition, Scale scale) {
        navigator.setYScale(stack, yPosition, scale);
    }

    public void setNavigatorXPrefixAndSuffix(XAxisPosition xPosition, @Nullable String prefix, @Nullable String suffix) {
        navigator.setXPrefixAndSuffix(xPosition, prefix, suffix);
    }

    public void setNavigatorYPrefixAndSuffix(int stack, YAxisPosition yPosition, @Nullable String prefix, @Nullable String suffix) {
        navigator.setYPrefixAndSuffix(stack, yPosition, prefix, suffix);
    }

    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        navigator.setYMinMax(stack, yPosition, min, max);
    }

    public void setNavigatorXMinMax(double min, double max) {
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            navigator.setXMinMax(xPosition, min, max);
        }
    }

    /**
     * Sets the visible amount of the scroll
     * (scroll  or corresponding chart X extent)
     */
    public void setScrollRange(XAxisPosition xPosition, Range range) {
        axisToScrolls.get(xPosition).setRange(range);
    }

    public void autoScaleNavigatorY(int stack, YAxisPosition yPosition) {
        navigator.autoScaleY(stack, yPosition);
    }

    public void autoScaleNavigatorY() {
        navigator.autoScaleY();
    }

    /**
     * =============================================================*
     * Protected method for careful use                            *
     * ==============================================================
     */

    boolean hoverOff() {
        if (chart.hoverOff()) {
            return true;
        }
        if (navigator.hoverOff()) {
            return true;
        }
        return false;
    }

    boolean hoverOn(int x, int y) {
        if (chart.hoverOn(x, y)) {
            return true;
        }
        if (navigator.hoverOn(x, y)) {
            return true;
        }

        return false;
    }

    boolean chartContain(int x, int y) {
        return chart.getBounds().contain(x, y);
    }

    boolean navigatorContain(int x, int y) {
        return navigator.getBounds().contain(x, y);
    }

    public boolean scrollContain(int x, int y) {
        if (!navigator.getBounds().contain(x, y)) {
            return false;
        }
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            if (axisToScrolls.get(xPosition).contain(x)) {
                return true;
            }
        }
        return false;
    }

    int getChartStack(BPoint point) {
        return chart.getStack(point);
    }

    int getNavigatorStack(BPoint point) {
        return navigator.getStack(point);
    }

    YAxisPosition getChartYAxis(int stack, BPoint point) {
        return chart.getYAxisPosition(stack, point);
    }

    YAxisPosition getNavigatorYAxis(int stack, BPoint point) {
        return navigator.getYAxisPosition(stack, point);
    }

    boolean setScrollsPosition(double x) {
        isChanged = false;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setPosition(x);
        }
        return isChanged;
    }

    boolean translateScrolls(double dx) {
        isChanged = false;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).translatePosition(dx);
        }
        return isChanged;
    }

    boolean translateScrollsViewport(double dx) {
        isChanged = false;
        double viewport_dx = dx;
        double scrolls_dx = dx;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            scrolls_dx = Math.min(viewport_dx * axisToScrolls.get(xPosition).getWidth() / width, viewport_dx);
        }
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).translatePosition(scrolls_dx);
        }
        return isChanged;
    }


    boolean zoomScrollExtent(XAxisPosition xAxisPosition, double zoomFactor) {
        isChanged = false;
        Scroll scroll = axisToScrolls.get(xAxisPosition);
        if (scroll != null) {
            scroll.zoomExtent(zoomFactor);
        }
        return isChanged;
    }

    boolean zoomScrollExtent(double zoomFactor) {
        isChanged = false;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).zoomExtent(zoomFactor);
        }
        return isChanged;
    }

    boolean autoScaleScrollExtent() {
        isChanged = false;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setExtent(chart.getBestExtent(xPosition));
        }
        return isChanged;
    }

    boolean autoScaleScrollExtent(XAxisPosition xPosition) {
        isChanged = false;
        axisToScrolls.get(xPosition).setExtent(chart.getBestExtent(xPosition));
        return isChanged;
    }

    XAxisPosition getChartSelectedTraceX() {
        return chart.getSelectedTraceX();
    }

    boolean selectTrace(int x, int y) {
        if (chart.selectTrace(x, y)) {
            return true;
        } else {
            return navigator.selectTrace(x, y);
        }
    }

    boolean zoomChartY(int stack, YAxisPosition yPosition, double zoomFactor) {
        return chart.zoomY(stack, yPosition, zoomFactor);
    }

    boolean translateChartY(int stack, YAxisPosition yPosition, int dy) {
        return chart.translateY(stack, yPosition, dy);
    }

    int getChartSelectedTraceStack() {
        return chart.getSelectedTraceStack();
    }

    YAxisPosition getChartSelectedTraceY() {
        return chart.getSelectedTraceY();
    }

    int getNavigatorSelectedTraceStack() {
        return navigator.getSelectedTraceStack();
    }

    YAxisPosition getNavigatorSelectedTraceY() {
        return navigator.getSelectedTraceY();
    }

    boolean isNavigatorTraceSelected() {
        return navigator.isTraceSelected();
    }

    public boolean isChartTraceSelected() {
        return chart.isTraceSelected();
    }

    boolean zoomNavigatorY(int stack, YAxisPosition yPosition, double zoomFactor) {
        return navigator.zoomY(stack, yPosition, zoomFactor);
    }

    boolean translateNavigatorY(int stack, YAxisPosition yPosition, int dy) {
        return navigator.translateY(stack, yPosition, dy);
    }
}
