package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.TextMetric;
import com.biorecorder.bichart.graphics.*;


class Tooltip {
    private Trace trace;
    private int pointIndex;
    private TooltipConfig config;

    public Tooltip(TooltipConfig tooltipConfig) {
        this.config = tooltipConfig;
    }

    public void setConfig(TooltipConfig tooltipConfig) {
        this.config = tooltipConfig;
    }

    /**
     * @return true if hoverPoint changed and false if new hoverPoint equal this.hoverPoint
     */
    public boolean setHoverPoint(Trace trace, int pointIndex) {
        if(this.trace != trace || this.pointIndex != pointIndex) {
            this.trace = trace;
            this.pointIndex = pointIndex;
            return true;
        }
        return false;
    }

    public boolean removeHoverPoint() {
        if(trace != null) {
            trace = null;
            return true;
        }
        return false;
    }

    public void draw(BCanvas canvas, BRectangle area) {
        if(trace == null || pointIndex < 0) {
            return;
        }
        // draw cross hair
        BPoint crossPoint = trace.getCrosshairPoint(pointIndex);
        trace.getXAxis().drawCrosshair(canvas, area, crossPoint.getX());
        trace.getYAxis().drawCrosshair(canvas, area, crossPoint.getY());
        String[] items = trace.getTooltipInfo(pointIndex);
        canvas.setTextStyle(config.getTextStyle());
        BDimension tooltipDimension  = getTextSize(canvas, items);
        int tooltipAreaX = crossPoint.getX() - tooltipDimension.width / 2;
        int tooltipAreaY = area.y;
        if (tooltipAreaX + tooltipDimension.width > area.x + area.width){
            tooltipAreaX = area.x + area.width - tooltipDimension.width;
        }
        if (tooltipAreaX < area.x){
            tooltipAreaX = area.x;
        }

        BRectangle tooltipArea = new BRectangle(tooltipAreaX, tooltipAreaY, tooltipDimension.width, tooltipDimension.height);
        canvas.setColor(config.getBackgroundColor());
        canvas.fillRect(tooltipArea.x, tooltipArea.y, tooltipArea.width, tooltipArea.height);
        canvas.setColor(config.getBorderColor());
        canvas.setStroke(config.getBorderWidth(), DashStyle.SOLID);
        canvas.drawRect(tooltipArea.x, tooltipArea.y, tooltipArea.width, tooltipArea.height);
        drawTooltipInfo(canvas, tooltipArea, items);
    }


    /**
     * https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
     */
    private void drawTooltipInfo(BCanvas canvas, BRectangle area, String[] items) {
        Insets margin = config.getMargin();
        int stringHeight = canvas.getRenderContext().getTextMetric(config.getTextStyle()).height();
        int lineSpace = getInterLineSpace();
        int x = area.x + margin.left();
        int y = area.y + margin.top();

        for (int i = 0; i < items.length; i++) {
            drawItem(canvas, x, y, items[i]);
            //g2.drawRect(x - margin.left(), y, area.width, stringHeght);
            y += (lineSpace + stringHeight);

        }
    }

    private int itemWidth(BCanvas canvas, String item) {
        TextMetric tm = canvas.getRenderContext().getTextMetric(config.getTextStyle());
        int width = 0;
        width += getColorMarkerSize() + getColorMarkerPadding();
        if (item != null) {
            width += tm.stringWidth(item);
        }
        return width;
    }


    private void drawItem(BCanvas canvas, int x, int y, String item) {
        TextMetric tm = canvas.getRenderContext().getTextMetric(config.getTextStyle());
        int string_y = y + tm.ascent();
        canvas.setColor(trace.getColor());
        int colorMarkerSize = getColorMarkerSize();
        canvas.fillRect(x, y + (tm.height() - colorMarkerSize) / 2 + 1, colorMarkerSize, colorMarkerSize);
        x = x + colorMarkerSize + getColorMarkerPadding();

        canvas.setColor(config.getColor());
        canvas.setTextStyle(config.getTextStyle());
        canvas.drawString(item, x, string_y);
        x = x + tm.stringWidth(item);
    }


    private int getColorMarkerSize() {
        return config.getTextStyle().getSize();
    }

    private int getColorMarkerPadding() {
        return (int) (config.getTextStyle().getSize() * 0.5);
    }

    private BDimension getTextSize(BCanvas canvas, String[] items) {
        int textWidth = 0;

        for (int i = 0; i < items.length; i++) {
            textWidth = Math.max(textWidth, itemWidth(canvas, items[i]));
        }

        Insets margin = config.getMargin();
        textWidth += margin.left() + margin.right();
        int strHeight = canvas.getRenderContext().getTextMetric(config.getTextStyle()).height();
        int textHeight = margin.top() + margin.bottom() + items.length * strHeight;
        textHeight += getInterLineSpace() * (items.length - 1);

        return new BDimension(textWidth, textHeight);
    }

    private int getInterLineSpace() {
        return (int) (config.getTextStyle().getSize() * 0.2);
    }
}
