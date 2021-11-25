package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.Orientation;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;

import java.util.ArrayList;
import java.util.List;

public class InteractiveBiChart implements Interactive {
    private final BiChart biChart;
    private boolean chartContain;
    private boolean scrollContain;
    private List<Integer> yAxesNumbers = new ArrayList<>(2);
    private List<Integer> xAxesNumbers = new ArrayList<>(2);;
    private int stack;
    private boolean released = true;

    public InteractiveBiChart(BiChart biChart) {
        this.biChart = biChart;
    }

    private void getPositions(int x, int y) {
        System.out.println("get pos");
        xAxesNumbers.clear();
        yAxesNumbers.clear();
        scrollContain = false;
        if(biChart.chartContain(x, y)) {
            chartContain = true;
            int selection =biChart.getChartSelectedTrace();
            if (selection >= 0) {
                yAxesNumbers.add(biChart.getChartTraceYAxisNumber(selection));
                xAxesNumbers.add(biChart.getChartTraceXAxisNumber(selection));
            } else {
                stack = biChart.getChartStackContaining(x, y);
                if(stack >= 0) {
                    xAxesNumbers = biChart.getChartXAxisNumbersUsedByStack(stack);
                    yAxesNumbers = biChart.getChartYAxisNumbersUsedByStack(stack);
                }
            }
        } else{
            chartContain = false;
            System.out.println("nav contain");
            int selection =biChart.getNavigatorSelectedTrace();
            if (selection >= 0) {
                yAxesNumbers.add(biChart.getNavigatorTraceYAxisNumber(selection));
            } else {
                stack = biChart.getNavigatorStackContaining(x, y);
                if(stack >= 0) {
                    yAxesNumbers = biChart.getNavigatorYAxisNumbersUsedByStack(stack);
                }
                int xAxisNumber = biChart.scrollContain(x, y);
                if(xAxisNumber > 0) {
                    xAxesNumbers.add(xAxisNumber);
                    scrollContain = true;
                }
            }
        }
        released = false;
    }


    @Override
    public boolean translateX(int x, int y, int dx) {
        if(released) {
            getPositions(x, y);
        }
        if(xAxesNumbers.size() > 0) {
            if(chartContain) {
                return biChart.translateChartX(xAxesNumbers.get(0), dx);
            }
            if(scrollContain) {
                return biChart.translateScrolls(xAxesNumbers.get(0), -dx);
            }
        }
        return false;
    }

    @Override
    public boolean translateY(int x, int y, int dy) {
        if (dy == 0) {
            return false;
        }
        if(released) {
            getPositions(x, y);
        }
        boolean isChanged = false;
        if(chartContain) {
            for (int i = 0; i < yAxesNumbers.size(); i++) {
                isChanged = biChart.translateChartY(yAxesNumbers.get(i), dy) || isChanged;
            }
        } else {
            for (int i = 0; i < yAxesNumbers.size(); i++) {
                isChanged = biChart.translateNavigatorY(yAxesNumbers.get(i), dy) || isChanged;
            }
        }
        return isChanged;
    }

    @Override
    public boolean scaleX(int x, int y, double scaleFactor) {
        if (scaleFactor == 0) {
            return false;
        }
        if(released) {
            getPositions(x, y);
        }
        if(chartContain) {
            boolean isChanged = false;
            for (int i = 0; i < xAxesNumbers.size(); i++) {
                isChanged = biChart.zoomChartX(xAxesNumbers.get(i), scaleFactor, x) || isChanged;
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
        if(released) {
            getPositions(x, y);
        }
        boolean isChanged = false;
        if(chartContain) {
            for (int i = 0; i < yAxesNumbers.size(); i++) {
                isChanged = biChart.zoomChartY(yAxesNumbers.get(i), scaleFactor, y) || isChanged;
            }
        } else {
            for (int i = 0; i < yAxesNumbers.size(); i++) {
                isChanged = biChart.zoomNavigatorY(yAxesNumbers.get(i), scaleFactor, y) || isChanged;
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
    public boolean centerX(int x, int y) {
        if(biChart.navigatorContain(x, y)) {
            return biChart.positionScrolls(x);
        } else {
            xAxesNumbers.clear();
            int selection =biChart.getChartSelectedTrace();
            if (selection >= 0) {
                xAxesNumbers.add(biChart.getChartTraceXAxisNumber(selection));
            } else {
                stack = biChart.getChartStackContaining(x, y);
                if(stack >= 0) {
                    xAxesNumbers = biChart.getChartXAxisNumbersUsedByStack(stack);
                }
            }
            if(xAxesNumbers.size() > 0) {
                return biChart.positionChartX(xAxesNumbers.get(0), x);
            }
        }
        return false;
    }

    @Override
    public void release() {
        released = true;
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
}

