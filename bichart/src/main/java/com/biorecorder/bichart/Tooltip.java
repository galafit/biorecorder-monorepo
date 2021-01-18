package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.TextMetric;
import com.biorecorder.bichart.graphics.*;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hdablin on 02.08.17.
 */
class Tooltip {
    private TooltipConfig tooltipConfig;
    private int x, y;
    private int y_offset = 2;
    private String separator = ":  ";
    private TooltipItem header;
    private ArrayList<TooltipItem> items = new ArrayList<TooltipItem>();

    private List<Crosshair> xCrosshairs = new ArrayList<>();
    private List<Crosshair> yCrosshairs = new ArrayList<>();

    public Tooltip(TooltipConfig tooltipConfig, int x, int y) {
        this.tooltipConfig = tooltipConfig;
        this.x = x;
        this.y = y;
    }

    public void addXCrosshair(int xAxisIndex, int position) {
        xCrosshairs.add(new Crosshair(xAxisIndex, position));
    }

    public void addYCrosshair(int yAxisIndex, int position) {
        yCrosshairs.add(new Crosshair(yAxisIndex, position));
    }


    public List<Crosshair> getXCrosshairs() {
        return xCrosshairs;
    }

    public List<Crosshair> getYCrosshairs() {
        return yCrosshairs;
    }

    public void setHeader(@Nullable BColor markColor, @Nullable String label, @Nullable String value) {
        header = new TooltipItem(markColor, label, value);
    }

    public void addLine(@Nullable BColor markColor, @Nullable String label, @Nullable String value){
        items.add(new TooltipItem(markColor, label, value));
    }


    public void draw(BCanvas canvas, BRectangle area) {
        canvas.setTextStyle(tooltipConfig.getTextStyle());
        BDimension tooltipDimension  = getTextSize(canvas);
        int tooltipAreaX = x - tooltipDimension.width / 2;
        int tooltipAreaY = y - tooltipDimension.height - y_offset;
        if (tooltipAreaX + tooltipDimension.width > area.x + area.width){
            tooltipAreaX = area.x + area.width - tooltipDimension.width;
        }
        if (tooltipAreaX < area.x){
            tooltipAreaX = area.x;
        }
        if (tooltipAreaY < area.y){
            tooltipAreaY = area.y;
        }
        if (tooltipAreaY + tooltipDimension.height > area.y + area.height ){
            tooltipAreaY = area.y + area.height - tooltipDimension.height;
        }

        BRectangle tooltipArea = new BRectangle(tooltipAreaX, tooltipAreaY, tooltipDimension.width, tooltipDimension.height);
        canvas.setColor(tooltipConfig.getBackgroundColor());
        canvas.fillRect(tooltipArea.x, tooltipArea.y, tooltipArea.width, tooltipArea.height);
        canvas.setColor(tooltipConfig.getBorderColor());
        canvas.setStroke(tooltipConfig.getBorderWidth(), DashStyle.SOLID);
        canvas.drawRect(tooltipArea.x, tooltipArea.y, tooltipArea.width, tooltipArea.height);
        drawTooltipInfo(canvas, tooltipArea);
    }


    /**
     * https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
     */
    private void drawTooltipInfo(BCanvas canvas, BRectangle area) {
        Insets margin = tooltipConfig.getMargin();
        int stringHeight = canvas.getRenderContext().getTextMetric(tooltipConfig.getTextStyle()).height();
        int lineSpace = getInterLineSpace();
        int x = area.x + margin.left();
        int y = area.y + margin.top();
        if (header != null) {
            canvas.setColor(tooltipConfig.getHeaderBackgroundColor());
            canvas.fillRect(area.x, area.y, area.width, stringHeight + margin.top());

            int headerWidth = itemWidth(canvas, header);

            drawItem(canvas, area.x + (area.width - headerWidth) / 2 , y, header);
            y += (lineSpace + stringHeight);
        }

        for (int i = 0; i < items.size(); i++) {
            drawItem(canvas, x, y, items.get(i));
            //g2.drawRect(x - margin.left(), y, area.width, stringHeght);
            y += (lineSpace + stringHeight);
        }
    }

    private int itemWidth(BCanvas canvas, TooltipItem infoItem) {
        TextMetric tm = canvas.getRenderContext().getTextMetric(tooltipConfig.getTextStyle());
        int width = 0;
        if (infoItem.getMarkColor() != null) {
            width += getColorMarkerSize() + getColorMarkerPadding();
        }
        if (infoItem.getLabel() != null) {
            String labelString = infoItem.getLabel() + separator;
            width += tm.stringWidth(labelString);
        }
        if (infoItem.getValue() != null) {
            width += tm.stringWidth(infoItem.getValue());
        }
        return width;
    }


    private void drawItem(BCanvas canvas, int x, int y, TooltipItem infoItem) {
        TextMetric tm = canvas.getRenderContext().getTextMetric(tooltipConfig.getTextStyle());
        int string_y = y + tm.ascent();
        if (infoItem.getMarkColor() != null) {
            canvas.setColor(infoItem.getMarkColor());
            int colorMarkerSize = getColorMarkerSize();
            canvas.fillRect(x, y + (tm.height() - colorMarkerSize) / 2 + 1, colorMarkerSize, colorMarkerSize);
            x = x + colorMarkerSize + getColorMarkerPadding();
        }
        if (infoItem.getLabel() != null) {
            canvas.setColor(tooltipConfig.getColor());
            canvas.setTextStyle(tooltipConfig.getTextStyle());
            String labelString = infoItem.getLabel() + separator;
            canvas.drawString(labelString, x, string_y);
            x = x + tm.stringWidth(labelString);
        }
        if (infoItem.getValue() != null) {
            canvas.setColor(tooltipConfig.getColor());
            // font for value is always BOLD!
            TextStyle ts = tooltipConfig.getTextStyle();
            TextStyle boldTextStyle = new TextStyle(ts.getFontName(), TextStyle.BOLD, ts.getSize());
            canvas.setTextStyle(boldTextStyle);
            canvas.drawString(infoItem.getValue(), x, string_y);
        }
    }


    private int getColorMarkerSize() {
        return tooltipConfig.getTextStyle().getSize();
    }

    private int getColorMarkerPadding() {
        return (int) (tooltipConfig.getTextStyle().getSize() * 0.5);
    }

    private BDimension getTextSize(BCanvas canvas) {
        int textWidth = 0;

        for (int i = 0; i < items.size(); i++) {
            textWidth = Math.max(textWidth, itemWidth(canvas, items.get(i)));
        }
        if (header != null) {
            textWidth = Math.max(textWidth, itemWidth(canvas, header));
        }
        Insets margin = tooltipConfig.getMargin();
        textWidth += margin.left() + margin.right();
        int strHeight = canvas.getRenderContext().getTextMetric(tooltipConfig.getTextStyle()).height();
        int textHeight = margin.top() + margin.bottom() + items.size() * strHeight;
        textHeight += getInterLineSpace() * (items.size() - 1);
        if (header != null) {
            textHeight += strHeight + getInterLineSpace();
        }
        return new BDimension(textWidth, textHeight);
    }

    private int getInterLineSpace() {
        return (int) (tooltipConfig.getTextStyle().getSize() * 0.2);
    }

    public class TooltipItem {
        private String label;
        private String value;
        private BColor markColor;

        public TooltipItem(BColor markColor, String label, String value) {
            this.label = label;
            this.value = value;
            this.markColor = markColor;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public BColor getMarkColor() {
            return markColor;
        }

        public void setMarkColor(BColor markColor) {
            this.markColor = markColor;
        }
    }
}
