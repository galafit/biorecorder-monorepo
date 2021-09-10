package biosignal.charts;

import java.awt.*;

class AxysY extends Axis {

    @Override
    void draw(Graphics g, int position) {
        int tickLineSize = 10;
        int tickTextSizeX = 50;
        int tickTextSizeY = 5;

        int tickStart = position - tickLineSize;

        g.setColor(Color.red);
        g.drawLine(position, start, position, end);
        g.setColor(Color.black);
        g.drawLine(tickStart, start, position, start);
        g.drawLine(tickStart, end, position, end);

        int fontSize = 12;
        g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, fontSize));
        g.drawString("" + min, position - tickTextSizeX, start + tickTextSizeY);
        g.drawString("" + max, position - tickTextSizeX, end + tickTextSizeY);
    }
}
