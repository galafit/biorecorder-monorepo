package com.biorecorder.bichart.button;

import com.biorecorder.bichart.graphics.Insets;
import com.biorecorder.bichart.graphics.TextMetric;
import com.biorecorder.bichart.graphics.TextStyle;
import com.biorecorder.bichart.graphics.*;

/**
 * Created by galafit on 18/12/17.
 */
public class SwitchButton {
    private boolean isSelected;
    private ButtonGroup group;
    private int borderWidth = 1;
    private int x;
    private int y;
    private int width;
    private int height;

    private BColor color = BColor.BLACK_LIGHT;
    private String label = "";
    private BColor backgroundColor = BColor.WHITE_OBSCURE_LIGHT;
    private TextStyle textStyle = new TextStyle(TextStyle.DEFAULT, TextStyle.NORMAL, 12);
    private Insets margin = new Insets((int)(textStyle.getSize() * 0.2),
            (int)(textStyle.getSize() * 0.2),
            (int)(textStyle.getSize() * 0.2),
            (int)(textStyle.getSize() * 0.2));
    
    public SwitchButton(String label) {
        if(label != null) {
            this.label = label;
        }
    }

    public void setGroup(ButtonGroup group) {
        this.group = group;
    }

    public void setLabel(String label) {
        if(label != null) {
            this.label = label;
        }
    }

    public void setSelected(boolean isSelected) {
        if (group != null) {
            // use the group model instead
            group.setSelected(this, isSelected);
        } else {
            this.isSelected = isSelected;
        }
    }

    public boolean isSelected() {
        if(group != null) {
            return group.isSelected(this);
        } else {
            return isSelected;
        }
    }


    public void switchState() {
        if(isSelected()) {
            setSelected(false);
        } else {
            setSelected(true);
        }
    }

    public String getLabel() {
        return label;
    }

    public BRectangle getBounds() {
        return new BRectangle(x, y, width, height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public BDimension getPrefferedSize(RenderContext renderContext) {
        TextMetric tm = renderContext.getTextMetric(textStyle);
        int width = tm.stringWidth(label) + getCheckMarkSize() + getCheckMarkPadding()
                + margin.left() + margin.right();
        int height = tm.height() + margin.top() + margin.bottom();
        return new BDimension(width, height);
    }

    public void draw(BCanvas canvas) {
        // draw backgroundColor
        canvas.setColor(backgroundColor);
        canvas.fillRect(x, y, width, height);
        canvas.setColor(color);

        // draw item
        TextMetric tm = canvas.getRenderContext().getTextMetric(textStyle);
        int x0 = x + margin.left();
        int y0 = y + margin.top() + tm.ascent();
        canvas.drawString(label, x0, y0);

        if(isSelected()) {
            // draw border
            canvas.setStroke(borderWidth, DashStyle.SOLID);
            int borderShift = borderWidth/2;
            canvas.drawRect(x + borderShift , y + borderShift, width - borderWidth, height - borderWidth);
            // draw selection marker
            canvas.setStroke(1, DashStyle.SOLID);
            x0 = x + margin.left() + tm.stringWidth(label) + getCheckMarkPadding();
            y0 = y + height/2;

            int x1 = x0 + getCheckMarkSize()/2;
            int y1 = y + height - margin.bottom();

            int x2 = x0 + getCheckMarkSize();
            int y2 = y + margin.top();

            canvas.drawLine(x0, y0, x1, y1);
            canvas.drawLine(x1, y1, x2, y2);
        }

    }

    private int getCheckMarkSize() {
        return (int) (textStyle.getSize() * 0.8);
    }

    private int getCheckMarkPadding() {
        return (int) (textStyle.getSize() * 0.5);
    }

    public void setBackgroundColor(BColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setTextStyle(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public void setMargin(Insets margin) {
        this.margin = margin;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public void setColor(BColor color) {
        this.color = color;
    }
}
