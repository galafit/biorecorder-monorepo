package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.Orientation;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scales.TimeScale;
import com.biorecorder.bichart.scroll.Scroll;
import com.biorecorder.bichart.scroll.ScrollListener;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;
import com.sun.istack.internal.Nullable;

import java.util.*;

public class BiChart {
    private DataProcessor dataProcessor;

    private int gap = 0; // between Chart and Preview px
    private Insets spacing = new Insets(0);
    private int navigatorHeightMin = 25; // px
    private BiChartConfig config;
    private int width = 0;
    private int height = 0;
    private boolean isPointToPointChart = false;
    protected Chart chart;
    protected Chart navigator;
    protected Map<Integer, Scroll> axisToScrolls = new HashMap<>(2);
    protected Map<Integer, List<ScrollListener>> axisToScrollListeners = new HashMap<>(2);
    protected int navDefaultXAxisNumber;

    private boolean isValid = false;
    private boolean scrollsChanged = false;
    private boolean scrollsAtEnd = false;
    private boolean isDateTime;
    private boolean isDataChanged = false;


    public BiChart(boolean isDateTime, BiChartConfig chartConfig, ProcessingConfig processingConfig, boolean scrollsAtEnd) {
        Scale xScale = createScale(isDateTime);
        this.isDateTime = isDateTime;
        this.config = new BiChartConfig(chartConfig);
        chart = new Chart(chartConfig.getChartConfig(), xScale);
        chart.setDefaultXOrientation(Orientation.TOP);
        chart.setDefaultYOrientation(Orientation.RIGHT);
        navigator = new Chart(chartConfig.getNavigatorConfig(), xScale);
        navigator.setDefaultXOrientation(Orientation.BOTTOM);
        navigator.setDefaultYOrientation(Orientation.RIGHT);
        navDefaultXAxisNumber = Chart.xAxisOrientationToNumber(Orientation.BOTTOM);
        chart.setSpacing(new Insets(0));
        navigator.setSpacing(new Insets(0));
        for (int i = 0; i < chart.xAxesCount(); i++) {
            axisToScrollListeners.put(i, new ArrayList<>());
        }
        dataProcessor = new DataProcessor(isDateTime, xScale, processingConfig);
        this.scrollsAtEnd = scrollsAtEnd;
    }

    public BiChart(boolean isDateTime, ProcessingConfig processingConfig, boolean scrollsAtEnd ) {
        this(isDateTime, DarkTheme.getNavigableChartConfig(), processingConfig, scrollsAtEnd);
    }

    public BiChart(boolean isDateTime, boolean scrollsAtEnd) {
        this(isDateTime, DarkTheme.getNavigableChartConfig(), new ProcessingConfig(), scrollsAtEnd);
    }

    public BiChart(boolean isDateTime) {
        this(isDateTime, DarkTheme.getNavigableChartConfig(), new ProcessingConfig(), false);
    }

    private static Scale createScale(boolean isDateTime) {
        Scale scale = isDateTime ? new TimeScale() : new LinearScale();
        return scale;
    }

