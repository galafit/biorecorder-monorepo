package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.AxisConfig;
import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.dataprocessing.DataProcessingConfig;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;
import com.sun.istack.internal.Nullable;

import java.util.*;


/**
 * Created by galafit on 3/10/17.
 */
public class NavigableChart {
    private Chart chart;
    private Chart navigator;
    private NavigableChartConfig config;

    private int navigatorMaxZoomFactor = -1;
    private boolean isAutoScrollEnabled = true;

    private int width;
    private int height;
    private BRectangle chartArea;
    private BRectangle navigatorArea;
    private Map<XAxisPosition, Scroll> scrolls = new Hashtable<XAxisPosition, Scroll>(2);
    private List<XAxisPosition> scrollsToAutoscale = new ArrayList<>(2);

    private boolean isScrollsAtTheEnd = true;
    private boolean isScrollsDirty = true;
    private boolean isAreasDirty = true;


    public NavigableChart() {
        this(new DataProcessingConfig());
    }

    public NavigableChart(NavigableChartConfig config) {
        this(config, new DataProcessingConfig());
    }

    public NavigableChart(DataProcessingConfig dataProcessingConfig) {
        this(dataProcessingConfig, dataProcessingConfig);
    }

    public NavigableChart(NavigableChartConfig config, DataProcessingConfig dataProcessingConfig) {
        this(config, dataProcessingConfig, dataProcessingConfig);
    }

    public NavigableChart(DataProcessingConfig chartDataProcessingConfig, DataProcessingConfig navigatorDataProcessingConfig) {
        this(DarkTheme.getNavigableChartConfig(), chartDataProcessingConfig, navigatorDataProcessingConfig);
    }

    public NavigableChart(NavigableChartConfig config, DataProcessingConfig chartDataProcessingConfig, DataProcessingConfig navigatorDataProcessingConfig) {
        this.config = new NavigableChartConfig(config);
        chart = new Chart(config.getChartConfig());
        DataProcessingConfig copyProcessingConfig = new DataProcessingConfig(navigatorDataProcessingConfig);
        copyProcessingConfig.setGroupAll(true);
        navigator = new Chart(config.getNavigatorConfig());
    }

    private void autoScaleChartY() {
        for (int i = 0; i < chart.stackCount(); i++) {
            chart.autoScaleY(i, YAxisPosition.LEFT);
            chart.autoScaleY(i, YAxisPosition.RIGHT);
        }
    }

    private void autoScaleNavigatorY() {
        for (int i = 0; i < navigator.stackCount(); i++) {
            navigator.autoScaleY(i, YAxisPosition.LEFT);
            navigator.autoScaleY(i, YAxisPosition.RIGHT);
        }
    }

    // navigator have all X axes synchronized (the same min and max)
    private void updateScrollsAndPreview(BCanvas canvas) {
        Range chartDataMinMax = chart.getAllTracesXMinMax();
        if (chartDataMinMax == null) {
            return;
        }
        double navigatorBestExtent = navigator.getBestExtent(XAxisPosition.BOTTOM, canvas.getRenderContext());
        Range navigatorRange = chartDataMinMax;
        if (navigatorBestExtent > chartDataMinMax.length()) {
            navigatorRange = new Range(chartDataMinMax.getMin(), chartDataMinMax.getMin() + navigatorBestExtent);
        }
        XAxisPosition[] xAxisPositions = navigator.getXAxes();
        for (int i = 0; i < xAxisPositions.length; i++) {
            navigator.setXMinMax(xAxisPositions[i], navigatorRange.getMin(), navigatorRange.getMax());
        }

        // create, remove and update scrolls
        xAxisPositions = chart.getXAxes();

        for (int i = 0; i < xAxisPositions.length; i++) {
            Range scrollRange = chartDataMinMax;
            XAxisPosition xAxisPosition = xAxisPositions[i];
            double extent = chart.getBestExtent(xAxisPosition, canvas.getRenderContext());
            if (extent > scrollRange.length()) {
                scrollRange = new Range(scrollRange.getMin(), scrollRange.getMin() + extent);
            }
            // create scrolls
            if (scrolls.get(xAxisPosition) == null) {
                if (extent > 0) {
                    Scroll scroll = new Scroll(scrollRange.getMin(), scrollRange.getMax(), extent);
                    chart.setXMinMax(xAxisPosition, scrollRange.getMin(), scrollRange.getMin() + extent);
                    for (int stack = 0; stack < chart.stackCount(); stack++) {
                        YAxisPosition[] yAxisPositions = chart.getYAxes(stack);
                        for (int j = 0; j < yAxisPositions.length; j++) {
                            chart.autoScaleY(stack, yAxisPositions[j]);
                        }

                    }
                    scrolls.put(xAxisPosition, scroll);
                    scroll.addListener(new ScrollListener() {
                        @Override
                        public void onScrollChanged(double scrollValue, double scrollExtent) {
                            Range xRange = new Range(scrollValue, scrollValue + scrollExtent);
                            chart.setXMinMax(xAxisPosition, xRange.getMin(), xRange.getMax());
                            isScrollsAtTheEnd = isScrollAtTheEnd(xAxisPosition);
                        }
                    });
                    if (isAutoScrollEnabled) {
                        scrollToEnd();
                    }
                }
            }

            // update scrolls
            scrolls.get(xAxisPosition).setMinMax(scrollRange.getMin(), scrollRange.getMax());

        }
        // remove unused scrolls
        if(scrolls.keySet().size() > xAxisPositions.length) {
            List<XAxisPosition> scrollKeyToRemove = new ArrayList<>();
            for (XAxisPosition scrollKey : scrolls.keySet()) {
                boolean needDelete = true;
                for (XAxisPosition axisPosition : xAxisPositions) {
                    if(axisPosition == scrollKey) {
                        needDelete = false;
                        break;
                    }
                }
                if(needDelete) {
                    scrollKeyToRemove.add(scrollKey);
                }
            }
            for (XAxisPosition xAxisPosition : scrollKeyToRemove) {
                scrolls.remove(xAxisPosition);
            }
        }
    }


