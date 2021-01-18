package com.biorecorder.bichart.axis;

import com.biorecorder.bichart.graphics.BText;
import com.biorecorder.bichart.graphics.TextAnchor;
import com.biorecorder.bichart.graphics.TextMetric;
import com.biorecorder.bichart.graphics.*;

/**
 * Created by galafit on 30/8/18.
 */
class TopOrientation extends HorizontalOrientation {
    @Override
    protected int getLabelY(AxisConfig config) {
        if(config.isTickLabelOutside()) {
            return  -config.getAxisLineWidth() / 2 - config.getTickMarkOutsideSize() - config.getTickPadding();
        } else {
            return config.getAxisLineWidth() / 2  + config.getTickPadding();
        }
    }

    @Override
    protected TextAnchor getLabelVTextAnchor(AxisConfig config) {
        if(config.isTickLabelOutside()) {
            return TextAnchor.START;
        } else {
            return TextAnchor.END;
        }
    }

    @Override
    public BLine createTickLine(int tickPosition, int axisLineWidth, int insideSize, int outsideSize) {
        int x = tickPosition;
        int y1 = -axisLineWidth / 2 - outsideSize;
        int y2 = axisLineWidth / 2 + insideSize;
        return new BLine(x, y1, x, y2);
    }

    @Override
    public BLine createGridLine(int tickPosition, BRectangle area) {
        int x = tickPosition;
        int y1 = 0;
        int y2 = area.height;
        return new BLine(x, y1, x, y2);
    }

    @Override
    public BText createTitle(String title, TextMetric tm, int start, int end, int width) {
        int y = - width + tm.height();
        int x = (end + start) / 2;
        return new BText(title, x, y, TextAnchor.MIDDLE, TextAnchor.START, tm);
    }

    @Override
    public void translateCanvas(BCanvas canvas, BRectangle area) {
        canvas.translate(0, area.y);
    }
}
