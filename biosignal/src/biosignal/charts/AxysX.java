package biosignal.charts;

import java.awt.*;

class AxysX extends Axis {

    @Override
    void draw(Graphics g, int position) {
        int tickLineSize = 10;
        int tickTextSizeX = 10;
        int tickTextSizeY = 20;

        int tickEnd = position + tickLineSize;

        g.setColor(Color.red);
        g.drawLine(start, position, end, position);
        g.setColor(Color.black);
        g.drawLine(start, position, start, tickEnd);
        g.drawLine(end, position, end, tickEnd);

        int fontSize = 12;
        g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, fontSize));
        g.drawString("" + min, start - tickTextSizeX, position + tickTextSizeY);
        g.drawString("" + max, end - tickTextSizeX, position + tickTextSizeY);
    }
}