    private boolean scrollToEnd() {
        boolean isMoved = false;
        for (XAxisPosition key : scrolls.keySet()) {
            Scroll scroll = scrolls.get(key);
            if (scroll.setValue(scroll.getMax() - scroll.getExtent())) {
                isMoved = true;
            }
        }
        return isMoved;
    }


    private boolean isScrollAtTheEnd(XAxisPosition xAxisPosition) {
        int gap = 5;
        Scroll scroll = scrolls.get(xAxisPosition);
        double max = scroll.getMax();
        double scrollEnd = scroll.getValue() + scroll.getExtent();
        int max_position = (int) navigatorScale(max);
        int scrollEndPosition = (int) navigatorScale(scrollEnd);
        int distance = max_position - scrollEndPosition;
        if (distance > gap) {
            return false;
        } else {
            return true;
        }
    }

    private Range getScrollTouchRange(Scroll scroll) {
        int scrollStart = (int) navigatorScale(scroll.getValue());
        int scrollEnd = (int) navigatorScale(scroll.getValue() + scroll.getExtent());
        int scrollWidth = scrollEnd - scrollStart;

        int touchRadius = config.getScrollConfig().getTouchRadius();
        if (scrollWidth < 2 * touchRadius) {
            int delta = touchRadius - scrollWidth / 2;
            int touchStart = scrollStart - delta;
            int touchEnd = scrollEnd + delta;
            int touchWidth = touchEnd - touchStart;

            int scrollAreaStart = (int) navigatorScale(scroll.getMin());
            int scrollAreaEnd = (int) navigatorScale(scroll.getMax());
            touchWidth = Math.min(touchWidth, scrollAreaEnd - scrollAreaStart);

            if (touchEnd > scrollAreaEnd) {
                touchEnd = scrollAreaEnd;
                touchStart = touchEnd - touchWidth;
            }
            if (touchStart < scrollAreaStart) {
                touchStart = scrollAreaStart;
                touchEnd = touchStart + touchWidth;
            }

            return new Range(touchStart, touchEnd);
        }

        return new Range(scrollStart, scrollEnd);
    }


    private void calculateAndSetAreas() {
        int top = config.getSpacing().top();
        int bottom = config.getSpacing().bottom();
        int left = config.getSpacing().left();
        int right = config.getSpacing().right();
        int gap = config.getGap();

        int width1 = width - left - right;
        int height1 = height - top - bottom;
        if (height1 > gap) {
            height1 -= gap;
        }

        int navigatorHeight;
        if (navigator.traceCount() == 0) {
            navigatorHeight = Math.min(config.getNavigatorHeightMin(), height1 / 2);
        } else {
            int chartWeight = chart.getStacksSumWeight();
            int navigatorWeight = navigator.getStacksSumWeight();
            navigatorHeight = height1 * navigatorWeight / (chartWeight + navigatorWeight);
        }

        int chartHeight = height1 - navigatorHeight;

        chartArea = new BRectangle(left, top, width1, chartHeight);
        navigatorArea = new BRectangle(left, height - navigatorHeight, width1, navigatorHeight);
        chart.setSize(chartArea.width, chartArea.height);
        navigator.setSize(navigatorArea.width, navigatorArea.height);
    }


