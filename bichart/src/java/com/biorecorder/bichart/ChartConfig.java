package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.AxisConfig;
import com.biorecorder.bichart.graphics.BColor;

/**
 * Created by galafit on 18/8/17.
 */
public class ChartConfig {
    private BColor[] traceColors;
    private BColor backgroundColor;
    private BColor marginColor;

    private TitleConfig titleConfig = new TitleConfig();
    private LegendConfig legendConfig = new LegendConfig();
    private TooltipConfig tooltipConfig = new TooltipConfig();

    private AxisConfig yAxisConfig = new AxisConfig();
    private AxisConfig xAxisConfig = new AxisConfig();

    public ChartConfig() {
        final BColor[] colors = {BColor.BLUE, BColor.RED, BColor.GRAY};
        BColor bgColor = BColor.WHITE;
        BColor marginBgColor = BColor.WHITE;
        BColor labelColor = BColor.GRAY;
        BColor axisColor = BColor.GRAY_LIGHT;;
        BColor gridColor = BColor.GRAY_LIGHT;
        BColor crosshairColor = labelColor;

        xAxisConfig = new AxisConfig();
        xAxisConfig.setColors(axisColor, labelColor, gridColor, gridColor);
        xAxisConfig.setTickMarkSize(4, 0);
        xAxisConfig.setCrosshairLineColor(crosshairColor);
        yAxisConfig = xAxisConfig;
        traceColors = colors;
        backgroundColor = bgColor;
        marginColor = marginBgColor;
        titleConfig.setTextColor(labelColor);

        legendConfig.setBackgroundColor(bgColor);
    }

    public ChartConfig(ChartConfig chartConfig) {
        traceColors = chartConfig.traceColors;
        backgroundColor = chartConfig.backgroundColor;
        marginColor = chartConfig.marginColor;
        titleConfig = new TitleConfig(chartConfig.titleConfig);
        legendConfig = new LegendConfig(chartConfig.legendConfig);
        tooltipConfig = new TooltipConfig(chartConfig.tooltipConfig);
        yAxisConfig = new AxisConfig(chartConfig.yAxisConfig);
        xAxisConfig = new AxisConfig(chartConfig.xAxisConfig);
    }

    public TitleConfig getTitleConfig() {
        return titleConfig;
    }

    public BColor[] getTraceColors() {
        return traceColors;
    }

    public void setTraceColors(BColor[] traceColors) {
        this.traceColors = traceColors;
    }

    public BColor getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(BColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public BColor getMarginColor() {
        return marginColor;
    }

    public void setMarginColor(BColor marginColor) {
        this.marginColor = marginColor;
    }

    public LegendConfig getLegendConfig() {
        return legendConfig;
    }

    public TooltipConfig getTooltipConfig() {
        return tooltipConfig;
    }

    public AxisConfig getYAxisConfig() {
        return yAxisConfig;
    }

    public void setYAxisConfig(AxisConfig yAxisConfig) {
        this.yAxisConfig = yAxisConfig;
    }

    public AxisConfig getXAxisConfig() {
        return xAxisConfig;
    }

    public void setXAxisConfig(AxisConfig xAxisConfig) {
        this.xAxisConfig = xAxisConfig;
    }
}
