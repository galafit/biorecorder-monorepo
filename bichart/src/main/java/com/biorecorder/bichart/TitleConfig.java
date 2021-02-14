package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.HorizontalAlign;
import com.biorecorder.bichart.graphics.Insets;
import com.biorecorder.bichart.graphics.TextStyle;
import com.sun.istack.internal.Nullable;

/**
 * Created by galafit on 31/8/18.
 */
public class TitleConfig {
    private TextStyle textStyle = new TextStyle(TextStyle.DEFAULT, TextStyle.NORMAL, 16);
    private BColor textColor = BColor.BLACK_LIGHT;
    private Insets margin;
    private int interLineSpace = -1;
    private HorizontalAlign align = HorizontalAlign.CENTER;

    public TitleConfig() {
    }

    public TitleConfig(TitleConfig config) {
        textStyle = config.textStyle;
        textColor = config.textColor;
        margin = config.margin;
        interLineSpace = config.interLineSpace;
        align = config.align;
    }

    public HorizontalAlign getAlign() {
        return align;
    }

    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    public int getInterLineSpace() {
        if(interLineSpace < 0) {
            return (int)(textStyle.getSize() * 0.2);
        }
        return interLineSpace;
    }

    public void setInterLineSpace(int interLineSpace) {
        this.interLineSpace = interLineSpace;
    }

    public TextStyle getTextStyle() {
        return textStyle;
    }

    public void setTextStyle(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public BColor getTextColor() {
        return textColor;
    }

    public void setTextColor(BColor textColor) {
        this.textColor = textColor;
    }

    public Insets getMargin() {
        if(margin != null) {
            return margin;
        }
        return new Insets(textStyle.getSize() / 2);
    }

    public void setMargin(@Nullable Insets margin) {
        this.margin = margin;
    }
}
