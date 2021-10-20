package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scales.TimeScale;
import com.biorecorder.bichart.scroll.Scroll;
import com.biorecorder.bichart.scroll.ScrollListener;
import com.biorecorder.bichart.traces.TracePainter;
import com.sun.istack.internal.Nullable;

import java.util.*;

public class BiChart {
    private static double DEFAULT_CHART_VIEWPORT_EXTENT_RATIO = 1.0 / 10;
    private int gap = 0; // between Chart and Preview px
    private Insets spacing = new Insets(0);
    private int navigatorHeightMin = 16; // px
    protected Chart chart;
    protected Chart navigator;
    private BiChartConfig config;
    private int width = 250;
    private int height = 100;
    private List<XYSeries> chartDataList = new ArrayList<>();
    private List<XYSeries> navigatorDataList = new ArrayList<>();
    private Map<XAxisPosition, Scroll> axisToScrolls = new HashMap<>(2);
    Map<XAxisPosition, List<ScrollListener>> axisToScrollListeners = new HashMap<>(2);

    private boolean isPointToPointChart = false;
    XAxisPosition navDefaultXPosition = XAxisPosition.BOTTOM;

    private boolean isValid = false;
    private boolean isChanged = false;
    private boolean isScrollsConfigured = false;
    private boolean isDateTime;

