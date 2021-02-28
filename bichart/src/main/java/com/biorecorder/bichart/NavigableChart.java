package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scroll.Scroll;
import com.biorecorder.bichart.scroll.ScrollListener;
import com.biorecorder.bichart.scroll.ScrollScale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;
import com.sun.istack.internal.Nullable;

import java.util.*;

public class NavigableChart {
    private int gap = 0; // between Chart and Preview px
    private Insets spacing = new Insets(5);
    private int navigatorHeightMin = 16; // px
    private Chart chart;
    private Chart navigator;
    private NavigableChartConfig config;
    private int width;
    private int height;
    private Map<XAxisPosition, Scroll> axisToScrolls = new HashMap<>(2);

    private boolean isValid = false;
    private boolean isChanged = false;


    public NavigableChart() {
        this(DarkTheme.getNavigableChartConfig());
    }

    public NavigableChart(NavigableChartConfig config) {
        this.config = new NavigableChartConfig(config);
        chart = new Chart(config.getChartConfig());
        navigator = new Chart(config.getNavigatorConfig());
        chart.setSpacing(new Insets(0));
        navigator.setSpacing(new Insets(0));
    }

    private void updateScrolls() {
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            Scroll scroll = axisToScrolls.get(xPosition);
            if (chart.isXAxisUsed(xPosition)) {
                if (scroll == null) {
                    //chart.autoScaleY();
                    //navigator.autoScaleY();
                    ScrollScale scrollScale = new ScrollScale() {
                        @Override
                        public double positionToValue(double x) {
                            return navigator.invert(XAxisPosition.BOTTOM, x);

                        }

                        @Override
                        public double valueToPosition(double value) {
                            return navigator.scale(XAxisPosition.BOTTOM, value);
                        }
                    };
                    Range navigatorRange = navigator.getAllTracesXMinMax();
                    if (navigatorRange == null) {
                        navigatorRange = chart.getAllTracesXMinMax();
                    }
                    scroll = new Scroll(new ScrollModelActive(chart, navigator, xPosition), scrollScale, config.getScrollConfig());
                    if (navigatorRange != null) {
                        scroll.setValues(navigatorRange.getMin(), chart.getBestExtent(xPosition), navigatorRange.getMin(), navigatorRange.getMax());
                    }
                    scroll.addListener(new ScrollListener() {
                        @Override
                        public void onScrollChanged() {
                            isChanged = true;
                        }
                    });
                    axisToScrolls.put(xPosition, scroll);
                }
            } else {
                if (scroll != null) {
                    axisToScrolls.remove(xPosition);
                }
            }
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
        //  выравниваем маргины
        chart.setMargin(null);
        navigator.setMargin(null);
        Insets chartMargin = chart.getMargin(renderContext);
        Insets navigatorMargin = navigator.getMargin(renderContext);
        if (chartMargin.left() != navigatorMargin.left() ||
                chartMargin.right() != navigatorMargin.right()) {
            int leftMargin = Math.max(chartMargin.left(), navigatorMargin.left());
            int rightMargin = Math.max(chartMargin.right(), navigatorMargin.right());
            chart.setMargin(new Insets(chartMargin.top(), rightMargin, chartMargin.bottom(), leftMargin));
            navigator.setMargin(new Insets(navigatorMargin.top(), rightMargin, navigatorMargin.bottom(), leftMargin));
        }
        updateScrolls();
    }


    double navigatorScale(double value) {
        return navigator.scale(XAxisPosition.BOTTOM, value);
    }

    double navigatorInvert(double value) {
        return navigator.invert(XAxisPosition.BOTTOM, value);
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
        return chart.contain(x, y);
    }

