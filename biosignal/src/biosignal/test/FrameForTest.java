package biosignal.test;

import javax.swing.*;
import java.awt.*;

public class FrameForTest extends JFrame {
    public FrameForTest(String title, JPanel p,
                        int xStart, int yStart, int widthStart, int HeightStart) {
        super(title);
        setLocation(xStart, yStart);
        setContentPane(p);
        p.setPreferredSize(new Dimension(widthStart, HeightStart));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
}
