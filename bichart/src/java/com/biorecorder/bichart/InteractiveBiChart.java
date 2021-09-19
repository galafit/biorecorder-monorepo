package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;

import java.util.ArrayList;
import java.util.List;

public class InteractiveBiChart implements Interactive {
    private final BiChart biChart;

    public InteractiveBiChart(BiChart biChart) {
        this.biChart = biChart;
    }

    private YAxisInfo getYPositions(int x, int y) {
        List<YAxisPosition> yPositions = new ArrayList<>(1);
        int stack;
        if(biChart.chartContain(x, y)) {
            int selection =biChart.getChartSelectedTrace();
            if (selection >= 0) {
                yPositions.add(biChart.getChartTraceYPosition(selection));
                stack = biChart.getChartTraceStack(selection);
            } else {
                stack = biChart.getChartStackContaining(x, y);
                yPositions = biChart.getChartYPositionsUsedByStack(stack);
            }
            return new YAxisInfo(true, stack, yPositions);
        } else {
            int selection =biChart.getNavigatorSelectedTrace();
            if (selection >= 0) {
                yPositions.add(biChart.getNavigatorTraceYPosition(selection));
                stack = biChart.getNavigatorTraceStack(selection);
            } else {
                stack = biChart.getNavigatorStackContaining(x, y);
                yPositions = biChart.getNavigatorYPositionsUsedByStack(stack);
            }
            return new YAxisInfo(false, stack, yPositions);
        }
    }

    private List<XAxisPosition> getChartXPositions(int x, int y) {
        List<XAxisPosition> xPositions;
        int selection =biChart.getChartSelectedTrace();
        int stack;
        xPositions = new ArrayList();
        if (selection >= 0) {
            xPositions.add(biChart.getChartTraceXPosition(selection));
        } else {
            stack = biChart.getChartStackContaining(x, y);
            if(stack >= 0) {
                xPositions = biChart.getChartXPositionsUsedByStack(stack);
            }
        }
        return xPositions;
    }


    @Override
    public boolean translateX(int x, int y, int dx) {
        if(biChart.chartContain(x, y)) {
            List<XAxisPosition> xPositions = getChartXPositions(x, y);
            if(xPositions.size() > 0) {
                return biChart.translateScrollsViewport(xPositions.get(0), dx);
            }
        }
        return false;
    }

    @Override
    public boolean translateY(int x, int y, int dy) {
        if (dy == 0) {
            return false;
        }
        YAxisInfo yAxisInfo = getYPositions(x, y);
        int stack = yAxisInfo.getStack();
        boolean isChanged = false;
        if(yAxisInfo.isChart) {
            for (YAxisPosition yPosition : yAxisInfo.getYPositions()) {
                isChanged = biChart.translateChartY(stack, yPosition, dy) || isChanged;
            }
        } else {
            for (YAxisPosition yPosition : yAxisInfo.getYPositions()) {
                isChanged = biChart.translateNavigatorY(stack, yPosition, dy) || isChanged;
            }
        }
        return isChanged;
    }

    @Override
    public boolean scaleX(int x, int y, double scaleFactor) {
        if (scaleFactor == 0) {
            return false;
        }
        if(biChart.chartContain(x, y)) {
            boolean isChanged = false;
            List<XAxisPosition> xPositions = getChartXPositions(x, y);
            for (XAxisPosition xPosition : xPositions) {
                isChanged = biChart.zoomScrollExtent(xPosition, scaleFactor) || isChanged;
            }
            return isChanged;
        }
        return false;
    }

    @Override
    public boolean scaleY(int x, int y, double scaleFactor) {
        if (scaleFactor == 0) {
            return false;
        }
        YAxisInfo yAxisInfo = getYPositions(x, y);
        int stack = yAxisInfo.getStack();
        boolean isChanged = false;
        if(yAxisInfo.isChart) {
            for (YAxisPosition yPosition : yAxisInfo.getYPositions()) {
                isChanged = biChart.zoomChartY(stack, yPosition, scaleFactor) || isChanged;
            }
        } else {
            for (YAxisPosition yPosition : yAxisInfo.getYPositions()) {
                isChanged = biChart.zoomNavigatorY(stack, yPosition, scaleFactor) || isChanged;
            }
        }
        return isChanged;
    }

