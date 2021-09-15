package com.biorecorder.bichart;

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

public class BiChart {
    private static double DEFAULT_CHART_VIEWPORT_EXTENT_RATIO = 1.0/10;
    private int gap = 0; // between Chart and Preview px
    private Insets spacing = new Insets(0);
    private int navigatorHeightMin = 16; // px
    private Chart chart;
    private Chart navigator;
    private NavigableChartConfig config;
    private int width = 250;
    private int height = 100;
    private List<ChartData> chartDataList = new ArrayList<>();
    private List<ChartData> navigatorDataList = new ArrayList<>();
    private Map<XAxisPosition, Scroll> axisToScrolls = new HashMap<>(2);
    private Map<XAxisPosition, List<ScrollListener>> axisToScrollListeners = new HashMap<>(2);

    private boolean isPointToPointChart = false;

    private boolean isValid = false;
    private boolean isChanged = false;
    private boolean isScrollsConfigured = false;

    public BiChart(NavigableChartConfig config, Scale xScale) {
        this.config = new NavigableChartConfig(config);
        chart = new Chart(config.getChartConfig(), xScale, XAxisPosition.TOP, YAxisPosition.LEFT);
        navigator = new Chart(config.getNavigatorConfig(), xScale, XAxisPosition.BOTTOM, YAxisPosition.LEFT);
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

    public void invalidate() {
        isValid = false;
    }

    public void revalidate(RenderContext renderContext) {
        if(isValid) {
            return;
        }
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
        //  активируем ауто маргин чтобы отступы считались автоматом
        chart.setAutoMargin();
        navigator.setAutoMargin();
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
        isValid = true;
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
        if(!navigator.isValid()) {
            return;
        }
        Scale xScale = navigator.getXScale(navigator.getDefaultXPosition());
        double xLength =  xScale.getLength();
        Range xMinMax = null;
        for (int i = 0; i < navigatorDataList.size(); i++) {
            ChartData data = navigatorDataList.get(i);
            xMinMax = Range.join(xMinMax, dataMinMax(data));
        }
        for (int i = 0; i < chartDataList.size(); i++) {
            ChartData data = chartDataList.get(i);
            xMinMax = Range.join(xMinMax, dataMinMax(data));
        }
        if (xMinMax != null) {
            Range bestRangeMin = null;
            if(isPointToPointChart) {
                for (XAxisPosition xPosition : XAxisPosition.values()) {
                    bestRangeMin = null;
                    List<Integer> traceNumbers = chart.getTraces(xPosition);
                    for (int i = 0; i < traceNumbers.size(); i++) {
                        int traceNumber = traceNumbers.get(i);
                        Range dataBestRange = dataBestRange(chartDataList.get(traceNumber), chart.getTraceMarkSize(traceNumber), xScale, xLength, xMinMax.getMin());
                        if (dataBestRange != null) {
                            if(bestRangeMin == null || bestRangeMin.length() > dataBestRange.length()) {
                                bestRangeMin = dataBestRange;
                            }
                        }
                    }
                    if (bestRangeMin != null && bestRangeMin.getMax() > xMinMax.getMax()) {
                        xMinMax = new Range(xMinMax.getMin(), bestRangeMin.getMax());
                    }
                }
            }
        }
        if (xMinMax != null) {
            setNavigatorXMinMax(xMinMax.getMin(), xMinMax.getMax());
        }
    }

    private void autoScaleChartX(XAxisPosition xPosition) {
        if(!chart.isValid()) {
            return;
        }
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
        Range xMinMax = minBestRange;
        Scale navigatorXScale = navigator.getXScale(navigator.getDefaultXPosition());
        Range navigatorMinMax = new Range(navigatorXScale.getMin(), navigatorXScale.getMax());
        if (xMinMax == null ) {
            // set chart default viewport extent (1/10 of navigator range)
            // we need it in the case if chart has no traces or traces have no data
            xMinMax = navigatorMinMax;
            xMinMax = new Range(xMinMax.getMin(), xMinMax.getMin() + xMinMax.length() * DEFAULT_CHART_VIEWPORT_EXTENT_RATIO);
        }

        axisToScrolls.remove(xPosition);
        chart.setXMinMax(xPosition, xMinMax.getMin(), xMinMax.getMax());
        if(chart.getTraces(xPosition).size() > 0) {
            Scroll scroll = axisToScrolls.get(xPosition);
            if(scroll == null) {
                scroll = createScroll(xPosition);
                axisToScrolls.put(xPosition, scroll);
                if(!isScrollsConfigured) {
                    scroll.setViewportMin(navigatorMinMax.getMin());
                }
            }
        }

    }

    private void autoScaleChartX() {
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            autoScaleChartX(xPosition);
        }
    }