    double navigatorScale(double value) {
        return navigator.scale(XAxisPosition.BOTTOM, value);
    }

    double navigatorInvert(double value) {
        return navigator.invert(XAxisPosition.BOTTOM, value);
    }

    private void drawScroll(BCanvas canvas, Scroll scroll) {
        //BRectangle area = navigator.getGraphArea(canvas.getRenderContext());
        BRectangle area = navigatorArea;
        ScrollConfig scrollConfig = config.getScrollConfig();

        int borderWidth = scrollConfig.getBorderWidth();

        int scrollStart = (int) navigatorScale(scroll.getValue());
        int scrollEnd = (int) navigatorScale(scroll.getValue() + scroll.getExtent());
        int scrollY = area.y + borderWidth / 2;
        int scrollHeight = area.height - (borderWidth / 2) * 2;
        int scrollWidth = Math.max(1, scrollEnd - scrollStart);

        Range touchRange = getScrollTouchRange(scroll);
        int touchStart = (int) touchRange.getMin();
        int touchWidth = (int) touchRange.length();
        if (touchStart != scrollStart || touchWidth != scrollWidth) {
            canvas.setColor(scrollConfig.getFillColor());
            canvas.fillRect(touchStart, scrollY, touchWidth, scrollHeight);
        } else {
            canvas.setColor(scrollConfig.getFillColor());
            canvas.fillRect(scrollStart, scrollY, scrollWidth, scrollHeight);
        }

        canvas.setColor(scrollConfig.getColor());
        canvas.setStroke(borderWidth, DashStyle.SOLID);
        canvas.drawRect(scrollStart, scrollY, scrollWidth, scrollHeight);

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

    boolean isChartContains(BPoint point) {
        if (point != null && chartArea.contains(point.getX(), point.getY())) {
            return true;
        }
        return false;
    }

    boolean isScrollContain(int x, int y) {
        if (!navigatorArea.contains(x, y)) {
            return false;
        }

        for (XAxisPosition key : scrolls.keySet()) {
            if (getScrollTouchRange(scrolls.get(key)).contains(x)) {
                return true;
            }
        }
        return false;
    }

    XAxisPosition[] getChartXAxes() {
        return chart.getXAxes();
    }

    YAxisPosition[] getChartYAxes(int stack) {
        return chart.getYAxes(stack);
    }

    int getChartStack(BPoint point) {
        return chart.getStack(point);
    }


    YAxisPosition getChartYAxis(int stack, BPoint point) {
        return chart.getYAxis(stack, point);
    }


    int getNavigatorStack(BPoint point) {
        return chart.getStack(point);
    }


    YAxisPosition getNavigatorYAxis(int stack, BPoint point) {
        return navigator.getYAxis(stack, point);
    }

    public boolean isNavigatorTraceSelected() {
        return navigator.isTraceSelected();
    }

    XAxisPosition getNavigatorSelectedTraceX() {
        return navigator.getSelectedTraceX();
    }

    int getNavigatorSelectedTraceStack() {
        return navigator.getSelectedTraceStack();
    }

    YAxisPosition getNavigatorSelectedTraceY() {
        return navigator.getSelectedTraceY();
    }

    public boolean isChartTraceSelected() {
        return chart.isTraceSelected();
    }

    XAxisPosition getChartSelectedTraceX() {
        return chart.getSelectedTraceX();
    }

    int getChartSelectedTraceStack() {
        return chart.getSelectedTraceStack();
    }

    YAxisPosition getChartSelectedTraceY() {
        return chart.getSelectedTraceY();
    }

    XAxisPosition[] getNavigatorXAxes() {
        return navigator.getXAxes();
    }

    YAxisPosition[] getNavigatorYAxes(int stack) {
        return navigator.getYAxes(stack);
    }


    /**==================================================*
     *                Base methods to interact           *
     * ==================================================*/

    public void draw(BCanvas canvas) {
        if (isAreasDirty) {
            int dx = 0;
            if (chartArea != null) {
                dx = width - chartArea.width;
            }
            if (dx != 0) {
                for (XAxisPosition key : scrolls.keySet()) {
                    Scroll scroll = scrolls.get(key);
                    double scrollExtentNew = chart.invert(key, width) - chart.invert(key, 0);
                    scroll.setExtent(scrollExtentNew);
                }
            }
            calculateAndSetAreas();
            isAreasDirty = false;
        }
        if (isScrollsDirty) {
            updateScrollsAndPreview(canvas);
            if (isScrollsAtTheEnd) {
                scrollToEnd();
            }
            isScrollsDirty = false;
        }

        for (int i = 0; i < scrollsToAutoscale.size(); i++) {
            XAxisPosition xAxisPosition = scrollsToAutoscale.get(i);
            Scroll scroll = scrolls.get(xAxisPosition);
            if (scroll != null) {
                scroll.setExtent(chart.getBestExtent(xAxisPosition, canvas.getRenderContext()));
            }
        }
        scrollsToAutoscale.clear();

        canvas.setColor(config.getBackgroundColor());
        canvas.fillRect(0, 0, width, height);
        chart.draw(canvas);
        canvas.save();
        canvas.translate(navigatorArea.x, navigatorArea.y);
        navigator.draw(canvas);
        canvas.restore();
        for (XAxisPosition key : scrolls.keySet()) {
            drawScroll(canvas, scrolls.get(key));
        }
    }

    /**
     * default -1 (no zoom limits)
     */
    public void setNavigatorMaxZoomFactor(int navigatorMaxZoomFactor) {
        this.navigatorMaxZoomFactor = navigatorMaxZoomFactor;
        scrolls.clear();
        isScrollsDirty = true;
    }

    public void setAutoScrollEnabled(boolean autoScrollEnabled) {
        isAutoScrollEnabled = autoScrollEnabled;
        scrolls.clear();
        isScrollsDirty = true;
    }


    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        isAreasDirty = true;
        isScrollsDirty = true;
    }

