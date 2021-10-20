package com.biorecorder.bichart.traces;

import com.biorecorder.bichart.ChartData;
import com.biorecorder.bichart.Range;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;

public class VerticalLinePainter implements TracePainter {
    @Override
    public int markSize() {
        return 1;
    }

    @Override
    public BPoint getCrosshairPoint(ChartData data, int dataIndex, Scale xScale, Scale yScale) {
        int x = (int) xScale.scale(data.value(dataIndex, 0));
        int y = (int) yScale.scale(data.value(dataIndex, 1));
        return new BPoint(x, y);
    }

    @Override
    public BRectangle getHoverArea(ChartData data, int dataIndex, Scale xScale, Scale yScale) {
        int x = (int) xScale.scale(data.value(dataIndex, 0));
        int y = (int) yScale.scale(data.value(dataIndex, 1));
        return new BRectangle(x, y, 0, 0);
    }

    @Override
    public Range yMinMax(ChartData data) {
        return data.columnMinMax(1);
    }

    @Override
    public String[] getTooltipInfo(ChartData data, int dataIndex, Scale xScale, Scale yScale) {
        String[] info = new String[2];
        info[0] = "x: " + xScale.formatDomainValue(data.value(dataIndex, 0));
        info[1] = "y: " + yScale.formatDomainValue(data.value(dataIndex, 1));
        return info;
    }

    @Override
    public void drawTrace(BCanvas canvas, ChartData data, Scale xScale, Scale yScale, BColor traceColor) {
        XYViewer xyData = new XYViewer(data);
        if (xyData.size() == 0) {
            return;
        }
        canvas.setColor(traceColor);
        int x0 = (int) xScale.getStart();
        int y0 = (int) yScale.getStart();
        int y1;
        int x1;
        for (int i = 0; i < xyData.size(); i++) {
            x1 = (int)xScale.scale(xyData.getX(i));
            y1 = (int)yScale.scale(xyData.getY(i));
            canvas.drawLine(x0, y0, x1, y1);
            y0 = y1;
            x0 = x1;
        }
    }

    class VerticalLine {
        int max;
        int min;

        public VerticalLine(int y) {
            min = y;
            max = y;
        }

        void setNewBounds(int y) {
            if (y >= min && y <= max) {
                min = max = y;
            } else if (y > max) {
                min = max + 1;
                max = y;
            } else if (y < min) {
                max = min - 1;
                min = y;
            }
        }
    }
}
