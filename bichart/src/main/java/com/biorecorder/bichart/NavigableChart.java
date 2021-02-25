package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
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
    private int width;
    private int height;
    private BRectangle chartArea;
    private BRectangle navigatorArea;
    private List<Scroll1> scrolls = new ArrayList<>(2);

    private boolean isAreasDirty = true;


    public NavigableChart() {
        this(DarkTheme.getNavigableChartConfig());
    }

    public NavigableChart(NavigableChartConfig config) {
        this.config = new NavigableChartConfig(config);
         chart = new Chart(config.getChartConfig());
         navigator = new Chart(config.getNavigatorConfig());
        // create, remove and update scrolls
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            scrolls.add(new Scroll1(chart, navigator, xPosition));
        }
    }

    public boolean isScrollsAtTheEnd() {
        int gap = 5;
        double maxExtent = 0;
        Scroll1 scrollWithMaxExtent = null;
        for (Scroll1 s : scrolls) {
          if(s.isUsed() && s.getExtent() >= maxExtent) {
              scrollWithMaxExtent = s;
              maxExtent = s.getExtent();
          }
        }
       if(scrollWithMaxExtent != null) {
           double max = scrollWithMaxExtent.getMax();
           double scrollEnd = scrollWithMaxExtent.getValue() + scrollWithMaxExtent.getExtent();
           int max_position = (int) navigatorScale(max);
           int scrollEndPosition = (int) navigatorScale(scrollEnd);
           int distance = max_position - scrollEndPosition;
           if (distance > gap) {
               return false;
           } else {
               return true;
           }
       }
       return false;
    }

    private Range getScrollTouchRange(Scroll1 scroll) {
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


    private void calculateAndSetAreas(RenderContext renderContext) {
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
        //  выравниваем маргины
        Insets chartMargin = chart.getMargin(renderContext);
        Insets navigatorMargin = navigator.getMargin(renderContext);
        if(chartMargin.left() != navigatorMargin.left() ||
                chartMargin.right() != navigatorMargin.right()) {
            int leftMargin = Math.min(chartMargin.left(), navigatorMargin.left());
            int rightMargin = Math.min(chartMargin.right(), navigatorMargin.right());
            chart.setMargin(new Insets(chartMargin.top(), rightMargin, chartMargin.bottom(), leftMargin));
            navigator.setMargin(new Insets(navigatorMargin.top(), rightMargin, navigatorMargin.bottom(), leftMargin));
        }
    }


    double navigatorScale(double value) {
        return navigator.scale(XAxisPosition.BOTTOM, value);
    }

    double navigatorInvert(double value) {
        return navigator.invert(XAxisPosition.BOTTOM, value);
    }

    private void drawScroll(BCanvas canvas, Scroll1 scroll) {
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

    boolean isChartContains(int x, int y) {
        return chartArea.contains(x, y);
    }

    boolean isScrollContain(int x, int y) {
        if (!navigatorArea.contains(x, y)) {
            return false;
        }
        for (Scroll1 s : scrolls) {
            if (getScrollTouchRange(s).contains(x)) {
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


    /**==================================================*
     *                Base methods to interact           *
     * ==================================================*/

    public void draw(BCanvas canvas) {
        if (isAreasDirty) {
            calculateAndSetAreas(canvas.getRenderContext());
            isAreasDirty = false;
        }
        canvas.translate(chartArea.x, chartArea.y);
        canvas.setColor(config.getBackgroundColor());
        canvas.fillRect(0, 0, width, height);
        chart.draw(canvas);
        canvas.save();
        canvas.translate(navigatorArea.x, navigatorArea.y);
        navigator.draw(canvas);
        for (Scroll1 s : scrolls) {
            if(s.isUsed()) {
                drawScroll(canvas, s);
            }
        }
        canvas.restore();
    }


    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        isAreasDirty = true;
    }

    public void setConfig(NavigableChartConfig config1) {
        this.config = new NavigableChartConfig(config1);
        chart.setConfig(config.getChartConfig());
        navigator.setConfig(config.getNavigatorConfig());
        scrolls.clear();
        isAreasDirty = true;
    }

    /**
     * @return true if scrollValue was changed and false if newValue = current scroll value
     */
    public boolean setScrollsValue(double newValue) {
        boolean scrollsMoved = false;
        for (Scroll1 s : scrolls) {
            if(s.isUsed()) {
                scrollsMoved = s.setValue(newValue) || scrollsMoved;
            }
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
        double value = navigatorInvert(x);
        for (Scroll1 s : scrolls) {
            if(s.isUsed()) {
                scrollsMoved = s.setValue(value) || scrollsMoved;
            }
        }
        return scrollsMoved;
    }

    public boolean translateScrolls(double dx) {
        boolean isMoved = false;
        for (Scroll1 s : scrolls) {
            if(s.isUsed()) {
                double currentX = navigatorInvert(s.getValue());
                double newScrollValue = navigatorScale(currentX + dx);
                isMoved = s.setValue(newScrollValue) || isMoved;
            }
        }
        return isMoved;
    }

    public XAxisPosition getChartSelectedTraceX() {
        return chart.getSelectedTraceX();
    }

    public boolean zoomScrollExtent(XAxisPosition xAxisPosition, double zoomFactor) {
        for (Scroll1 s : scrolls) {
            if(s.isUsed() && s.getChartXPosition() == xAxisPosition) {
                return s.setExtent(s.getExtent() * zoomFactor);
            }
        }
        return false;
    }

    public int getWidth() {
        return width;
    }

    public double getScrollWidth(XAxisPosition xAxisPosition) throws IllegalArgumentException {
        for (Scroll1 s : scrolls) {
            if(s.isUsed() && s.getChartXPosition() == xAxisPosition) {
                double scrollStart = navigatorScale(s.getValue());
                double scrollEnd = navigatorScale(s.getValue() + s.getExtent());
                return scrollEnd - scrollStart;
            }
        }
        String errMsg = "X axis: " + xAxisPosition + " does not have scroll";
        throw new IllegalArgumentException(errMsg);
    }

    public void autoScaleScrollExtent() {
        for (Scroll1 s : scrolls) {
            if(s.isUsed()) {
                s.autoScale();
            }
        }
    }

    public void autoScaleSelectedTraceXScrollExtent() {
        chart.getSelectedTraceXBestExtent();

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
        isAreasDirty = true;
    }

    public void addChartStack() {
        chart.addStack();
        isAreasDirty = true;
    }

    public void setChartStackWeight(int stack, int weight) {
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
    }


    public void addChartTrace(ChartData data, TracePainter tracePainter, XAxisPosition xPosition, YAxisPosition yPosition) {
        chart.addTrace(data,  tracePainter, xPosition, yPosition);
        isAreasDirty = true;
    }

    public void addChartTrace(ChartData data, TracePainter tracePainter, int stackNumber) {
        chart.addTrace(data,  tracePainter, stackNumber);
        isAreasDirty = true;
    }

    public void addChartTrace(ChartData data, TracePainter tracePainter,  int stackNumber, XAxisPosition xPosition, YAxisPosition yPosition) {
        chart.addTrace(data,  tracePainter, stackNumber, xPosition, yPosition);
        isAreasDirty = true;
    }

    public void removeChartTrace(int traceNumber) {
        chart.removeTrace(traceNumber);
        isAreasDirty = true;
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

    public boolean isNavigatorContains(BPoint point) {
        if (point != null && navigatorArea.contains(point.getX(), point.getY())) {
            return true;
        }
        return false;
    }

    public void zoomNavigatorY(int stack, YAxisPosition yPosition, double zoomFactor) {
        navigator.zoomY(stack, yPosition, zoomFactor);
    }

    public void zoomNavigatorSelectedTraceY(double zoomFactor) {
        navigator.zoomSelectedTraceY(zoomFactor);
    }

    public void zoomNavigatorX(XAxisPosition xPosition, double zoomFactor) {
        navigator.zoomX(xPosition, zoomFactor);
    }

    public void zoomNavigatorSelectedTraceX(double zoomFactor) {
        navigator.zoomSelectedTraceX(zoomFactor);
    }

    public void translateNavigatorY(int stack, YAxisPosition yPosition, int dy) {
        navigator.translateY(stack, yPosition, dy);
    }

    public void translateNavigatorSelectedTraceY(int dy) {
        navigator.translateSelectedTraceY(dy);
    }

    public void translateNavigatorX(XAxisPosition xPosition, int dy) {
        navigator.translateX(xPosition, dy);
    }

    public void translateNavigatorSelectedTraceX(int dy) {
        navigator.translateSelectedTraceX(dy);
    }
    public int navigatorStackCount() {
        return navigator.stackCount();
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
    }

    public void addNavigatorTrace(ChartData data, TracePainter tracePainter, int stack) {
        navigator.addTrace(data,  tracePainter, stack);
        isAreasDirty = true;
    }

    public void removeNavigatorTrace(int traceNumber) {
        navigator.removeTrace(traceNumber);
        isAreasDirty = true;
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
