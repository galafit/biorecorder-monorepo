package biosignal.charts;

import java.awt.*;

class Trace {
    private int[] data = new int[0];
    void setData(int[] data) {
        this.data = data;
    }

    void draw(Graphics g, AxysX aXysX, AxysY aXysY, Color color) {
        if (data.length > 0) {
            g.setColor(color);
            int x0 = aXysX.getStart();
            int y0 = aXysY.getStart();
            int y1;
            int x1;
            int dataLength = data.length;
            for (int i = 0; i < dataLength; ) {
                x1 = aXysX.valueToPosition(i);
                y1 = aXysY.valueToPosition(data[i]);
                i++;
                g.drawLine(x0, y0, x1, y1);
                y0 = y1;
                x0 = x1;
            }
        }
    }
}
