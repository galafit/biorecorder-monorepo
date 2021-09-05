package com.biorecorder.bichart.traces;


import com.biorecorder.bichart.chart.ChartData;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;

/**
 * Created by galafit on 11/10/17.
 */
public class LineTracePainter implements TracePainter {
    private LineTraceConfig traceConfig;

    public LineTracePainter() {
        this(new LineTraceConfig());
    }
    
    public LineTracePainter(LineTraceConfig config) {
        traceConfig = config;
    }

   @Override
    public int markSize() {
        return traceConfig.getMarkSize();
    }

    @Override
    public BPoint getCrosshairPoint(ChartData data, int dataIndex,  Scale xScale, Scale yScale) {
        int x = (int) xScale.scale(data.value(dataIndex, 0));
        int y = (int) yScale.scale(data.value(dataIndex, 1));
        return new BPoint(x, y);
    }

    @Override
    public BRectangle getHoverArea(ChartData data, int dataIndex,  Scale xScale, Scale yScale) {
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

    private BColor getFillColor(BColor color) {
        return new BColor(color.getRed(), color.getGreen(), color.getBlue(), 110);
    }

    @Override
    public void drawTrace(BCanvas canvas, ChartData data, Scale xScale, Scale yScale, BColor traceColor) {
        XYViewer xyData = new XYViewer(data);
        if (xyData.size() == 0) {
            return;
        }

        BPath path = null;
        canvas.setStroke(traceConfig.getLineWidth(), traceConfig.getLineDashStyle());
        BColor lineColor = traceColor;
        BColor markColor = traceColor;
        if(traceConfig.getMode() == LineTraceConfig.LINEAR) {
            path = drawLinearPath(canvas, xyData, xScale, yScale, lineColor, markColor);
        }
        if(traceConfig.getMode() == LineTraceConfig.STEP) {
            path = drawStepPath(canvas, xyData, xScale, yScale, lineColor, markColor);
        }
        if(traceConfig.getMode() == LineTraceConfig.VERTICAL_LINES) {
            path = drawVerticalLinesPath(canvas, xyData, xScale, yScale, lineColor, markColor);
        }

        if(path != null && traceConfig.isFilled()) {
            int x_0 = (int) xScale.scale(xyData.getX(0));
            int x_last = (int) xScale.scale(xyData.getX(xyData.size() - 1));
            path.lineTo(x_last, (int)yScale.getStart());
            path.lineTo(x_0, (int)yScale.getStart());
            path.close();
            canvas.setColor(getFillColor(traceColor));
            canvas.fillPath(path);
        }
    }



    private BPath drawLinearPath(BCanvas canvas, XYViewer xyData, Scale xScale, Scale yScale, BColor lineColor, BColor markColor) {
        BPath path = canvas.getEmptyPath();
        int x = (int) xScale.scale(xyData.getX(0));
        int y = (int) yScale.scale(xyData.getY(0));
        path.moveTo(x, y);
        canvas.setColor(markColor);
        int markSize = traceConfig.getMarkSize();
        canvas.fillRect(x - markSize / 2,y - markSize / 2, markSize, markSize);
        for (int i = 1; i < xyData.size(); i++) {
            x = (int) xScale.scale(xyData.getX(i));
            y = (int) yScale.scale(xyData.getY(i));
            path.lineTo(x, y);
            canvas.fillRect(x - markSize / 2,y - markSize / 2, markSize, markSize);

        }
        if(traceConfig.getLineWidth() > 0) {
            canvas.setColor(lineColor);
            canvas.drawPath(path);
        }

        return path;
    }

    private BPath drawStepPath(BCanvas canvas, XYViewer xyData, Scale xScale,  Scale yScale, BColor lineColor, BColor markColor) {
        BPath path = canvas.getEmptyPath();
        int x = (int) xScale.scale(xyData.getX(0));
        int y = (int) yScale.scale(xyData.getY(0));
        path.moveTo(x, y);
        canvas.setColor(markColor);
        int pointRadius = traceConfig.getMarkSize()/ 2;
        canvas.fillOval(x - pointRadius, y - pointRadius, 2 * pointRadius,2 * pointRadius);
        for (int i = 1; i < xyData.size(); i++) {
            x = (int) xScale.scale(xyData.getX(i));
            path.lineTo(x, y);
            y = (int) yScale.scale(xyData.getY(i));
            path.lineTo(x, y);
            canvas.fillOval(x - pointRadius,y - pointRadius, 2 * pointRadius,2 * pointRadius);
        }
        canvas.setColor(lineColor);
        canvas.drawPath(path);
        return path;
    }

    private BPath drawVerticalLinesPath(BCanvas canvas, XYViewer xyData, Scale xScale,  Scale yScale, BColor lineColor, BColor markColor) {
        int x = (int) xScale.scale(xyData.getX(0));
        int y = (int) yScale.scale(xyData.getY(0));
        int pointRadius = traceConfig.getMarkSize() / 2;
        canvas.fillOval(x - pointRadius, y - pointRadius, 2 * pointRadius,2 * pointRadius);
        VerticalLine vLine = new VerticalLine(y);
        for (int i = 1; i < xyData.size(); i++) {
            int x_prev = x;
            x = (int) xScale.scale(xyData.getX(i));
            // draw horizontal lines to avoid line breaking
            canvas.setColor(lineColor);
            if(x > x_prev + 1) {
                vLine.setNewBounds(y);
                canvas.drawLine(x_prev, y, x, y);
            }
            y = (int) yScale.scale(xyData.getY(i));
            vLine.setNewBounds(y);
            // draw vertical line
            canvas.drawLine(x, vLine.min, x, vLine.max);
            canvas.setColor(markColor);
            canvas.fillOval(x - pointRadius,y - pointRadius, 2 * pointRadius,2 * pointRadius);
        }
        return null;
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