    public void setConfig(NavigableChartConfig config1) {
        this.config = new NavigableChartConfig(config1);
        chart.setConfig(config.getChartConfig());
        navigator.setConfig(config.getNavigatorConfig());
        scrolls.clear();
        isAreasDirty = true;
        isScrollsDirty = true;
    }

    public void zoomNavigatorY(int stack, YAxisPosition yPosition, double zoomFactor) {
        navigator.zoomY(stack, yPosition, zoomFactor);
    }

    public void translateNavigatorY(int stack, YAxisPosition yPosition, int dy) {
        navigator.translateY(stack, yPosition, dy);

    }

    /**
     * @return true if scrollValue was changed and false if newValue = current scroll value
     */
    public boolean setScrollsValue(double newValue) {
        boolean scrollsMoved = false;
        for (XAxisPosition key : scrolls.keySet()) {
            scrollsMoved = scrolls.get(key).setValue(newValue) || scrollsMoved;
        }
        return scrollsMoved;
    }


    /**
     * @return true if scrollValue was changed and false if newValue = current scroll value
     */
    public boolean setScrollsPosition(double x, double y) {
        if (navigatorArea == null) {
            return false;
        }
        boolean scrollsMoved = false;
        for (XAxisPosition key : scrolls.keySet()) {
            double value = navigatorInvert(x);
            scrollsMoved = scrolls.get(key).setValue(value) || scrollsMoved;
        }
        return scrollsMoved;
    }

    public boolean translateScrolls(double dx) {
        Double maxScrollsPosition = null;
        for (XAxisPosition key : scrolls.keySet()) {
            double scrollPosition = navigatorScale(scrolls.get(key).getValue());
            maxScrollsPosition = (maxScrollsPosition == null) ? scrollPosition : Math.max(maxScrollsPosition, scrollPosition);
        }
        if (maxScrollsPosition != null) {
            return setScrollsPosition(maxScrollsPosition + dx, navigatorArea.y);
        }
        return false;
    }


    public boolean zoomScrollExtent(XAxisPosition xAxisPosition, double zoomFactor) {
        Scroll scroll = scrolls.get(xAxisPosition);
        if (scroll != null) {
            double bestExtent = chart.getTracesBestExtent(xAxisPosition);
            double newExtent = scroll.getExtent() * zoomFactor;
            if (navigatorMaxZoomFactor <= 0 || newExtent / bestExtent < navigatorMaxZoomFactor) {
                scroll.setExtent(newExtent);
                return true;
            }
        }
        return false;
    }

    public int getWidth() {
        return width;
    }

    public double getScrollWidth(XAxisPosition xAxisPosition) throws IllegalArgumentException {
        Scroll scroll = scrolls.get(xAxisPosition);
        if (scroll != null) {
            double scrollStart = navigatorScale(scroll.getValue());
            double scrollEnd = navigatorScale(scroll.getValue() + scroll.getExtent());
            return scrollEnd - scrollStart;
        }
        String errMsg = "No scroll associated with x axis: " + xAxisPosition;
        throw new IllegalArgumentException(errMsg);
    }

    public void setScrollExtent(XAxisPosition xAxisPosition, double extent) {
        Scroll scroll = scrolls.get(xAxisPosition);
        if (scroll != null) {
            scroll.setExtent(extent);
        }
    }

