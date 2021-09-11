package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.chart.InteractiveDrawable;
import com.biorecorder.bichart.chart.SmartChart;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.swing.SwingCanvas;
import com.biorecorder.bichart.swing.SwingRenderContext;
import com.biorecorder.bichart.traces.TracePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class ChartPanel extends JPanel implements KeyListener {
    final int scrollPointsPerRotation = 10;
    // во сколько раз растягивается или сжимается ось при автозуме
    private double defaultZoom = 2;
    private BPoint pressPoint;
    private int pastX;
    private int pastY;
    private boolean isXDirection;
    private boolean isYDirection;

    private SmartChart chart;

    public ChartPanel(boolean isDateTime) {
        chart = new SmartChart(isDateTime);
        init();
    }

    public void setChartTraceData(int traceNumber, XYSeries data) {
        chart.setChartTraceData(traceNumber, data);
    }

    public void setNavigatorTraceData(int traceNumber, XYSeries data) {
        chart.setNavigatorTraceData(traceNumber, data);
    }

    public void appendNavigatorTraceData(int traceNumber, XYSeries dataToAppend) {
        chart.appendNavigatorTraceData(traceNumber, dataToAppend);
    }

    public void autoScaleX() {
        chart.autoScaleX();
    }

    public void autoScaleX(XAxisPosition xPosition) {
        chart.autoScaleX(xPosition);
    }

    public void setChartYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        chart.setChartYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleChartY() {
        chart.autoScaleChartY();
    }

    public void addChartStack() {
        chart.addChartStack();
    }

    public void addChartTrace(String name, XYSeries data, TracePainter tracePainter) {
        chart.addChartTrace(name, data, tracePainter);
    }

    public Range getChartXRange() {
        return chart.getChartXMinMax(XAxisPosition.TOP);
    }

    public void addNavigatorStack() {
        chart.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYSeries data, TracePainter tracePainter) {
        chart.addNavigatorTrace(name, data, tracePainter);
    }

    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        chart.setNavigatorYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleNavigatorY() {
        chart.autoScaleNavigatorY();
    }

    private void init() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if(chart.onLongPress(e.getX(), e.getY())) {
                        repaint();
                    }
                } else {
                    int dy = pastY - e.getY();
                    int dx = pastX - e.getX();

                    pastX = e.getX();
                    pastY = e.getY();
                    if(!isXDirection && !isYDirection) {
                        if(Math.abs(dy) >= Math.abs(dx)) {
                            isYDirection = true;
                        } else {
                            isXDirection = true;
                        }
                    }

                    if (e.isAltDown()
                            || e.isControlDown()
                            // || e.isShiftDown()
                            || e.isMetaDown()) { // zoom

                        if(chart.onScaleY(pressPoint, distanceToScaleFactor(dy))) {
                            repaint();
                        }
                    } else { // scroll
                        if(isYDirection) {
                            if(chart.onScrollY(pressPoint, dy)) {
                                repaint();
                            }
                        } else {
                            if(chart.onScrollX(pressPoint, dx)) {
                                repaint();
                            }
                        }
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if(chart.onDoubleTap(e.getX(), e.getY())) {
                        repaint();
                    }
                }
                if (e.getClickCount() == 1) {
                    if(chart.onTap(e.getX(), e.getY())) {
                        repaint();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if(chart.onLongPress(e.getX(), e.getY())) {
                        repaint();
                    }
                } else {
                    pastX = e.getX();
                    pastY = e.getY();
                    pressPoint = new BPoint(e.getX(), e.getY());
                    isXDirection = false;
                    isYDirection = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressPoint = null;
                if(chart.onTapUp(e.getX(), e.getY())) {
                    repaint();
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                e.consume(); // avoid the event to be triggered twice
                int dx = e.getWheelRotation() * scrollPointsPerRotation;
                if (e.isAltDown()
                        || e.isControlDown()
                        //    || e.isShiftDown() // JAVA BUG on MAC!!!!
                        || e.isMetaDown()) { // scaleX
                    if(chart.onScaleX(null, distanceToScaleFactor(dx))) {
                        repaint();
                    }

                } else { // translateScrolls X
                    if (chart.onScrollX(null, dx)) {
                        repaint();
                    }
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                chart.onResize(getWidth(), getHeight());
                chart.update(new SwingRenderContext());
                repaint();
            }
        });
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        chart.onResize(preferredSize.width, preferredSize.height);
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int dx = 0;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            dx = scrollPointsPerRotation;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            dx = -scrollPointsPerRotation;

        }
        if (chart.onScrollX(null, dx)) {
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private double distanceToScaleFactor(int distance) {
        return 1 + defaultZoom * distance / 100;
    }

    public void update() {
        if (chart.update(new SwingRenderContext())) {
            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        chart.draw(new SwingCanvas((Graphics2D) g));
    }
}
