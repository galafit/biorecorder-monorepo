package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.*;
import com.sun.istack.internal.Nullable;


/**
 * Created by galafit on 18/8/17.
 */
public class LegendConfig {
    private boolean isEnabled = true;
    private TextStyle textStyle = new TextStyle(TextStyle.DEFAULT, TextStyle.NORMAL, 12);
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    private VerticalAlign verticalAlign = VerticalAlign.TOP;

    private int borderWidth = 1;
    private BColor backgroundColor = BColor.WHITE;
    private Insets buttonsMargin;
    private boolean isAttachedToStacks = false;

    private int interItemSpace = 0;
    private int interLineSpace = 1;

    public LegendConfig() {
    }

    public LegendConfig(LegendConfig legendConfig) {
        backgroundColor = legendConfig.backgroundColor;
        buttonsMargin = legendConfig.buttonsMargin;
        isAttachedToStacks = legendConfig.isAttachedToStacks;
        textStyle = legendConfig.textStyle;
        verticalAlign = legendConfig.verticalAlign;
        horizontalAlign = legendConfig.horizontalAlign;
        interItemSpace = legendConfig.interItemSpace;
        interLineSpace = legendConfig.interLineSpace;
        isEnabled = legendConfig.isEnabled;
        borderWidth = legendConfig.borderWidth;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public int getInterItemSpace() {
        return interItemSpace;
    }

    public void setInterItemSpace(int interItemSpace) {
        this.interItemSpace = interItemSpace;
    }

    public int getInterLineSpace() {
        return interLineSpace;
    }

    public void setInterLineSpace(int interLineSpace) {
        this.interLineSpace = interLineSpace;
    }

    public boolean isAttachedToStacks() {
        return isAttachedToStacks;
    }

    public void setAttachedToStacks(boolean attachedToStacks) {
        isAttachedToStacks = attachedToStacks;
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
    }

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    public TextStyle getTextStyle() {
        return textStyle;
    }

    public void setTextStyle(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    public BColor getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(BColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Insets getButtonsMargin() {
        if(buttonsMargin != null) {
            return buttonsMargin;
        }

        return new Insets(textStyle.getSize()/2);
    }

    public void setButtonsMargin(@Nullable Insets buttonsMargin) {
        this.buttonsMargin = buttonsMargin;
    }
}
