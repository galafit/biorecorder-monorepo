package com.biorecorder.bichart.axis;

import com.biorecorder.bichart.graphics.*;

/**
 * Created by galafit on 29/8/18.
 */
abstract class HorizontalOrientationFunctions implements OrientationFunctions {
    @Override
    public int labelSizeForWidth(TextMetric tm,  String label) {
        return tm.height();
    }

    @Override
    public BText createTickLabel(TextMetric tm, int tickPosition, String tickLabel, int start, int end, int tickPixelInterval, AxisConfig config, int interLabelGap, boolean isCategory) {
        int charSize = tm.stringWidth("0");
        int space = 2;// px
        int charHalfWidth = charSize / 2;
        int labelSize = tm.stringWidth(tickLabel);
        TextAnchor labelVAnchor = getLabelVTextAnchor(config);
        int y = getLabelY(config);
        int x;
        int labelShift = -charHalfWidth;
        if(config.isTickLabelCentered() || isCategory) {
            labelShift = -(labelSize / 2);
        }
        if (config.isTickLabelOutside()) {
            x = tickPosition + labelShift;
            if (x < start) {
                int x1 = tickPosition + tickPixelInterval -  labelSize - interLabelGap + labelShift;
                int x2 = start;
                x = Math.max(x + labelShift, Math.min(x1, x2));
            }

            if (x + labelSize > end) {
                int x1 = tickPosition - tickPixelInterval + labelShift + 2 * labelSize + interLabelGap;
                int x2 =  end;
                x = Math.min(x + labelSize, Math.max(x1, x2));
                return new BText(tickLabel, x, y, TextAnchor.END, labelVAnchor, tm);
            }
        } else {
            x = tickPosition + space;
        }


        return new BText(tickLabel, x, y, TextAnchor.START, labelVAnchor, tm);

    }

    @Override
    public int labelSizeForOverlap(TextMetric tm, String label) {
        return tm.stringWidth(label);
    }

    @Override
    public BLine createAxisLine(int start, int end) {
        return new BLine(start, 0, end, 0);
    }

    @Override
    public boolean contains(int point, int start, int end) {
        return point <= end && point >= start;
    }

    @Override
    public boolean isVertical() {
        return false;
    }

    protected abstract int getLabelY(AxisConfig config);

    protected abstract TextAnchor getLabelVTextAnchor(AxisConfig config);
}
