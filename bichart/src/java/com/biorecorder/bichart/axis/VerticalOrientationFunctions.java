package com.biorecorder.bichart.axis;

import com.biorecorder.bichart.graphics.*;


/**
 * Created by galafit on 29/8/18.
 */
abstract class VerticalOrientationFunctions implements OrientationFunctions {
    @Override
    public int labelSizeForWidth(TextMetric tm,  String label) {
        return tm.stringWidth(label);
    }

    @Override
    public BText createTickLabel(TextMetric tm, int tickPosition, String tickLabel, int start, int end, int tickPixelInterval, AxisConfig config, int interLabelGap, boolean isCategory) {
        int space = 2;// px
        int labelHeight = tm.ascent();
        int x = getLabelX(config);
        TextAnchor labelHAnchor = getLabelHTextAnchor(config);

        if(config.isTickLabelOutside()) {
            int y = tickPosition;
            if(y + labelHeight/2 + 1 > start) {
                y = start - space;
                return new BText(tickLabel, x, y, labelHAnchor, TextAnchor.START, tm);
            }
            if(y - labelHeight/2 - 1 < end) {
                y = end;
                return new BText(tickLabel, x, y, labelHAnchor, TextAnchor.END, tm);
            }
            return new BText(tickLabel, x, y, labelHAnchor, TextAnchor.MIDDLE, tm);

        } else {
            int y = tickPosition;

            if(y - labelHeight - 1 < end) {
                //y = (int)getEnd();
                return new BText(tickLabel, x, y, labelHAnchor, TextAnchor.END , tm);
            }
            y = tickPosition - space;
            return new BText(tickLabel, x, y, labelHAnchor, TextAnchor.START, tm);
        }
    }

    @Override
    public int labelSizeForOverlap(TextMetric tm, String label) {
        return tm.height();
    }

    @Override
    public BLine createAxisLine(int start, int end) {
        return new BLine(0, start, 0, end);
    }

    @Override
    public boolean contains(int point, int start, int end) {
        return point <= start && point >= end;
    }

    @Override
    public boolean isVertical() {
        return true;
    }

    protected abstract int getLabelX(AxisConfig config);
    protected abstract TextAnchor getLabelHTextAnchor(AxisConfig config);
}
