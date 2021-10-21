package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;

import java.util.ArrayList;
import java.util.List;

public class InteractiveBiChart implements Interactive {
    private final BiChart biChart;
    private boolean chartContain;
    private boolean scrollContain;
    private List<YAxisPosition> yPositions = new ArrayList<>(2);
    private List<XAxisPosition> xPositions = new ArrayList<>(2);;
    private int stack;
    private boolean released = true;

    public InteractiveBiChart(BiChart biChart) {
        this.biChart = biChart;
    }

    private void getPositions(int x, int y) {
        xPositions.clear();
        yPositions.clear();
        scrollContain = false;
        if(biChart.chartContain(x, y)) {
            chartContain = true;
            int selection =biChart.getChartSelectedTrace();
            if (selection >= 0) {
                yPositions.add(biChart.getChartTraceYPosition(selection));
                xPositions.add(biChart.getChartTraceXPosition(selection));
                stack = biChart.getChartTraceStack(selection);
            } else {
                stack = biChart.getChartStackContaining(x, y);
                if(stack >= 0) {
                    xPositions = biChart.getChartXPositionsUsedByStack(stack);
                    yPositions = biChart.getChartYPositionsUsedByStack(stack);
                }
            }
        } else{
            chartContain = false;
            int selection =biChart.getNavigatorSelectedTrace();
            if (selection >= 0) {
                yPositions.add(biChart.getNavigatorTraceYPosition(selection));
                stack = biChart.getNavigatorTraceStack(selection);
            } else {
                stack = biChart.getNavigatorStackContaining(x, y);
                if(stack >= 0) {
                    yPositions = biChart.getNavigatorYPositionsUsedByStack(stack);
                }
                XAxisPosition xPosition = biChart.scrollContain(x, y);
                if(xPosition != null) {
                    xPositions.add(xPosition);
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
        if(xPositions.size() > 0) {
            if(chartContain) {
                return biChart.translateChartX(xPositions.get(0), dx);
            }
            if(scrollContain) {
                return biChart.translateScrolls(xPositions.get(0), -dx);
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
            for (YAxisPosition yPosition : yPositions) {
                isChanged = biChart.translateChartY(stack, yPosition, dy) || isChanged;
            }
        } else {
            for (YAxisPosition yPosition : yPositions) {
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
        if(released) {
            getPositions(x, y);
        }
        if(chartContain) {
            boolean isChanged = false;
            for (XAxisPosition xPosition : xPositions) {
                isChanged = biChart.zoomChartX(xPosition, scaleFactor, x) || isChanged;
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
            for (YAxisPosition yPosition : yPositions) {
                isChanged = biChart.zoomChartY(stack, yPosition, scaleFactor, y) || isChanged;
            }
        } else {
            for (YAxisPosition yPosition : yPositions) {
                isChanged = biChart.zoomNavigatorY(stack, yPosition, scaleFactor, y) || isChanged;
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
                return biChart.positionChartX(xPositions.get(0), x);
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

