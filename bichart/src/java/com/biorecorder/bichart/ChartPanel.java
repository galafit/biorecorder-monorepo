package com.biorecorder.bichart;

import com.biorecorder.bichart.swing.SwingCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChartPanel extends JPanel {
    Interactive interactive;

    final int scrollPointsPerRotation = 10;
    // во сколько раз растягивается или сжимается ось при автозуме
    private double defaultZoom = 2;
    private int pastX;
    private int pastY;
    private int pressedX;
    private int pressedY;

    public ChartPanel(Chart chart) {
        interactive = new InteractiveChart(chart);
        init();
    }

    public ChartPanel(BiChart chart) {
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
                        if(interactive.translateY(pressedX, pressedY, dy)) {
                            repaint();
                        }
                    } else {
                        if(interactive.scrollContain(pastX, pastY) && interactive.translateScroll(dx)) {
                            repaint();
                        } else if(interactive.translateX(pressedX, pressedY, dx)) {
                            repaint();
                        }
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isAltDown() || e.isControlDown() || e.isMetaDown()) {
                    interactive.autoScaleX();
                    interactive.autoScaleY();
                    repaint();
                } else  {
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
                int d = e.getUnitsToScroll(); //e.getWheelRotation() * scrollPointsPerRotation;
                if (e.isAltDown()
                        || e.isControlDown()
                        //    || e.isShiftDown() // JAVA BUG on MAC!!!!
                        || e.isMetaDown()) { // scaleY
                    if(interactive.scaleY(e.getX(), e.getY(), distanceToScaleFactor(d))) {
                        repaint();
                    }

                } else { // scale X
                    if (interactive.scaleX(e.getX(), e.getY(), distanceToScaleFactor(d))) {
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
