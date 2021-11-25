package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.BPoint;

public class TooltipData {
    private BColor color;
    private BPoint crossPoint;
    private String[] tooltipInfo;

    public TooltipData(BColor color, BPoint crossPoint, String[] tooltipInfo) {
        this.color = color;
        this.crossPoint = crossPoint;
        this.tooltipInfo = tooltipInfo;
    }

    public BColor getColor() {
        return color;
    }

    public BPoint getCrossPoint() {
        return crossPoint;
    }

    public String[] getTooltipInfo() {
        return tooltipInfo;
    }
}
