package biosignal.test;

import javax.swing.*;
import java.awt.*;

public class FiguresTest extends JPanel {
    private static final int X_START = 200; // Левый  угол фрейма от начала экрана
    private static final int Y_START = 500; // Верхий угол фрейма  от начала экрана
    private static final int WIDTH_START = 1000;  // Ширина фрейма
    private static final int HEIGHT_START = 100;  // Высота фрейма

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.red);
        g.drawRect(0, 0, WIDTH_START, HEIGHT_START);
        g.drawLine(0, 0, WIDTH_START, HEIGHT_START);

        g.setColor(Color.green);
        g.drawLine(0, 0, WIDTH_START, HEIGHT_START);

        g.setColor(Color.black);
        g.drawLine(0, HEIGHT_START / 2, WIDTH_START, HEIGHT_START / 2);
        g.drawLine(WIDTH_START / 2, 0, WIDTH_START / 2, HEIGHT_START);
    }

    public static void main(String[] args) {
        new FrameForTest("Frame", new FiguresTest(),
                X_START, Y_START, WIDTH_START, HEIGHT_START);
    }
}
