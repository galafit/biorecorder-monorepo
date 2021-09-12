package com.biorecorder.bichart.chart;

import com.biorecorder.bichart.configs.NavigableChartConfig;
import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scroll.Scroll;
import com.biorecorder.bichart.scroll.ScrollListener;
import com.biorecorder.bichart.traces.TracePainter;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigableChart {
    private static double DEFAULT_CHART_VIEWPORT_EXTENT_RATIO = 1.0/10;
    private int gap = 0; // between Chart and Preview px
    private Insets spacing = new Insets(0);
    private int navigatorHeightMin = 16; // px
    private XAxisPosition navigatorXPosition = XAxisPosition.BOTTOM;
    private Chart chart;
    private Chart navigator;
    private NavigableChartConfig config;
    private int width = 250;
    private int height = 100;
    private List<ChartData> chartDataList = new ArrayList<>();
    private List<ChartData> navigatorDataList = new ArrayList<>();
    private Map<XAxisPosition, Scroll> axisToScrolls = new HashMap<>(2);
    private Map<XAxisPosition, List<ScrollListener>> axisToScrollListeners = new HashMap<>(2);

    private boolean isValid = false;
    private boolean isChanged = false;
    private boolean isConfigured = false;

    public NavigableChart(NavigableChartConfig config, Scale xScale) {
        this.config = new NavigableChartConfig(config);
        chart = new Chart(config.getChartConfig(), xScale);
        navigator = new Chart(config.getNavigatorConfig(), xScale);
        chart.setDefaultXPosition(XAxisPosition.TOP);
        navigator.setDefaultXPosition(XAxisPosition.BOTTOM);
        chart.setSpacing(new Insets(0));
        navigator.setSpacing(new Insets(0));
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            axisToScrollListeners.put(xPosition, new ArrayList<>());
        }
    }

    public List<Integer> getChartTraces(XAxisPosition xAxisPosition) {
        return chart.getTraces(xAxisPosition);
    }

    public void addScrollListener(XAxisPosition xAxisPosition, ScrollListener scrollListener) {
        axisToScrollListeners.get(xAxisPosition).add(scrollListener);
    }

    public int getWidth() {
        return width;
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

    private Range dataMinMax(ChartData data) {
        if(data.rowCount() > 0) {
            return new Range(data.value(0, 0), data.value(data.rowCount() - 1, 0));
        }
        return null;
    }

    private Range dataBestRange(ChartData data, int markSize, Scale xScale, double xLength, double min) {
        int dataSize = data.rowCount();
        Range dataRange = dataMinMax(data);
        if (xLength > 0 && dataSize > 1 && dataRange != null && dataRange.length() > 0) {
            xScale.setStartEnd(1, dataSize * markSize);
            xScale.setMinMax(dataRange.getMin(), dataRange.getMax());
            double positionOfMin =  xScale.scale(min);
            double positionOfMax = positionOfMin + xLength;
            double max = xScale.invert(positionOfMax);
            return new Range(min, max);
        }
        return null;
    }

    private void autoScaleNavigatorX() {
        Scale xScale = navigator.getXScale(navigatorXPosition);
        double xLength =  xScale.getLength();
        Range xMinMax = null;
        if(isConfigured) {
            // need it in the case of chart data cropping after configuring
            xMinMax = navigator.getXMinMax(navigatorXPosition);
        }
        for (int i = 0; i < navigatorDataList.size(); i++) {
            ChartData data = navigatorDataList.get(i);
            xMinMax = Range.join(xMinMax, dataMinMax(data));
        }
        for (int i = 0; i < chartDataList.size(); i++) {
            ChartData data = chartDataList.get(i);
            xMinMax = Range.join(xMinMax, dataMinMax(data));
        }
        if (xMinMax != null) {
            Range minBestRange = null;
           /* for (int i = 0; i < navigatorDataList.size(); i++) {
                Range dataBestRange = dataBestRange(navigatorDataList.get(i), navigator.getTraceMarkSize(i), xScale, xLength, xMinMax.getMin());
                if (dataBestRange != null) {
                    if(minBestRange == null || minBestRange.length() > dataBestRange.length()) {
                        minBestRange = dataBestRange;
                    }
                }
            }
            if (minBestRange != null && minBestRange.getMax() > xMinMax.getMax()) {
                xMinMax = new Range(xMinMax.getMin(), minBestRange.getMax());
            }*/
            for (XAxisPosition xPosition : XAxisPosition.values()) {
                minBestRange = null;
                List<Integer> traceNumbers = chart.getTraces(xPosition);
                for (int i = 0; i < traceNumbers.size(); i++) {
                    int traceNumber = traceNumbers.get(i);
                    Range dataBestRange = dataBestRange(chartDataList.get(traceNumber), chart.getTraceMarkSize(traceNumber), xScale, xLength, xMinMax.getMin());
                    if (dataBestRange != null) {
                        if(minBestRange == null || minBestRange.length() > dataBestRange.length()) {
                            minBestRange = dataBestRange;
                        }
                    }
                }
                if (minBestRange != null && minBestRange.getMax() > xMinMax.getMax()) {
                    xMinMax = new Range(xMinMax.getMin(), minBestRange.getMax());
                }
            }
        }
        if (xMinMax != null) {
            setNavigatorXMinMax(xMinMax.getMin(), xMinMax.getMax());
        }
    }

    private void autoScaleChartX(XAxisPosition xPosition) {
        Scale xScale = chart.getXScale(xPosition);
        double chartXMin = xScale.getMin();
        double xLength =  xScale.getLength();
        List<Integer> traceNumbers = chart.getTraces(xPosition);
        Range minBestRange = null;
        for (int i = 0; i < traceNumbers.size(); i++) {
            int traceNumber = traceNumbers.get(i);
            Range dataBestRange = dataBestRange(chartDataList.get(traceNumber), chart.getTraceMarkSize(traceNumber), xScale, xLength, chartXMin);
            if (dataBestRange != null) {
                if(minBestRange == null || minBestRange.length() > dataBestRange.length()) {
                    minBestRange = dataBestRange;
                }
            }
        }
        Range xMinMax = null;
        if (minBestRange != null ) {
            xMinMax = minBestRange;
        } else{
            // set chart default viewport extent (1/10 of navigator range)
            // we need it in the case if chart has no traces or traces have no data
            xMinMax = navigator.getXMinMax(navigatorXPosition);
            xMinMax = new Range(xMinMax.getMin(), xMinMax.getMin() + xMinMax.length() * DEFAULT_CHART_VIEWPORT_EXTENT_RATIO);
        }
        axisToScrolls.remove(xPosition);
        chart.setXMinMax(xPosition, xMinMax.getMin(), xMinMax.getMax());
        if(chart.getTraces(xPosition).size() > 0) {
            Scroll scroll = axisToScrolls.get(xPosition);
            if(scroll == null) {
                scroll = createScroll(xPosition);
                axisToScrolls.put(xPosition, scroll);
                if(!isConfigured) {
                    scroll.setViewportMin(navigator.getXMinMax(navigatorXPosition).getMin());
                }
            }
        }

    }
    private void autoScaleChartX() {
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            autoScaleChartX(xPosition);
        }
    }

    public void autoScaleX(XAxisPosition xPosition) {
        autoScaleNavigatorX();
        autoScaleChartX(xPosition);
    }


    public void autoScaleX() {
        autoScaleNavigatorX();
        autoScaleChartX();
        isConfigured = true;
    }

    private Scroll createScroll(XAxisPosition xAxisPosition) {
        Range navigatorRange = navigator.getXMinMax(navigatorXPosition);
        Range chartRange = chart.getXRange(xAxisPosition);
        Scale chartScale = chart.getXScale(xAxisPosition);
        Scroll scroll = new Scroll(config.getScrollConfig(), chartScale);
        scroll.setMinMax(navigatorRange.getMin(), navigatorRange.getMax());
        scroll.setViewportMinMax(chartRange.getMin(), chartRange.getMax());
        Range scrollRange = scroll.getViewportMinMax();
        chart.setXMinMax(xAxisPosition, scrollRange.getMin(), scrollRange.getMax());
        List<ScrollListener> scrollListeners = axisToScrollListeners.get(xAxisPosition);
        scroll.addListener(new ScrollListener() {
            @Override
            public void onScrollChanged(double viewportMin, double viewportMax) {
                isChanged = true;
                chart.setXMinMax(xAxisPosition, viewportMin, viewportMax);
                for (ScrollListener listener : scrollListeners) {
                    listener.onScrollChanged(viewportMin, viewportMax);
                }
            }
        });
        return scroll;
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
        if(!isConfigured) {
            autoScaleX();
            autoScaleChartY();
            autoScaleNavigatorY();
            isConfigured = true;
        }
        canvas.setColor(config.getBackgroundColor());
        canvas.fillRect(0, 0, width, height);
        chart.draw(canvas);
        canvas.save();
        navigator.draw(canvas);
        Scale xScale = navigator.getXScale(navigatorXPosition);
        Range scrollTrack = new Range(xScale.getStart(), xScale.getEnd());
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            if(chart.getTraces(xPosition).size() > 0) {
                Scroll scroll = axisToScrolls.get(xPosition);
                if(scroll == null) {
                    scroll = createScroll(xPosition);
                    axisToScrolls.put(xPosition, scroll);
                }
                scroll.draw(canvas, scrollTrack, navigator.getBounds());
            }
        }
    }

    public int getChartTraceMarkSize(int traceNumber) {
        return  chart.getTraceMarkSize(traceNumber);
    }

    public int getNavigatorTraceMarkSize(int traceNumber) {
        return  navigator.getTraceMarkSize(traceNumber);
    }

    public XAxisPosition getChartTraceXAxisPosition(int traceNumber) {
        return chart.getTraceXAxisPosition(traceNumber);
    }
    public XAxisPosition getChartDefaultXAxisPosition() {
        return chart.getDefaultXAxisPosition();
    }


    public void setSize(int width, int height) {
        System.out.println();
        this.width = width;
        this.height = height;
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
        chartDataList.add(data);
        invalidate();
    }


    public void addChartTrace(String name, ChartData data, TracePainter tracePainter, boolean isXOpposite,  boolean isYOpposite) {
        chart.addTrace(name, data, tracePainter, isXOpposite, isYOpposite);
        chartDataList.add(data);
        invalidate();
    }

    public void addChartTrace(String name, ChartData data, TracePainter tracePainter, int stackNumber) {
        chart.addTrace(name, data, tracePainter, stackNumber);
        chartDataList.add(data);
        invalidate();
    }

    public void addChartTrace(String name, ChartData data, TracePainter tracePainter, int stackNumber, boolean isXOpposite,  boolean isYOpposite) {
        chart.addTrace(name, data, tracePainter, stackNumber, isXOpposite, isYOpposite);
        chartDataList.add(data);
        invalidate();
    }

    public void removeChartTrace(int traceNumber) {
        chart.removeTrace(traceNumber);
        chartDataList.remove(traceNumber);
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

    public void setChartTraceData(int traceNumber, ChartData data) {
        chart.setTraceData(traceNumber, data);
        chartDataList.set(traceNumber, data);
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
        navigatorDataList.add(data);
    }

    public void addNavigatorTrace(String name,ChartData data, TracePainter tracePainter, int stack) {
        navigator.addTrace(name, data, tracePainter, stack);
        navigatorDataList.add(data);
    }

    public void removeNavigatorTrace(int traceNumber) {
        navigator.removeTrace(traceNumber);
        navigatorDataList.remove(traceNumber);
    }

    public int navigatorTraceCount() {
        return navigator.traceCount();
    }

    public void setNavigatorTraceData(int traceNumber, ChartData data) {
        navigator.setTraceData(traceNumber, data);
        navigatorDataList.set(traceNumber, data);
    }

    public Range getNavigatorXMinMax() {
        return navigator.getXMinMax(navigatorXPosition);
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
            Scroll scroll = axisToScrolls.get(xPosition);
            if(scroll != null) {
                scroll.setMinMax(min, max);
            }
        }
    }

    public void setChartXMinMax(XAxisPosition xAxisPosition, double min, double max) {
        axisToScrolls.put(xAxisPosition, null);
        chart.setXMinMax(xAxisPosition, min, max);
    }

    public void autoScaleNavigatorY(int stack, YAxisPosition yPosition) {
        navigator.autoScaleY(stack, yPosition);
    }

    public void autoScaleNavigatorY() {
        navigator.autoScaleY();
    }

    public Range getChartXRange(XAxisPosition xAxisPosition) {
        return chart.getXRange(xAxisPosition);
    }

    public Scale getNavigatorXScale() {
        return navigator.getXScale(navigatorXPosition);
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
            Scale xScale = navigator.getXScale(navigatorXPosition);
            Range scrollTrack = new Range(xScale.getStart(), xScale.getEnd());
            if (axisToScrolls.get(xPosition).scrollbarContain(x, scrollTrack)) {
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
        Scale xScale = navigator.getXScale(navigatorXPosition);
        Range scrollTrack = new Range(xScale.getStart(), xScale.getEnd());
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setScrollbarPosition(x, scrollTrack);
        }
        return isChanged;
    }

    boolean translateScrolls(double dx) {
        isChanged = false;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            Scale xScale = navigator.getXScale(navigatorXPosition);
            Range scrollTrack = new Range(xScale.getStart(), xScale.getEnd());
            axisToScrolls.get(xPosition).moveScrollbar(dx, scrollTrack);
        }
        return isChanged;

    }

    boolean translateScrollsViewport(double dx) {
        isChanged = false;
        double dx1 = dx;
        Scale xScale = navigator.getXScale(navigatorXPosition);
        Range scrollTrack = new Range(xScale.getStart(), xScale.getEnd());
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            double d = dx * axisToScrolls.get(xPosition).scrollTrackToViewRatio(scrollTrack);
            if(Math.abs(dx1) > Math.abs(d)) {
                dx1 = d;
            }
        }
        translateScrolls(dx1);
        return isChanged;
    }


    boolean zoomScrollExtent(XAxisPosition xAxisPosition, double zoomFactor) {
        isChanged = false;
        Scroll scroll = axisToScrolls.get(xAxisPosition);
        if (scroll != null) {
            scroll.zoomViewport(zoomFactor);
        }
        return isChanged;
    }

    boolean zoomScrollExtent(double zoomFactor) {
        isChanged = false;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).zoomViewport(zoomFactor);
        }
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

    public Range getChartXMinMax(XAxisPosition xAxisPosition) {
        return chart.getXMinMax(xAxisPosition);
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
