package com.biorecorder.bichart;

import com.biorecorder.bichart.swing.SwingCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChartPanel1 extends JPanel {
    Interactive interactive;

    final int scrollPointsPerRotation = 10;
    // во сколько раз растягивается или сжимается ось при автозуме
    private double defaultZoom = 2;
    private int pastX;
    private int pastY;
    private int pressedX;
    private int pressedY;
    private boolean isScrollMoving;

    public ChartPanel1(Chart chart) {
        interactive = new InteractiveChart(chart);
        init();
    }

    public ChartPanel1(BiChart chart) {
        interactive = new InteractiveBiChart(chart);
        init();
    }

    private void init() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if(interactive.hoverOn(e.getX(), e.getY())) {
                        repaint();
                    }
                } else {
                    int dy = pastY - e.getY();
                    int dx = pastX - e.getX();

                    pastX = e.getX();
                    pastY = e.getY();

                    if (e.isAltDown() // zoom Y
                            || e.isControlDown()
                            // || e.isShiftDown()
                            || e.isMetaDown()) {
                        if(interactive.scaleY(pressedX, pressedY, distanceToScaleFactor(dy))) {
                            repaint();
                        }
                    } else {
                        if(interactive.scrollContain(pastX, pastY) && interactive.translateScroll(dx)) {
                            repaint();
                        } else if(interactive.translateY(pressedX, pressedY, dy)) {
                            repaint();
                        }
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // double click
                    interactive.autoScaleX();
                    interactive.autoScaleY();
                    repaint();
                }
                if (e.getClickCount() == 1) { // single click
                    if(interactive.switchTraceSelection(e.getX(), e.getY())) {
                        repaint();
                    } else if(interactive.setScrollPosition(e.getX(), e.getY())) {
                        repaint();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if(interactive.hoverOn(e.getX(), e.getY())) {
                        repaint();
                    }
                } else {
                    pressedX = e.getX();
                    pressedY = e.getY();
                    pastX = e.getX();
                    pastY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(interactive.hoverOff()) {
                    repaint();
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                e.consume(); // avoid the event to be triggered twice
                int dx = e.getWheelRotation() * scrollPointsPerRotation;
                dx = e.getUnitsToScroll();
                if (e.isAltDown()
                        || e.isControlDown()
                        //    || e.isShiftDown() // JAVA BUG on MAC!!!!
                        || e.isMetaDown()) { // scaleX
                    if(interactive.scaleX(e.getX(), e.getY(), distanceToScaleFactor(dx))) {
                        repaint();
                    }

                } else { // translates X
                    if (interactive.translateX(e.getX(), e.getY(), dx)) {
                        repaint();
                    }
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                interactive.resize(getWidth(), getHeight());
                repaint();
            }
        });
    }

    private double distanceToScaleFactor(int distance) {
        return 1 + defaultZoom * distance / 100;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        interactive.draw(new SwingCanvas((Graphics2D) g));
    }

    public KeyListener getKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int dx = 0;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    dx = 1;
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    dx = -1;

                }
                if (interactive.translateScroll(dx)) {
                    repaint();
                }
            }
        };
    }
}