    private Scroll createScroll(XAxisPosition xAxisPosition) {
        Scale chartScale = chart.getXScale(xAxisPosition);
        Scale navigatorScale = navigator.getXScale(navigator.getDefaultXPosition());
        Range navigatorRange = new Range(navigatorScale.getMin(), navigatorScale.getMax());
        Range chartRange = new Range(chartScale.getMin(), chartScale.getMax());
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
        revalidate(canvas.getRenderContext());
        if(!isScrollsConfigured) {
            autoScaleX();
            autoScaleChartY();
            autoScaleNavigatorY();
            isScrollsConfigured = true;
        }
        canvas.setColor(config.getBackgroundColor());
        canvas.fillRect(0, 0, width, height);
        chart.draw(canvas);
        canvas.save();
        navigator.draw(canvas);
        Scale xScale = navigator.getXScale(navigator.getDefaultXPosition());
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

    public void autoScaleX(XAxisPosition xPosition) {
        autoScaleNavigatorX();
        autoScaleChartX(xPosition);
    }


    public void autoScaleX() {
        autoScaleNavigatorX();
        autoScaleChartX();
        isScrollsConfigured = true;
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



    public int getChartTraceMarkSize(int traceNumber) {
        return  chart.getTraceMarkSize(traceNumber);
    }

    public int getNavigatorTraceMarkSize(int traceNumber) {
        return  navigator.getTraceMarkSize(traceNumber);
    }

    public XAxisPosition getChartTraceXAxisPosition(int traceNumber) {
        return chart.getTraceXPosition(traceNumber);
    }

    public XAxisPosition getChartDefaultXAxisPosition() {
        return chart.getDefaultXPosition();
    }


    public void setSize(int width, int height) throws IllegalArgumentException {
        if(width == 0 || height == 0) {
            String errMsg = "Width and height must be > 0";
            throw new IllegalArgumentException(errMsg);
        }
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
    }

    public void addChartTrace(String name, ChartData data, TracePainter tracePainter, int stackNumber) {
        chart.addTrace(name, data, tracePainter, stackNumber);
        chartDataList.add(data);
    }

    public void addChartTrace(String name, ChartData data, TracePainter tracePainter, int stackNumber, boolean isXOpposite,  boolean isYOpposite) {
        chart.addTrace(name, data, tracePainter, stackNumber, isXOpposite, isYOpposite);
        chartDataList.add(data);
    }

    public void removeChartTrace(int traceNumber) {
        chart.removeTrace(traceNumber);
        chartDataList.remove(traceNumber);
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

    public void setChartXPrefixAndSuffix(XAxisPosition xPosition, @Nullable String prefix, @Nullable String suffix) {
        navigator.setXPrefixAndSuffix(xPosition, prefix, suffix);
    }

    public void setChartYPrefixAndSuffix(int stack, YAxisPosition yPosition, @Nullable String prefix, @Nullable String suffix) {
        navigator.setYPrefixAndSuffix(stack, yPosition, prefix, suffix);
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

    public BRectangle getBounds() {
        return new BRectangle(0, 0, width, height);
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

    public void autoScaleNavigatorY() {
        navigator.autoScaleY();
    }

    public Scale getNavigatorXScale() {
        return navigator.getXScale(navigator.getDefaultXPosition());
    }

    /**
     * =============================================================*
     * Protected method for careful use  mostly to interact through GUI                          *
     * ==============================================================
     */
    List<XAxisPosition> getChartXPositionsUsedByStack(int stack) {
        return  chart.getXPositionsUsedByStack(stack);
    }

    List<YAxisPosition> getChartYPositionsUsedByStack(int stack) {
        return  chart.getYPositionsUsedByStack(stack);
    }

    List<YAxisPosition> getNavigatorYPositionsUsedByStack(int stack) {
        return  navigator.getYPositionsUsedByStack(stack);
    }

    XAxisPosition getChartTraceXPosition(int traceNumber) {
        return chart.getTraceXPosition(traceNumber);
    }

    YAxisPosition getChartTraceYPosition(int traceNumber) {
        return chart.getTraceYPosition(traceNumber);
    }

    int getChartTraceStack(int traceNumber) {
        return chart.getTraceStack(traceNumber);
    }

    XAxisPosition getChartDefaultXPosition() {
        return chart.getDefaultXPosition();
    }

    YAxisPosition getChartDefaultYPosition() {
        return chart.getDefaultYPosition();
    }

    XAxisPosition getNavigatorTraceXPosition(int traceNumber) {
        return navigator.getTraceXPosition(traceNumber);
    }

    YAxisPosition getNavigatorTraceYPosition(int traceNumber) {
        return navigator.getTraceYPosition(traceNumber);
    }

    int getNavigatorTraceStack(int traceNumber) {
        return navigator.getTraceStack(traceNumber);
    }

    XAxisPosition getNavigatorDefaultXPosition() {
        return navigator.getDefaultXPosition();
    }

    YAxisPosition getNavigatorDefaultYPosition() {
        return navigator.getDefaultYPosition();
    }

    boolean chartContain(int x, int y) {
        return chart.getBounds().contain(x, y);
    }

    boolean navigatorContain(int x, int y) {
        return navigator.getBounds().contain(x, y);
    }

    public int getChartStackContaining(int x, int y) {
        return chart.getStackContaining(x, y);

    }

    public int getChartLegendButtonContaining(int x, int y) {
        return chart.getLegendButtonContaining(x, y);
    }

    public int getNavigatorStackContaining(int x, int y) {
        return navigator.getStackContaining(x, y);

    }

    public int getNavigatorLegendButtonContaining(int x, int y) {
        return navigator.getLegendButtonContaining(x, y);
    }

    public void selectChartTrace(int traceNumber) {
        chart.selectTrace(traceNumber);
    }

    void removeChartTraceSelection() {
        chart.removeTraceSelection();
    }

    public int getChartSelectedTrace() {
        return chart.getSelectedTrace();
    }

    public void selectNavigatorTrace(int traceNumber) {
        navigator.selectTrace(traceNumber);
    }

    void removeNavigatorTraceSelection() {
        navigator.removeTraceSelection();
    }

    public int getNavigatorSelectedTrace() {
        return navigator.getSelectedTrace();
    }

    public boolean hoverOff() {
        boolean isChanged = chart.hoverOff();
        isChanged = navigator.hoverOff() || isChanged;
        return isChanged;
    }

    public boolean chartHoverOn(int traceNumber, int pointIndex) {
        return chart.hoverOn(traceNumber, pointIndex);
    }

    public boolean navigatorHoverOn(int traceNumber, int pointIndex) {
        return navigator.hoverOn(traceNumber, pointIndex);
    }

    public TracePoint getChartNearestPoint(int x, int y) {
        return chart.getNearestPoint(x, y);
    }

    public TracePoint getNavigatorNearestPoint(int x, int y) {
        return navigator.getNearestPoint(x, y);
    }

    public boolean scrollContain(int x, int y) {
        if (!navigator.getBounds().contain(x, y)) {
            return false;
        }
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            Scale xScale = navigator.getXScale(navigator.getDefaultXPosition());
            Range scrollTrack = new Range(xScale.getStart(), xScale.getEnd());
            if (axisToScrolls.get(xPosition).scrollbarContain(x, scrollTrack)) {
                return true;
            }
        }
        return false;
    }


    boolean setScrollsPosition(double x) {
        isChanged = false;
        Scale xScale = navigator.getXScale(navigator.getDefaultXPosition());
        Range scrollTrack = new Range(xScale.getStart(), xScale.getEnd());
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setScrollbarPosition(x, scrollTrack);
        }
        return isChanged;
    }

    boolean translateScrolls(double dx) {
        isChanged = false;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            Scale xScale = navigator.getXScale(navigator.getDefaultXPosition());
            Range scrollTrack = new Range(xScale.getStart(), xScale.getEnd());
            axisToScrolls.get(xPosition).moveScrollbar(dx, scrollTrack);
        }
        return isChanged;

    }

    boolean translateScrollsViewport(double dx) {
        isChanged = false;
        double dx1 = dx;
        Scale xScale = navigator.getXScale(navigator.getDefaultXPosition());
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

    boolean zoomChartY(int stack, YAxisPosition yPosition, double zoomFactor) {
        return chart.zoomY(stack, yPosition, zoomFactor);
    }

    boolean translateChartY(int stack, YAxisPosition yPosition, int dy) {
        return chart.translateY(stack, yPosition, dy);
    }

    boolean zoomNavigatorY(int stack, YAxisPosition yPosition, double zoomFactor) {
        return navigator.zoomY(stack, yPosition, zoomFactor);
    }

    boolean translateNavigatorY(int stack, YAxisPosition yPosition, int dy) {
        return navigator.translateY(stack, yPosition, dy);
    }
}