    public BiChart(BiChartConfig config, boolean isDateTime) {
        Scale xScale = createScale(isDateTime);
        this.isDateTime = isDateTime;
        this.config = new BiChartConfig(config);
        chart = new Chart(config.getChartConfig(), xScale, XAxisPosition.TOP, YAxisPosition.RIGHT);
        navigator = new Chart(config.getNavigatorConfig(), xScale, navDefaultXPosition, YAxisPosition.RIGHT);
        chart.setSpacing(new Insets(0));
        navigator.setSpacing(new Insets(0));
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            axisToScrollListeners.put(xPosition, new ArrayList<>());
        }
    }

    private static Scale createScale(boolean isDateTime) {
        Scale scale = isDateTime ? new TimeScale() : new LinearScale();
        return scale;
    }

    public void addScrollListener(XAxisPosition xAxisPosition, ScrollListener scrollListener) {
        axisToScrollListeners.get(xAxisPosition).add(scrollListener);
    }

    public void invalidate() {
        isValid = false;
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
        //  выравниваем маргины и фиксируем их
        Insets chartMargin = chart.getMargin(renderContext);
        Insets navigatorMargin = navigator.getMargin(renderContext);
        int leftMargin = Math.max(chartMargin.left(), navigatorMargin.left());
        int rightMargin = Math.max(chartMargin.right(), navigatorMargin.right());
        chart.setMargin(new Insets(chartMargin.top(), rightMargin, chartMargin.bottom(), leftMargin));
        navigator.setMargin(new Insets(navigatorMargin.top(), rightMargin, navigatorMargin.bottom(), leftMargin));

        int viewportExtent = getChartXLength();
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setViewportExtent(viewportExtent);
        }
        isValid = true;
    }

    private int getChartXLength() {
        return width - spacing.right() - spacing.left();
    }

    private Scale getChartBestScale(List<Integer> traceNumbers, Range minMax, int xLength) {
        Scale xScale = createScale(isDateTime);
        int fullLength = isPointToPointChart ? 0 : xLength;
        for (int i = 0; i < traceNumbers.size(); i++) {
            int traceNumber = traceNumbers.get(i);
            XYSeries traceData = chartDataList.get(traceNumber);
            Range traceDataMinMax = dataMinMax(traceData);
            if (traceData.size() > 1 && traceDataMinMax != null && traceDataMinMax.length() > 0) {
                int dataLength = traceData.size() * chart.getTraceMarkSize(traceNumber);
                xScale.setMinMax(traceDataMinMax.getMin(), traceDataMinMax.getMax());
                xScale.setStartEnd(0, dataLength);
                double minPosition = xScale.scale(minMax.getMin());
                double maxPosition = xScale.scale(minMax.getMax());
                fullLength = Math.max(fullLength, (int) (maxPosition - minPosition));
            }
        }
        if (fullLength == 0) {
            fullLength = xLength;
        }
        xScale.setStartEnd(0, fullLength);
        xScale.setMinMax(minMax.getMin(), minMax.getMax());
        return xScale;
    }

    private void createScroll(XAxisPosition xPosition, int viewportExtent) {
        List<Integer> traceNumbers = chart.getTraces(xPosition);
        Range minMax = null;
        for (Integer traceNumber : traceNumbers) {
            XYSeries traceData = chartDataList.get(traceNumber);
            minMax = Range.join(minMax, dataMinMax(traceData));
        }
        if (minMax == null) {
            axisToScrolls.remove(xPosition);
            return;
        }
        if (minMax.length() == 0) {
            axisToScrolls.remove(xPosition);
            chart.setXMinMax(xPosition, minMax.getMin() - 1, minMax.getMax() + 1);
            return;
        }
        if (axisToScrolls.get(xPosition) == null) {
            Scale scrollScale = getChartBestScale(traceNumbers, minMax, viewportExtent);
            Scroll scroll = new Scroll(config.getScrollConfig(), scrollScale);
            axisToScrolls.put(xPosition, scroll);
            scroll.setViewportExtent(viewportExtent);
            chart.setXMinMax(xPosition, scroll.getViewportMin(), scroll.getViewportMax());
            scroll.addListener(new ScrollListener() {
                @Override
                public void onScrollChanged(double viewportMin, double viewportMax) {
                    isChanged = true;
                    List<ScrollListener> scrollListeners = axisToScrollListeners.get(xPosition);
                    chart.setXMinMax(xPosition, viewportMin, viewportMax);
                    for (ScrollListener listener : scrollListeners) {
                        listener.onScrollChanged(viewportMin, viewportMax);
                    }
                }
            });
        }
    }

    private void createScrolls() {
        int viewportExtent = getChartXLength();
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            createScroll(xPosition, viewportExtent);
        }
        isScrollsConfigured = true;
    }

    public void autoScaleChartX(XAxisPosition xPosition) {
        if (!isValid) {
            return;
        }
        List<Integer> traceNumbers = chart.getTraces(xPosition);
        Range minMax = null;
        for (Integer traceNumber : traceNumbers) {
            XYSeries traceData = chartDataList.get(traceNumber);
            minMax = Range.join(minMax, dataMinMax(traceData));
        }
        if (minMax == null) {
            axisToScrolls.remove(xPosition);
            return;
        }
        if (minMax.length() == 0) {
            axisToScrolls.remove(xPosition);
            chart.setXMinMax(xPosition, minMax.getMin() - 1, minMax.getMax() + 1);
            return;
        }
        Scroll scroll = axisToScrolls.get(xPosition);
        if (scroll != null) {
            Scale bestScale = getChartBestScale(traceNumbers, minMax, getChartXLength());
            double zoomFactor = bestScale.getLength() / (scroll.getEnd() - scroll.getStart());
            scroll.zoom(zoomFactor, (int) (scroll.getViewportExtent() / 2));
        }
    }

    private void autoScaleNavigatorX() {
        Range xMinMax = null;
        for (int i = 0; i < navigatorDataList.size(); i++) {
            XYSeries data = navigatorDataList.get(i);
            xMinMax = Range.join(xMinMax, dataMinMax(data));
        }
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            Scroll scroll = axisToScrolls.get(xPosition);
            Range scrollRange = new Range(scroll.getMin(), scroll.getMax());
            xMinMax = Range.join(xMinMax, scrollRange);
        }
        if (xMinMax != null) {
            navigator.setXMinMax(navDefaultXPosition, xMinMax.getMin(), xMinMax.getMax());
        }
    }


    public void autoScaleX() {
        if (!isValid) {
            return;
        }
        if (!isScrollsConfigured) {
            createScrolls();
        }
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            autoScaleChartX(xPosition);
        }
        autoScaleNavigatorX();
    }

    public double geChartXMin(XAxisPosition xPosition) {
        return chart.getXMin(xPosition);
    }

    public double geChartXMax(XAxisPosition xPosition) {
        return chart.getXMax(xPosition);
    }

    /**
     * ==================================================*
     * Base methods to interact                          *
     * ==================================================*
     */

    public void draw(BCanvas canvas) {
        revalidate(canvas.getRenderContext());
        if (!isScrollsConfigured) {
            createScrolls();
            autoScaleNavigatorX();
            autoScaleChartY();
            autoScaleNavigatorY();
        }
        canvas.setColor(config.getBackgroundColor());
        canvas.fillRect(0, 0, width, height);
        chart.draw(canvas);
        canvas.save();
        navigator.draw(canvas);
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            Scroll scroll = axisToScrolls.get(xPosition);
            if (scroll != null) {
                navigator.drawRect(canvas, navDefaultXPosition, scroll.getViewportMin(), scroll.getViewportMax(), scroll.getColor(), scroll.getBorder());
            }
        }
    }

    public void setNavigatorXMinMax(double min, double max) {
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            navigator.setXMinMax(xPosition, min, max);
            Scroll scroll = axisToScrolls.get(xPosition);
            if (scroll != null) {
                scroll.setMinMax(min, max);
            }
        }
    }

    public int getChartTraceMarkSize(int traceNumber) {
        return chart.getTraceMarkSize(traceNumber);
    }

    public int getNavigatorTraceMarkSize(int traceNumber) {
        return navigator.getTraceMarkSize(traceNumber);
    }

    public void setSize(int width, int height) throws IllegalArgumentException {
        if (width == 0 || height == 0) {
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

    public void addChartTrace(String name, XYSeries data, TracePainter tracePainter) {
        addChartTrace(name, data, tracePainter, false, false);
    }


    public void addChartTrace(String name, XYSeries data, TracePainter tracePainter, boolean isXOpposite, boolean isYOpposite) {
        chart.addTrace(name, data, tracePainter, isXOpposite, isYOpposite);
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

    public void setChartTraceData(int traceNumber, XYSeries data) {
        chart.setTraceData(traceNumber, data);
        chartDataList.set(traceNumber, data);
    }

    /**
     * =======================Base methods to interact with navigator==========================
     **/

    public BRectangle getBounds() {
        return new BRectangle(0, 0, width, height);
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

    public void addNavigatorTrace(String name, XYSeries data, TracePainter tracePainter) {
        addNavigatorTrace(name, data, tracePainter, false);
    }

    public void addNavigatorTrace(String name, XYSeries data, TracePainter tracePainter, boolean isYOpposite) {
        navigator.addTrace(name, data, tracePainter, false, isYOpposite);
        navigatorDataList.add(data);
    }

    public void removeNavigatorTrace(int traceNumber) {
        navigator.removeTrace(traceNumber);
        navigatorDataList.remove(traceNumber);
    }

    public int navigatorTraceCount() {
        return navigator.traceCount();
    }

    public void setNavigatorTraceData(int traceNumber, XYSeries data) {
        navigator.setTraceData(traceNumber, data);
        navigatorDataList.set(traceNumber, data);
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

    public void autoScaleNavigatorY(int stack, YAxisPosition yPosition) {
        navigator.autoScaleY(stack, yPosition);
    }

    public void autoScaleNavigatorY() {
        navigator.autoScaleY();
    }


    /**
     * =============================================================*
     * Protected method for careful use  mostly to interact through GUI                          *
     * ==============================================================
     */
    static Range dataMinMax(XYSeries data) {
        if (data.size() > 0) {
            return new Range(data.getX(0), data.getX(data.size() - 1));
        }
        return null;
    }

    List<XAxisPosition> getChartXPositionsUsedByStack(int stack) {
        return chart.getXPositionsUsedByStack(stack);
    }

    List<YAxisPosition> getChartYPositionsUsedByStack(int stack) {
        return chart.getYPositionsUsedByStack(stack);
    }

    List<YAxisPosition> getNavigatorYPositionsUsedByStack(int stack) {
        return navigator.getYPositionsUsedByStack(stack);
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

    YAxisPosition getNavigatorTraceYPosition(int traceNumber) {
        return navigator.getTraceYPosition(traceNumber);
    }

    int getNavigatorTraceStack(int traceNumber) {
        return navigator.getTraceStack(traceNumber);
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

    XAxisPosition scrollContain(int x, int y) {
        if (!navigator.getBounds().contain(x, y)) {
            return null;
        }
        double xValue = navigator.positionToValue(navDefaultXPosition, x);
        double viewportMinRange = Double.MAX_VALUE;
        XAxisPosition scrollXAxisPosition = null;
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            Scroll scroll = axisToScrolls.get(xPosition);
            if (scroll.viewportContainValue(xValue)) {
                double viewportRange = scroll.getViewportMax() - scroll.getViewportMin();
                if (viewportMinRange > viewportRange) {
                    scrollXAxisPosition = xPosition;
                    viewportMinRange = viewportRange;
                }
            }
        }
        return scrollXAxisPosition;
    }

    boolean positionScrolls(double x) {
        isChanged = false;
        double xValue = navigator.positionToValue(navDefaultXPosition, x);
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setViewportCenterValue(xValue);
        }
        return isChanged;
    }

    boolean positionChartX(XAxisPosition xAxisPosition, double x) {
        isChanged = false;
        double xValue = chart.positionToValue(xAxisPosition, x);
        for (XAxisPosition xPosition : axisToScrolls.keySet()) {
            axisToScrolls.get(xPosition).setViewportCenterValue(xValue);
        }
        return isChanged;
    }

    boolean translateChartX(XAxisPosition xAxisPosition, double dx) {
        isChanged = false;
        Scroll scroll = axisToScrolls.get(xAxisPosition);
        if (scroll != null) {
            double centerValue = scroll.getViewportCenterValue();
            double centerValueNew = chart.positionToValue(xAxisPosition, chart.valueToPosition(xAxisPosition, centerValue) + dx);
            for (XAxisPosition xPosition : axisToScrolls.keySet()) {
                axisToScrolls.get(xPosition).setViewportCenterValue(centerValueNew);
            }
        }
        return isChanged;
    }


    boolean translateScrolls(XAxisPosition xAxisPosition, double dx) {
        isChanged = false;
        Scroll scroll = axisToScrolls.get(xAxisPosition);
        if (scroll != null) {
            double centerValue = scroll.getViewportCenterValue();
            double centerValueNew = navigator.positionToValue(navDefaultXPosition, navigator.valueToPosition(navDefaultXPosition, centerValue) + dx);
            for (XAxisPosition xPosition : axisToScrolls.keySet()) {
                axisToScrolls.get(xPosition).setViewportCenterValue(centerValueNew);
            }
        }
        return isChanged;
    }

    boolean zoomChartX(XAxisPosition xAxisPosition, double zoomFactor, int anchorPoint) {
        isChanged = false;
        Scroll scroll = axisToScrolls.get(xAxisPosition);
        if (scroll != null) {
            scroll.zoom(zoomFactor, anchorPoint);
        }
        return isChanged;
    }

    boolean zoomChartY(int stack, YAxisPosition yPosition, double zoomFactor, int anchorPoint) {
        return chart.zoomY(stack, yPosition, zoomFactor, anchorPoint);
    }

    boolean translateChartY(int stack, YAxisPosition yPosition, int dy) {
        return chart.translateY(stack, yPosition, dy);
    }

    boolean zoomNavigatorY(int stack, YAxisPosition yPosition, double zoomFactor, int anchorPoint) {
        return navigator.zoomY(stack, yPosition, zoomFactor, anchorPoint);
    }

    boolean translateNavigatorY(int stack, YAxisPosition yPosition, int dy) {
        return navigator.translateY(stack, yPosition, dy);
    }
}