    public boolean scrollContain(int x, int y) {
        if (!navigator.contain(x, y)) {
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

    YAxisPosition getChartYAxis(int stack, BPoint point) {
        return chart.getYAxisPosition(stack, point);
    }

    int getNavigatorStack(BPoint point) {
        return chart.getStack(point);
    }


    YAxisPosition getNavigatorYAxis(int stack, BPoint point) {
        return navigator.getYAxisPosition(stack, point);
    }


    /**
     * ==================================================*
     * Base methods to interact           *
     * ==================================================
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


    public boolean setScrollsPosition(double x) {
        isChanged = false;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setPosition(x);
        }
        return isChanged;
    }

    public boolean translateScrolls(double dx) {
        isChanged = false;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).translatePosition(dx);
        }
        return isChanged;
    }

    public boolean zoomScrollExtent(XAxisPosition xAxisPosition, double zoomFactor) {
        isChanged = false;
        Scroll scroll = axisToScrolls.get(xAxisPosition);
        if (scroll != null) {
            scroll.zoomExtent(zoomFactor);
        }
        return isChanged;
    }

    public double getScrollWidth(XAxisPosition xAxisPosition) throws IllegalArgumentException {
        Scroll scroll = axisToScrolls.get(xAxisPosition);
        if (scroll != null) {
            return scroll.getWidth();
        }
        return 0;
    }

    public void autoScaleScrollExtent() {
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setExtent(chart.getBestExtent(xPosition));
        }
    }

    public void autoScaleSelectedTraceXScrollExtent() {
        XAxisPosition xPosition = chart.getSelectedTraceX();
        axisToScrolls.get(xPosition).setExtent(chart.getSelectedTraceXBestExtent());

    }

    public XAxisPosition getChartSelectedTraceX() {
        return chart.getSelectedTraceX();
    }

    public int getWidth() {
        return width;
    }

    public boolean selectTrace(int x, int y) {
        if (chart.selectTrace(x, y)) {
            return true;
        } else {
            return navigator.selectTrace(x, y);
        }
    }

    public void setTitle(String title) {
        chart.setTitle(title);
    }


    /**
     * =======================Base methods to interact with chart ==========================
     **/
    public int chartStackCount() {
        return chart.stackCount();
    }

    public boolean isChartTraceSelected() {
        return chart.isTraceSelected();
    }

    public void zoomChartY(int stack, YAxisPosition yPosition, double zoomFactor) {
        chart.zoomY(stack, yPosition, zoomFactor);
    }

    public void zoomChartSelectedTraceY(double zoomFactor) {
        chart.zoomSelectedTraceY(zoomFactor);
    }

    public void zoomChartX(XAxisPosition xPosition, double zoomFactor) {
        chart.zoomX(xPosition, zoomFactor);
    }

    public void zoomChartSelectedTraceX(double zoomFactor) {
        chart.zoomSelectedTraceX(zoomFactor);
    }

    public void translateChartY(int stack, YAxisPosition yPosition, int dy) {
        chart.translateY(stack, yPosition, dy);
    }

    public void translateChartSelectedTraceY(int dy) {
        chart.translateSelectedTraceY(dy);
    }

    public void translateChartX(XAxisPosition xPosition, int dy) {
        chart.translateX(xPosition, dy);
    }

    public void translateChartSelectedTraceX(int dy) {
        chart.translateSelectedTraceX(dy);
    }

    public void autoScaleChartY(int stack, YAxisPosition yPosition) {
        chart.autoScaleY(stack, yPosition);
    }

    public void autoScaleChartY() {
        chart.autoScaleY();
    }

    public void autoScaleChartSelectedTraceY() {
        chart.autoScaleSelectedTraceY();
    }

    public void autoScaleChartSelectedTraceX() {
        chart.autoScaleSelectedTraceX();
    }

    public void addChartStack(int weight) {
        chart.addStack(weight);
        invalidate();
    }

    public void addChartStack() {
        chart.addStack();
        invalidate();
    }

    public void setChartStackWeight(int stack, int weight) {
        chart.setStackWeight(stack, weight);
        invalidate();
    }

    /**
     * @throws IllegalStateException if stack axis are used by some trace traces and
     *                               therefor can not be deleted
     */
    public void removeChartStack(int stackNumber) throws IllegalStateException {
        chart.removeStack(stackNumber);
        invalidate();
    }

    public void addChartTrace(ChartData data, TracePainter tracePainter) {
        chart.addTrace(data, tracePainter);
    }


    public void addChartTrace(ChartData data, TracePainter tracePainter, XAxisPosition xPosition, YAxisPosition yPosition) {
        chart.addTrace(data, tracePainter, xPosition, yPosition);
    }

    public void addChartTrace(ChartData data, TracePainter tracePainter, int stackNumber) {
        chart.addTrace(data, tracePainter, stackNumber);
    }

    public void addChartTrace(ChartData data, TracePainter tracePainter, int stackNumber, XAxisPosition xPosition, YAxisPosition yPosition) {
        chart.addTrace(data, tracePainter, stackNumber, xPosition, yPosition);
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

    /**
     * =======================Base methods to interact with navigator==========================
     **/
    public boolean isNavigatorTraceSelected() {
        return navigator.isTraceSelected();
    }

    public boolean navigatorContain(int x, int y) {
        return navigator.contain(x, y);
    }

    public void zoomNavigatorY(int stack, YAxisPosition yPosition, double zoomFactor) {
        navigator.zoomY(stack, yPosition, zoomFactor);
    }

    public void zoomNavigatorSelectedTraceY(double zoomFactor) {
        navigator.zoomSelectedTraceY(zoomFactor);
    }

    public void translateNavigatorY(int stack, YAxisPosition yPosition, int dy) {
        navigator.translateY(stack, yPosition, dy);
    }

    public void translateNavigatorSelectedTraceY(int dy) {
        navigator.translateSelectedTraceY(dy);
    }


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

    public void setNavigatorStackWeight(int stack, int weight) {
        navigator.setStackWeight(stack, weight);
        invalidate();
    }

    /**
     * @throws IllegalStateException if stack axis are used by some trace traces and
     *                               therefor can not be deleted
     */
    public void removeNavigatorStack(int stack) throws IllegalStateException {
        navigator.removeStack(stack);
        invalidate();
    }

    public void addNavigatorTrace(ChartData data, TracePainter tracePainter) {
        navigator.addTrace(data, tracePainter);
    }

    public void addNavigatorTrace(ChartData data, TracePainter tracePainter, int stack) {
        navigator.addTrace(data, tracePainter, stack);
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

    public void autoScaleNavigatorY(int stack, YAxisPosition yPosition) {
        navigator.autoScaleY(stack, yPosition);
    }

    public void autoScaleNavigatorSelectedTraceY() {
        navigator.autoScaleSelectedTraceY();
    }

    public void autoScaleNavigatorY() {
        navigator.autoScaleY();
    }

}
