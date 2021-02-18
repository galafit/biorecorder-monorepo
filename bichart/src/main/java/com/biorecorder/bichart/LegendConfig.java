package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.*;
import com.sun.istack.internal.Nullable;


/**
 * Created by galafit on 18/8/17.
 */
public class LegendConfig {
    private TextStyle textStyle = new TextStyle(TextStyle.DEFAULT, TextStyle.NORMAL, 12);
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private int borderWidth = 1;
    private BColor backgroundColor = BColor.WHITE;
    private Insets buttonsMargin;
    private Insets legendMargin = new Insets(3);

    private int interItemSpace = 0;
    private int interLineSpace = 1;

    public LegendConfig() {
    }

    public LegendConfig(LegendConfig legendConfig) {
        backgroundColor = legendConfig.backgroundColor;
        buttonsMargin = legendConfig.buttonsMargin;
        textStyle = legendConfig.textStyle;
        verticalAlign = legendConfig.verticalAlign;
        horizontalAlign = legendConfig.horizontalAlign;
        interItemSpace = legendConfig.interItemSpace;
        interLineSpace = legendConfig.interLineSpace;
        borderWidth = legendConfig.borderWidth;
        legendMargin = legendConfig.legendMargin;
    }

    public Insets getLegendMargin() {
        if(legendMargin == null) {
            return new Insets(0);
        }
        return legendMargin;
    }

    public void setLegendMargin(Insets legendMargin) {
        this.legendMargin = legendMargin;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
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
        return new Insets((int)(textStyle.getSize() * 0.2));
    }

    public void setButtonsMargin(@Nullable Insets buttonsMargin) {
        this.buttonsMargin = buttonsMargin;
    }
}