    public void autoScaleScrollExtent(XAxisPosition xAxisPosition) {
        scrollsToAutoscale.add(xAxisPosition);
    }

    public boolean hasScroll(XAxisPosition xAxisPosition) {
        return scrolls.get(xAxisPosition) != null;
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

    public int navigatorStackCount() {
        return navigator.stackCount();
    }


    public void autoScaleChartY(int stack, YAxisPosition yPosition) {
        chart.autoScaleY(stack, yPosition);
    }

    public void addChartStack(int weight) {
        chart.addStack(weight);
        isAreasDirty = true;
    }

    public void addChartStack() {
        chart.addStack();
        isAreasDirty = true;
    }

    public void setChartStackWeigt(int stack, int weight) {
        chart.setStackWeight(stack, weight);
        isAreasDirty = true;
    }

    /**
     * @throws IllegalStateException if stack axis are used by some trace traces and
     *                               therefor can not be deleted
     */
    public void removeChartStack(int stackNumber) throws IllegalStateException {
        chart.removeStack(stackNumber);
        isAreasDirty = true;
    }

    public void addChartTrace(ChartData data, TracePainter tracePainter) {
        chart.addTrace(data, tracePainter);
        isAreasDirty = true;
        isScrollsDirty = true;
    }


    public void addChartTrace(ChartData data, TracePainter tracePainter, XAxisPosition xPosition, YAxisPosition yPosition) {
        chart.addTrace(data,  tracePainter, xPosition, yPosition);
        isAreasDirty = true;
        isScrollsDirty = true;
    }

    public void addChartTrace(ChartData data, TracePainter tracePainter, int stackNumber) {
        chart.addTrace(data,  tracePainter, stackNumber);
        isAreasDirty = true;
        isScrollsDirty = true;
    }

    public void addChartTrace(ChartData data, TracePainter tracePainter,  int stackNumber, XAxisPosition xPosition, YAxisPosition yPosition) {
        chart.addTrace(data,  tracePainter, stackNumber, xPosition, yPosition);
        isAreasDirty = true;
        isScrollsDirty = true;
    }

    public void removeChartTrace(int traceNumber) {
        chart.removeTrace(traceNumber);
        isAreasDirty = true;
        isScrollsDirty = true;
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
        scrolls.clear();
        isScrollsDirty = true;
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

    public void zoomChartY(int stack, YAxisPosition yPosition, double zoomFactor) {
        chart.zoomY(stack, yPosition, zoomFactor);
    }

    public void translateChartY(int stack, YAxisPosition yPosition, int dy) {
        chart.translateY(stack, yPosition, dy);
    }


    /**
     * =======================Base methods to interact with navigator==========================
     **/

    public boolean isNavigatorContains(BPoint point) {
        if (point != null && navigatorArea.contains(point.getX(), point.getY())) {
            return true;
        }
        return false;
    }

    public void addNavigatorStack() {
        navigator.addStack();
        isAreasDirty = true;
    }

    public void addNavigatorStack(int weight) {
        navigator.addStack(weight);
        isAreasDirty = true;
    }

    public void setNavigatorStackWeigt(int stack, int weight) {
        navigator.setStackWeight(stack, weight);
        isAreasDirty = true;
    }

    /**
     * @throws IllegalStateException if stack axis are used by some trace traces and
     *                               therefor can not be deleted
     */
    public void removeNavigatorStack(int stack) throws IllegalStateException {
        navigator.removeStack(stack);
        isAreasDirty = true;
    }

    public void addNavigatorTrace(ChartData data, TracePainter tracePainter) {
        navigator.addTrace(data,  tracePainter);
        isAreasDirty = true;
        isScrollsDirty = true;
    }


    public void addNavigatorTrace(ChartData data, TracePainter tracePainter, int stack) {
        navigator.addTrace(data,  tracePainter, stack);
        isAreasDirty = true;
        isScrollsDirty = true;
    }

    public void removeNavigatorTrace(int traceNumber) {
        navigator.removeTrace(traceNumber);
        isAreasDirty = true;
        isScrollsDirty = true;
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
        scrolls.clear();
        isScrollsDirty = true;
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

    public AxisConfig getNavigatorXConfig(XAxisPosition xPosition) {
        return navigator.getXConfig(xPosition);
    }

    public AxisConfig getNavigatorYConfig(int stack, YAxisPosition yPosition) {
        return navigator.getYConfig(stack, yPosition);
    }


    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        navigator.setYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleNavigatorY(int stack, YAxisPosition yPosition) {
        navigator.autoScaleY(stack, yPosition);
    }

}
