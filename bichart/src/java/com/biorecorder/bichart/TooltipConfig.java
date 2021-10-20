package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.Insets;
import com.biorecorder.bichart.graphics.TextStyle;
import com.sun.istack.internal.Nullable;


/**
 * Created by galafit on 19/8/17.
 */
public class TooltipConfig {
    private TextStyle textStyle = new TextStyle(TextStyle.DEFAULT, TextStyle.NORMAL, 12);
    private BColor color = BColor.BLACK_LIGHT;
    private BColor backgroundColor = BColor.WHITE_OBSCURE_LIGHT;
    private BColor borderColor = new BColor(180, 180, 180);
    private int borderWidth = 1;
    private Insets margin;

    public TooltipConfig() {
    }

    public TooltipConfig(TooltipConfig config) {
        textStyle = config.textStyle;
        color = config.color;
        backgroundColor = config.backgroundColor;
        borderColor = config.borderColor;
        borderWidth = config.borderWidth;
        margin = config.margin;
    }

    public BColor getColor() {
        return color;
    }

    public void setColor(BColor color) {
        this.color = color;
    }

    public TextStyle getTextStyle() {
        return textStyle;
    }

    public BColor getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(BColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public BColor getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(BColor borderColor) {
        this.borderColor = borderColor;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Insets getMargin() {
        if(margin != null) {
            return margin;
        }
        return new Insets((int)(getTextStyle().getSize() * 0.4),
                (int)(getTextStyle().getSize() * 0.8),
                (int)(getTextStyle().getSize() * 0.4),
                (int)(getTextStyle().getSize() * 0.8));
    }

    public void setMargin(@Nullable Insets margin) {
        this.margin = margin;
    }
}