    @Override
    public boolean switchTraceSelection(int x, int y) {
        if(biChart.chartContain(x, y)) {
            int selection = biChart.getChartSelectedTrace();
            int newSelection = biChart.getChartLegendButtonContaining(x, y);
            if(newSelection < 0) {
                return false;
            } else {
                if(selection == newSelection) {
                    biChart.removeChartTraceSelection();
                } else {
                    biChart.selectChartTrace(newSelection);
                }
                return true;
            }
        } else {
            int selection = biChart.getNavigatorSelectedTrace();
            int newSelection = biChart.getNavigatorLegendButtonContaining(x, y);
            if(newSelection < 0) {
                return false;
            } else {
                if(selection == newSelection) {
                    biChart.removeNavigatorTraceSelection();
                } else {
                    biChart.selectNavigatorTrace(newSelection);
                }
                return true;
            }
        }
    }

    @Override
    public boolean scrollContain(int x, int y) {
        return biChart.scrollContain(x, y);
    }

    @Override
    public boolean setScrollPosition(int x, int y) {
        if(biChart.navigatorContain(x, y)) {
            return biChart.setScrollsPosition(x);
        } else {
            List<XAxisPosition> xPositions;
            int selection =biChart.getChartSelectedTrace();
            int stack;
            xPositions = new ArrayList();
            if (selection >= 0) {
                xPositions.add(biChart.getChartTraceXPosition(selection));
            } else {
                stack = biChart.getChartStackContaining(x, y);
                if(stack >= 0) {
                    xPositions = biChart.getChartXPositionsUsedByStack(stack);
                }
            }
            if(xPositions.size() > 0) {
                return biChart.setScrollsViewport(xPositions.get(0), x);
            }
        }
        return false;
    }

    @Override
    public boolean translateScroll(int dx) {
        return biChart.translateScrolls(-dx);
    }

    @Override
    public boolean autoScaleX() {
        biChart.autoScaleX();
        return true;
    }

    @Override
    public boolean autoScaleY() {
        biChart.autoScaleChartY();
        biChart.autoScaleNavigatorY();
        return true;
    }

    @Override
    public boolean hoverOn(int x, int y) {
        if(biChart.chartContain(x, y)){
            TracePoint tracePoint = biChart.getChartNearestPoint(x, y);
            if(tracePoint != null) {
                return biChart.chartHoverOn(tracePoint.getTraceNumber(), tracePoint.getPointIndex());
            } else {
                return biChart.hoverOff();
            }
        } else {
            TracePoint tracePoint = biChart.getNavigatorNearestPoint(x, y);
            if(tracePoint != null) {
                return biChart.navigatorHoverOn(tracePoint.getTraceNumber(), tracePoint.getPointIndex());
            } else {
                return biChart.hoverOff();
            }
        }

    }

    @Override
    public boolean hoverOff() {
        return biChart.hoverOff();
    }

    @Override
    public boolean resize(int width, int height) {
        BRectangle bounds = biChart.getBounds();
        if(width != bounds.width || height != bounds.height) {
            biChart.setSize(width, height);
            return true;
        }
        return false;
    }

    @Override
    public void draw(BCanvas canvas) {
        biChart.draw(canvas);
    }

    private static class YAxisInfo {
        private boolean isChart;
        private List<YAxisPosition> yPositions;
        private int stack;

        public YAxisInfo(boolean isChart, int stack, List<YAxisPosition> yPositions) {
            this.isChart = isChart;
            this.yPositions = yPositions;
            this.stack = stack;
        }

        public List<YAxisPosition> getYPositions() {
            return yPositions;
        }

        public int getStack() {
            return stack;
        }

        public boolean isChart() {
            return isChart;
        }
    }
}
