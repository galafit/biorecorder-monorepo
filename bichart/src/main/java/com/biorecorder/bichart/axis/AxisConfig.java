package com.biorecorder.bichart.axis;

import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.TextStyle;
import com.biorecorder.bichart.graphics.DashStyle;
import com.biorecorder.bichart.scales.TickLabelFormat;
import com.sun.istack.internal.Nullable;

/**
 * Created by galafit on 5/9/17.
 */
public class AxisConfig {
    private BColor axisLineColor =  BColor.GRAY;
    private int axisLineWidth = 1; // if width = 0 line will not be drawn
    private DashStyle axisLineDashStyle = DashStyle.SOLID;

    private int tickPadding; // (px) padding between tick mark and tick label
    private TextStyle tickLabelTextStyle = new TextStyle(TextStyle.DEFAULT, TextStyle.NORMAL, 12);

    private int titlePadding; // px
    private TextStyle titleTextStyle = new TextStyle(TextStyle.DEFAULT, TextStyle.NORMAL, 12);
    private BColor titleColor = BColor.GRAY;

    private int tickMarkInsideSize = 0; // px
    private int tickMarkOutsideSize = 3; // px
    private int tickMarkWidth = 1; // px
    private BColor tickMarkColor = BColor.GRAY;
    private BColor tickLabelColor = BColor.GRAY;
    private boolean isTickLabelCentered = true;

    private int minorTickMarkWidth = 1; // px
    private BColor minorTickMarkColor = BColor.GRAY;
    private BColor gridColor = BColor.GRAY_LIGHT;
    private int gridLineWidth = 1;
    private DashStyle gridLineDashStyle = DashStyle.SOLID;

    private int minorGridLineWidth = 1;
    private DashStyle minorGridLineDashStyle = DashStyle.DOT;
    private BColor minorGridColor = BColor.GRAY_LIGHT;
    private int minorTickMarkInsideSize = 0; // px
    private int minorTickMarkOutsideSize = 0; // px
    private int minorTickIntervalCount = 0; // number of minor intervals in one major interval
    private boolean isTickLabelOutside = true;

    private BColor crosshairLineColor = BColor.GRAY;
    private int crosshairLineWidth = 1;
    private DashStyle crosshairLineDashStyle = DashStyle.SOLID;

    public AxisConfig() {
        titlePadding = titleTextStyle.getSize() / 4;
        tickPadding = tickLabelTextStyle.getSize() / 2;
    }

    public AxisConfig(AxisConfig axisConfig) {
        axisLineColor = axisConfig.axisLineColor;
        axisLineWidth = axisConfig.axisLineWidth;
        axisLineDashStyle = axisConfig.axisLineDashStyle;

        gridColor = axisConfig.gridColor;
        minorGridColor = axisConfig.minorGridColor;
        gridLineDashStyle = axisConfig.gridLineDashStyle;
        gridLineWidth = axisConfig.gridLineWidth;
        minorGridLineDashStyle = axisConfig.minorGridLineDashStyle;
        minorGridLineWidth = axisConfig.minorGridLineWidth;

        tickPadding = axisConfig.tickPadding;
        tickMarkWidth = axisConfig.tickMarkWidth;
        tickMarkInsideSize = axisConfig.tickMarkInsideSize;
        tickMarkOutsideSize = axisConfig.tickMarkOutsideSize;
        tickMarkColor = axisConfig.tickMarkColor;
        tickLabelColor = axisConfig.tickLabelColor;
        tickLabelTextStyle = axisConfig.tickLabelTextStyle;
        isTickLabelCentered = axisConfig.isTickLabelCentered;

        minorTickMarkWidth = axisConfig.minorTickMarkWidth;
        minorTickMarkOutsideSize = axisConfig.minorTickMarkOutsideSize;
        minorTickMarkInsideSize = axisConfig.minorTickMarkInsideSize;
        minorTickMarkColor = axisConfig.minorTickMarkColor;
        minorTickIntervalCount = axisConfig.minorTickIntervalCount;

        titlePadding = axisConfig.titlePadding;
        titleTextStyle = axisConfig.titleTextStyle;
        titleColor = axisConfig.titleColor;

        isTickLabelOutside = axisConfig.isTickLabelOutside;

        crosshairLineColor = axisConfig.crosshairLineColor;
        crosshairLineDashStyle = axisConfig.crosshairLineDashStyle;
        crosshairLineWidth = axisConfig.crosshairLineWidth;
    }

    public void setTickLabelTextStyle(TextStyle tickLabelTextStyle) {
        this.tickLabelTextStyle = tickLabelTextStyle;
    }

    public BColor getCrosshairLineColor() {
        return crosshairLineColor;
    }

    public void setCrosshairLineColor(BColor crosshairLineColor) {
        this.crosshairLineColor = crosshairLineColor;
    }

    public int getCrosshairLineWidth() {
        return crosshairLineWidth;
    }

    public void setCrosshairLineWidth(int crosshairLineWidth) {
        this.crosshairLineWidth = crosshairLineWidth;
    }

    public DashStyle getCrosshairLineDashStyle() {
        return crosshairLineDashStyle;
    }

    public void setCrosshairLineDashStyle(DashStyle crosshairLineDashStyle) {
        this.crosshairLineDashStyle = crosshairLineDashStyle;
    }

    public boolean isTickLabelCentered() {
        return isTickLabelCentered;
    }

