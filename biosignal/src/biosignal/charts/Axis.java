package biosignal.charts;

import java.awt.*;

abstract class Axis {
    int min = 0;   // min, max пределы выводимой величины
    int max = 100;
    int start = 0;   // start, end позиции на панели по оси x в точках x0, x1
    int end = 100;

    void setMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    int[] getMinMax() {
        return new int[]{ min, max };
    }

    void setStartEnd(int start, int end) {
        this.start = start;
        this.end = end;
    }

    int getStart() {
        return start;
    }

    int getEnd() {
        return end;
    }

    int getLength() {
        return Math.abs(end - start);
    }

    int valueToPosition(int value) {
        return (int)(start + 1.0*(value - min) * (end - start) / (max - min));
    }

    int positionToValue(int position) {
        return (min + (position - start) * (max - min) / (end - start));
    }

    abstract void draw(Graphics g, int position);
}