    public void addScrollListener(Orientation Orientation, ScrollListener scrollListener) {
        axisToScrollListeners.get(Orientation).add(scrollListener);
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
        if(width1 <= 0 || height1 <= 0) {
            chart.setBounds(0, 0, 0, 0);
            navigator.setBounds(0, 0, 0, 0);
            return;
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
        Insets chartMargin = chart.calculateMargin(renderContext);
        Insets navigatorMargin = navigator.calculateMargin(renderContext);
        int leftMargin = Math.max(chartMargin.left(), navigatorMargin.left());
        int rightMargin = Math.max(chartMargin.right(), navigatorMargin.right());
        chart.setMargin(new Insets(chartMargin.top(), rightMargin, chartMargin.bottom(), leftMargin));
        navigator.setMargin(new Insets(navigatorMargin.top(), rightMargin, navigatorMargin.bottom(), leftMargin));

        int xLength = getXLength();
        for (Integer xAxisNumber : axisToScrolls.keySet()) {
            axisToScrolls.get(xAxisNumber).setViewportExtent(xLength);
        }
        dataProcessor.onResize(xLength);
        isValid = true;
    }

    protected int getXLength() {
        Insets margin = chart.getMargin();
        return width - spacing.right() - spacing.left() - margin.left() - margin.right();
    }

    private Scale getChartBestScale(List<Integer> traceNumbers, Range minMax) {
        Scale xScale = createScale(isDateTime);
        int fullLength = 0; //isPointToPointChart ? 0 : xLength;
        for (int i = 0; i < traceNumbers.size(); i++) {
            int traceNumber = traceNumbers.get(i);
            Range traceDataMinMax = dataProcessor.getChartTraceDataRange(traceNumber);
            int traceDataSize = dataProcessor.getChartTraceDataSize(traceNumber);
            if (traceDataSize > 1 && traceDataMinMax != null && traceDataMinMax.length() > 0) {
                int dataLength = traceDataSize * chart.getTraceMarkSize(traceNumber);
                xScale.setMinMax(traceDataMinMax.getMin(), traceDataMinMax.getMax());
                xScale.setStartEnd(0, dataLength);
                double minPosition = xScale.scale(minMax.getMin());
                double maxAxisNumber = xScale.scale(minMax.getMax());
                fullLength = Math.max(fullLength, (int) (maxAxisNumber - minPosition));
            }
        }
       /* if (fullLength == 0) {
            fullLength = xLength;
        }*/
        xScale.setStartEnd(0, fullLength);
        xScale.setMinMax(minMax.getMin(), minMax.getMax());
        return xScale;
    }

    // return true if new scroll was created and false otherwise
    protected Scroll createScroll(int xAxisNumber, int viewportExtent) {
        List<Integer> traceNumbers = chart.getTraces(xAxisNumber);
        Range minMax = null;

        for (Integer traceNumber : traceNumbers) {
            minMax = Range.join(minMax, dataProcessor.getChartTraceDataRange(traceNumber));
        }
        if (minMax == null) {
            axisToScrolls.remove(xAxisNumber);
            return null;
        }
        if (minMax.length() == 0) {
            axisToScrolls.remove(xAxisNumber);
            double xMin = minMax.getMin() - 1;
            double xMax = minMax.getMax() + 1;
            chart.setXMinMax(xAxisNumber, xMin, xMax);
            Set<Integer> yAxes = chart.getYAxesNumbersUsedByXAxis(xAxisNumber);
            for (Integer yAxisNumber : yAxes) {
                chart.autoScaleY(yAxisNumber);
            }
            return null;
        }
        Scale scrollScale = getChartBestScale(traceNumbers, minMax);
        if(scrollScale.length() <= viewportExtent) {
            axisToScrolls.remove(xAxisNumber);
            chart.setXMinMax(xAxisNumber, minMax.getMin(), minMax.getMax());
            Set<Integer> yAxes = chart.getYAxesNumbersUsedByXAxis(xAxisNumber);
            for (Integer yAxisNumber : yAxes) {
                chart.autoScaleY(yAxisNumber);
            }
            dataProcessor.onChartRangeChanged(minMax.getMin(), minMax.getMax(), chart.getTraces(xAxisNumber));
            return null;
        }
        if (axisToScrolls.get(xAxisNumber) == null) {
            Scroll scroll = new Scroll(config.getScrollConfig(), scrollScale);
            axisToScrolls.put(xAxisNumber, scroll);
            scroll.setViewportExtent(viewportExtent);
            chart.setXMinMax(xAxisNumber, scroll.getViewportMin(), scroll.getViewportMax());
            dataProcessor.onChartRangeChanged(scroll.getViewportMin(), scroll.getViewportMax(), chart.getTraces(xAxisNumber));
            scroll.addListener(new ScrollListener() {
                @Override
                public void onScrollChanged(double viewportMin, double viewportMax) {
                    scrollsChanged = true;
                    List<ScrollListener> scrollListeners = axisToScrollListeners.get(xAxisNumber);
                    chart.setXMinMax(xAxisNumber, viewportMin, viewportMax);
                    dataProcessor.onChartRangeChanged(viewportMin, viewportMax, chart.getTraces(xAxisNumber));
                    for (ScrollListener listener : scrollListeners) {
                        listener.onScrollChanged(viewportMin, viewportMax);
                    }
                }
            });
            return scroll;
        }
        return null;
    }

    private void setChartTraceData() {
        Map<Integer, XYSeries> tracesData = dataProcessor.chartTracesDataToUpdate();
        if(tracesData != null) {
            for (Integer traceNumber : tracesData.keySet()) {
                chart.setTraceData(traceNumber, tracesData.get(traceNumber));
            }
        }
    }

    private void setNavigatorTraceData() {
        Map<Integer, XYSeries> tracesData = dataProcessor.navigatorTracesDataToUpdate();
        if(tracesData != null) {
            for (Integer traceNumber : tracesData.keySet()) {
                navigator.setTraceData(traceNumber, tracesData.get(traceNumber));
            }
        }
    }


    public void autoScaleChartX(Integer xAxisNumber) {
        if (!isValid) {
            return;
        }
        List<Integer> traceNumbers = chart.getTraces(xAxisNumber);
        Range minMax = null;
        for (Integer traceNumber : traceNumbers) {
            minMax = Range.join(minMax, dataProcessor.getChartTraceDataRange(traceNumber));
        }
        if (minMax == null) {
            axisToScrolls.remove(xAxisNumber);
            return;
        }
        if (minMax.length() == 0) {
            axisToScrolls.remove(xAxisNumber);
            chart.setXMinMax(xAxisNumber, minMax.getMin() - 1, minMax.getMax() + 1);
            return;
        }
        Scroll scroll = axisToScrolls.get(xAxisNumber);
        if (scroll != null) {
            Scale bestScale = getChartBestScale(traceNumbers, minMax);
            double bestLength = bestScale.scale(scroll.getMax()) - bestScale.scale(scroll.getMin());
            double zoomFactor = bestLength / (scroll.getEnd() - scroll.getStart());
            scroll.zoom(zoomFactor, (int) (scroll.getViewportExtent() / 2));
        }
    }

    public void autoScaleNavigatorX() {
        Range xMinMax = null;
        for (int i = 0; i < navigator.traceCount(); i++) {
            xMinMax = Range.join(xMinMax, dataProcessor.getNavigatorTraceDataRange(i));
        }
        for (int i = 0; i < chart.traceCount(); i++) {
            xMinMax = Range.join(xMinMax, dataProcessor.getChartTraceDataRange(i));
        }
        if (xMinMax != null) {
            navigator.setXMinMax(navDefaultXAxisNumber, xMinMax.getMin(), xMinMax.getMax());
            for (Integer xAxisNumber : axisToScrolls.keySet()) {
                Scroll scroll = axisToScrolls.get(xAxisNumber);
                scroll.setMinMax(xMinMax.getMin(), xMinMax.getMax());
            }
            dataProcessor.onNavigatorRangeChanged(xMinMax.getMin(), xMinMax.getMax());
        }
    }


    public void autoScaleX() {
        if (!isValid) {
            return;
        }
        for (int i = 0; i < chart.xAxesCount(); i++) {
            autoScaleChartX(i);
        }
        autoScaleNavigatorX();
    }

    public double getChartXMin(int xAxisNumber) {
        return chart.getXMin(xAxisNumber);
    }

    public double getChartXMax(int xAxisNumber) {
        return chart.getXMax(xAxisNumber);
    }


    /**
     * ==================================================*
     * Base methods to interact                          *
     * ==================================================*
     */

    protected void configure() {
        boolean scrollCreated = false;
        int viewportExtent = getXLength();
        for (int i = 0; i < chart.xAxesCount(); i++) {
            Scroll scroll = createScroll(i, viewportExtent);
            if (scroll != null) {
                scrollCreated = true;
            }
        }
        boolean scrollsAtTheEnd = true;
        if(!scrollCreated || !scrollsAtEnd) {
            for (Integer xAxisNumber : axisToScrolls.keySet()) {
                Scroll scroll = axisToScrolls.get(xAxisNumber);
                if (scroll != null && !scroll.isViewportAtTheEnd()) {
                    scrollsAtTheEnd = false;
                }
            }
        }

        autoScaleNavigatorX();
        if (scrollsAtTheEnd) {
            for (Integer xAxisNumber : axisToScrolls.keySet()) {
                Scroll scroll = axisToScrolls.get(xAxisNumber);
                if (scroll != null) {
                    scroll.setViewportAtTheEnd();
                }
            }
        }
        setChartTraceData();
        setNavigatorTraceData();
        if (scrollCreated) {
            autoScaleChartY();
        }
        autoScaleNavigatorY();
    }

    public void draw(BCanvas canvas) {
        if(width <= 0 || height <= 0) {
            return;
        }
        revalidate(canvas.getRenderContext());
        if (isDataChanged) {
            dataProcessor.dataAppended();
            configure();
            isDataChanged = false;
        }
        setChartTraceData();
        setNavigatorTraceData();
        canvas.setColor(config.getBackgroundColor());
        canvas.fillRect(0, 0, width, height);
        chart.draw(canvas);
        canvas.save();
        navigator.draw(canvas);
        for (int i = 0; i < chart.xAxesCount(); i++) {
            Scroll scroll = axisToScrolls.get(i);
            if (scroll != null) {
                navigator.drawRect(canvas, navDefaultXAxisNumber, scroll.getViewportMin(), scroll.getViewportMax(), scroll.getColor(), scroll.getBorder());
            }
        }
    }

    public void setSize(int width, int height) throws IllegalArgumentException {
        this.width = width;
        this.height = height;
        invalidate();
    }

    public void setTitle(String title) {
        chart.setTitle(title);
    }

    public void dataAppended() {
        isDataChanged = true;
    }


    /**
     * =======================Base methods to interact with chart ==========================
     **/

    public void autoScaleChartY(int yAxisNumber) {
        chart.autoScaleY(yAxisNumber);
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


    public void addChartTrace(String name, XYSeries data, TracePainter tracePainter) {
        addChartTrace(name, data, tracePainter, false, false);
    }


    public void addChartTrace(String name, XYSeries data, TracePainter tracePainter, boolean isXOpposite, boolean isYOpposite) {
        chart.addTrace(name, data, tracePainter, isXOpposite, isYOpposite);
        dataProcessor.addChartTraceData(data, chart.getTraceMarkSize(chart.traceCount() - 1));
        isDataChanged = true;
    }

    public int chartTraceCount() {
        return chart.traceCount();
    }

    public void setChartXTitle(int xAxisNumber, String title) {
        chart.setXTitle(xAxisNumber, title);
    }

    public void setChartYTitle(int yAxisNumber, String title) {
        chart.setYTitle(yAxisNumber, title);
    }

    public void setChartXPrefixAndSuffix(int xAxisNumber, @Nullable String prefix, @Nullable String suffix) {
        navigator.setXPrefixAndSuffix(xAxisNumber, prefix, suffix);
    }

    public void setChartYPrefixAndSuffix(int yAxisNumber, @Nullable String prefix, @Nullable String suffix) {
        navigator.setYPrefixAndSuffix(yAxisNumber, prefix, suffix);
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

    public void addNavigatorTrace(String name, XYSeries data, TracePainter tracePainter) {
        addNavigatorTrace(name, data, tracePainter, false);
    }

    public void addNavigatorTrace(String name, XYSeries data, TracePainter tracePainter, boolean isYOpposite) {
        navigator.addTrace(name, data, tracePainter, false, isYOpposite);
        dataProcessor.addNavigatorTraceData(data, navigator.getTraceMarkSize(navigator.traceCount() -1));
        isDataChanged = true;
    }

    public int navigatorTraceCount() {
        return navigator.traceCount();
    }

    public void setNavigatorXTitle(int xAxisNumber, String title) {
        navigator.setXTitle(xAxisNumber, title);
    }

    public void setNavigatorYTitle(int yAxisNumber, String title) {
        navigator.setYTitle(yAxisNumber, title);
    }

    public void setNavigatorXPrefixAndSuffix(int xAxisNumber, @Nullable String prefix, @Nullable String suffix) {
        navigator.setXPrefixAndSuffix(xAxisNumber, prefix, suffix);
    }

    public void setNavigatorYPrefixAndSuffix(int yAxisNumber, @Nullable String prefix, @Nullable String suffix) {
        navigator.setYPrefixAndSuffix(yAxisNumber, prefix, suffix);
    }

    public void autoScaleNavigatorY(int yAxisNumber) {
        navigator.autoScaleY(yAxisNumber);
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

    List<Integer> getChartXAxisNumbersUsedByStack(int stack) {
        return chart.getXAxesNumbersUsedByStack(stack);
    }

    List<Integer> getChartYAxisNumbersUsedByStack(int stack) {
        return chart.getYAxesNumbersUsedByStack(stack);
    }


    int getChartTraceXAxisNumber(int traceNumber) {
        return chart.getTraceXAxisNumber(traceNumber);
    }

    int getChartTraceYAxisNumber(int traceNumber) {
        return chart.getTraceYAxisNumber(traceNumber);
    }

    List<Integer> getNavigatorYAxisNumbersUsedByStack(int stack) {
        return navigator.getYAxesNumbersUsedByStack(stack);
    }

    int getNavigatorTraceYAxisNumber(int traceNumber) {
        return navigator.getTraceYAxisNumber(traceNumber);
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

    int scrollContain(int x, int y) {
        if (!navigator.getBounds().contain(x, y)) {
            return -1;
        }
        double xValue = navigator.positionToValue(navDefaultXAxisNumber, x);
        double viewportMinRange = Double.MAX_VALUE;
        int scrollXAxisNumber = -1;
        for (Integer xAxisNumber : axisToScrolls.keySet()) {
            Scroll scroll = axisToScrolls.get(xAxisNumber);
            if (scroll.viewportContainValue(xValue)) {
                double viewportRange = scroll.getViewportMax() - scroll.getViewportMin();
                if (viewportMinRange > viewportRange) {
                    scrollXAxisNumber = xAxisNumber;
                    viewportMinRange = viewportRange;
                }
            }
        }
        return scrollXAxisNumber;
    }

    boolean positionScrolls(double x) {
        scrollsChanged = false;
        double xValue = navigator.positionToValue(navDefaultXAxisNumber, x);
        for (Integer xAxisNumber : axisToScrolls.keySet()) {
            axisToScrolls.get(xAxisNumber).setViewportCenterValue(xValue);
        }
        return scrollsChanged;
    }

    boolean positionChartX(int xAxisNumber, double x) {
        scrollsChanged = false;
        double xValue = chart.positionToValue(xAxisNumber, x);
        for (Integer xNumber : axisToScrolls.keySet()) {
            axisToScrolls.get(xNumber).setViewportCenterValue(xValue);
        }
        return scrollsChanged;
    }

    boolean translateChartX(int xAxisNumber, double dx) {
        scrollsChanged = false;
        Scroll scroll = axisToScrolls.get(xAxisNumber);
        if (scroll != null) {
            double centerValue = scroll.getViewportCenterValue();
            double centerValueNew = chart.positionToValue(xAxisNumber, chart.valueToPosition(xAxisNumber, centerValue) + dx);
            scroll.setViewportCenterValue(centerValueNew);
            centerValueNew = scroll.getViewportCenterValue();
            if (centerValueNew != centerValue) {
                for (Integer xNumber : axisToScrolls.keySet()) {
                    if (xNumber != xAxisNumber) {
                        axisToScrolls.get(xNumber).setViewportCenterValue(centerValueNew);
                    }
                }
            }
        }
        return scrollsChanged;
    }

    boolean translateScrolls(int xAxisNumber, double dx) {
        scrollsChanged = false;
        Scroll scroll = axisToScrolls.get(xAxisNumber);
        if (scroll != null) {
            double centerValue = scroll.getViewportCenterValue();
            double centerValueNew = navigator.positionToValue(navDefaultXAxisNumber, navigator.valueToPosition(navDefaultXAxisNumber, centerValue) + dx);
            scroll.setViewportCenterValue(centerValueNew);
            centerValueNew = scroll.getViewportCenterValue();
            if (centerValueNew != centerValue) {
                for (Integer xNumber : axisToScrolls.keySet()) {
                    if (xNumber != xAxisNumber) {
                        axisToScrolls.get(xNumber).setViewportCenterValue(centerValueNew);
                    }
                }
            }
        }
        return scrollsChanged;
    }

    boolean zoomChartX(int xAxisNumber, double zoomFactor, int anchorPoint) {
        scrollsChanged = false;
        Scroll scroll = axisToScrolls.get(xAxisNumber);
        if (scroll != null) {
            scroll.zoom(zoomFactor, anchorPoint);
        }
        return scrollsChanged;
    }

    boolean zoomChartY(int yAxisNumber, double zoomFactor, int anchorPoint) {
        return chart.zoomY(yAxisNumber, zoomFactor, anchorPoint);
    }

    boolean translateChartY(int yAxisNumber, int dy) {
        return chart.translateY(yAxisNumber, dy);
    }

    boolean zoomNavigatorY(int yAxisNumber, double zoomFactor, int anchorPoint) {
        return navigator.zoomY(yAxisNumber, zoomFactor, anchorPoint);
    }

    boolean translateNavigatorY(int yAxisNumber, int dy) {
        return navigator.translateY(yAxisNumber, dy);
    }
}
