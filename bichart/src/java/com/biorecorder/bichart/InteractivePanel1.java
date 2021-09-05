package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.swing.SwingCanvas;
import com.biorecorder.bichart.swing.SwingRenderContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InteractivePanel1  extends JPanel implements KeyListener {
    Interactive chart;

    final int scrollPointsPerRotation = 10;
    // во сколько раз растягивается или сжимается ось при автозуме
    private double defaultZoom = 2;
    private int pastX;
    private int pastY;
    private boolean isXDirection;
    private boolean isYDirection;

    public InteractivePanel1(Interactive chart) {
        this.chart = chart;
        init();
    }

    private void init() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if(chart.hoverOn(e.getX(), e.getY())) {
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

                        if(chart.scaleY(pastX, pastY, distanceToScaleFactor(dy))) {
                            repaint();
                        }
                    } else { // scroll
                        if(isYDirection) {
                            if(chart.translateY(pastX, pastY, dy)) {
                                repaint();
                            }
                        } else {
                            if(chart.translateX(pastX, pastY, dx)) {
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
                    if(chart.autoScale()) {
                        repaint();
                    }
                }
                if (e.getClickCount() == 1) {
                    if(chart.switchTraceSelection(e.getX(), e.getY())) {
                        repaint();
                    } else if(chart.setScrollPosition(e.getX(), e.getY())) {
                        repaint();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if(chart.hoverOn(e.getX(), e.getY())) {
                        repaint();
                    }
                } else {
                    pastX = e.getX();
                    pastY = e.getY();
                    isXDirection = false;
                    isYDirection = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(chart.hoverOff()) {
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
                    if(chart.scaleX(e.getX(), e.getY(), distanceToScaleFactor(dx))) {
                        repaint();
                    }

                } else { // translateScrolls X
                    if (chart.translateX(e.getX(), e.getY(), dx)) {
                        repaint();
                    }
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                chart.resize(getWidth(), getHeight());
                repaint();
            }
        });
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
        if (chart.translateScroll(dx)) {
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private double distanceToScaleFactor(int distance) {
        return 1 + defaultZoom * distance / 100;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        chart.draw(new SwingCanvas((Graphics2D) g));
    }

}