    public void setTickLabelCentered(boolean tickLabelCentered) {
        isTickLabelCentered = tickLabelCentered;
    }

    public int getMinorTickIntervalCount() {
        return minorTickIntervalCount;
    }

    public void setMinorTickIntervalCount(int minorTickIntervalCount) {
        this.minorTickIntervalCount = minorTickIntervalCount;
    }

    public void setColors(BColor axisColor, BColor labelColor, BColor gridColor, BColor minorGridColor) {
        axisLineColor = axisColor;
        tickMarkColor = axisColor;
        minorTickMarkColor = axisColor;
        titleColor = labelColor;
        tickLabelColor = labelColor;
        this.gridColor = gridColor;
        this.minorGridColor = minorGridColor;
    }

    public void setLineColor(BColor lineColor) {
        this.axisLineColor = lineColor;
    }

    public BColor getAxisLineColor() {
        return axisLineColor;
    }

    public void setAxisLineColor(BColor lineColor) {
        this.axisLineColor = lineColor;
    }

    public int getAxisLineWidth() {
        return axisLineWidth;
    }

    public void setAxisLineWidth(int axisLineWidth) {
        this.axisLineWidth = axisLineWidth;
    }

    public DashStyle getAxisLineDashStyle() {
        return axisLineDashStyle;
    }

    public void setAxisLineDashStyle(DashStyle axisLineDashStyle) {
        this.axisLineDashStyle = axisLineDashStyle;
    }

    /** ======================= Title ========================== **/

    public int getTitlePadding() {
        return titlePadding;
    }

    public void setTitlePadding(int titlePadding) {
        this.titlePadding = titlePadding;
    }

    public TextStyle getTitleTextStyle() {
        return titleTextStyle;
    }

    public BColor getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(BColor titleColor) {
        this.titleColor = titleColor;
    }



    /** ======================= Ticks ========================== **/


    public boolean isTickLabelOutside() {
        return isTickLabelOutside;
    }

    public void setTickLabelOutside(boolean isTickLabelOutside) {
        this.isTickLabelOutside = isTickLabelOutside;

    }

    public void setTickMarkSize(int insideSize, int outsideSize) {
        this.tickMarkOutsideSize = outsideSize;
        this.tickMarkInsideSize = insideSize;
    }

    public int getTickMarkInsideSize() {
        return tickMarkInsideSize;
    }

    public int getTickMarkOutsideSize() {
        return tickMarkOutsideSize;
    }

    public void setTickMarkColor(BColor tickMarkColor) {
        this.tickMarkColor = tickMarkColor;
    }

    public BColor getTickMarkColor() {
        return tickMarkColor;
    }

    public int getTickMarkWidth() {
        return tickMarkWidth;
    }

    public void setTickMarkWidth(int tickMarkWidth) {
        this.tickMarkWidth = tickMarkWidth;
    }

    public int getTickPadding() {
        return tickPadding;
    }

    public void setTickPadding(int tickPadding) {
        this.tickPadding = tickPadding;
    }


    public TextStyle getTickLabelTextStyle() {
        return tickLabelTextStyle;
    }

    public BColor getTickLabelColor() {
        return tickLabelColor;
    }


    /** =======================Minor Ticks ========================== **/


    public int getMinorTickMarkInsideSize() {
        return minorTickMarkInsideSize;
    }

    public void setMinorTickMarkSize(int insideSize, int outsideSize) {
        this.minorTickMarkInsideSize = insideSize;
        this.minorTickMarkOutsideSize = outsideSize;
    }

    public int getMinorTickMarkOutsideSize() {
        return minorTickMarkOutsideSize;
    }

    public void setMinorTickMarkColor(BColor minorTickMarkColor) {
        this.minorTickMarkColor = minorTickMarkColor;
    }

    public BColor getMinorTickMarkColor() {
        return minorTickMarkColor;
    }

    public int getMinorGridLineWidth() {
        return minorGridLineWidth;
    }

    public void setMinorGridLineWidth(int minorGridLineWidth) {
        this.minorGridLineWidth = minorGridLineWidth;
    }

    public DashStyle getMinorGridLineDashStyle() {
        return minorGridLineDashStyle;
    }

    public void setMinorGridLineDashStyle(DashStyle minorGridLineDashStyle) {
        this.minorGridLineDashStyle = minorGridLineDashStyle;
    }

    public int getMinorTickMarkWidth() {
        return minorTickMarkWidth;
    }

    public void setMinorTickMarkWidth(int minorTickMarkWidth) {
        this.minorTickMarkWidth = minorTickMarkWidth;
    }


    /** ======================= Grid ========================== **/


    public void setGridColor(BColor gridColor) {
        this.gridColor = gridColor;
    }

    public void setMinorGridColor(BColor minorGridColor) {
        this.minorGridColor = minorGridColor;
    }

    public BColor getGridColor() {
        return gridColor;
    }

    public BColor getMinorGridColor() {
        return minorGridColor;
    }

    public int getGridLineWidth() {
        return gridLineWidth;
    }

    public void setGridLineWidth(int gridLineWidth) {
        this.gridLineWidth = gridLineWidth;
    }

    public DashStyle getGridLineDashStyle() {
        return gridLineDashStyle;
    }

    public void setGridLineDashStyle(DashStyle gridLineDashStyle) {
        this.gridLineDashStyle = gridLineDashStyle;
    }
}
