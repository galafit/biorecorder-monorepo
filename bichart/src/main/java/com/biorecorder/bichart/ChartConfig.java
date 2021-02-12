package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.AxisConfig;
import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.Insets;
import com.sun.istack.internal.Nullable;

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

    private Insets margin;
    private Insets spacing;
    private int autoSpacing = 5; //px taken into account only if spacing is null

    private int stackGap = 4; //px

    private AxisConfig yAxisConfig = new AxisConfig();
    private AxisConfig xAxisConfig = new AxisConfig();

    private XAxisPosition primaryXPosition = XAxisPosition.BOTTOM;
    private YAxisPosition primaryYPosition = YAxisPosition.LEFT;

    private int defaultStackWeight = 4;

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
        spacing = chartConfig.spacing;
        margin = chartConfig.margin;
        autoSpacing = chartConfig.autoSpacing;
        stackGap = chartConfig.stackGap;
        yAxisConfig = new AxisConfig(chartConfig.yAxisConfig);
        xAxisConfig = new AxisConfig(chartConfig.xAxisConfig);
        primaryXPosition = chartConfig.primaryXPosition;
        primaryYPosition = chartConfig.primaryYPosition;
        defaultStackWeight = chartConfig.defaultStackWeight;
    }

    public int getStackGap() {
        return stackGap;
    }

    public void setStackGap(int stackGap) {
        this.stackGap = stackGap;
    }

    public Insets getMargin() {
        return margin;
    }

    /**
     * if null (default) margins will be calculated automatically
     */
    public void setMargin(@Nullable Insets margin) {
        this.margin = margin;
    }

    public Insets getSpacing() {
        if(spacing == null) {
            return new Insets(autoSpacing);
        }
        return spacing;
    }

    /**
     * if null (default) spaces will be calculated automatically on the base of autoSpacing
     */
    public void setSpacing(@Nullable Insets spacing) {
        this.spacing = spacing;
    }

    public int getAutoSpacing() {
        return autoSpacing;
    }

    public void setAutoSpacing(int autoSpacing) {
        this.autoSpacing = autoSpacing;
    }

    public XAxisPosition getPrimaryXPosition() {
        return primaryXPosition;
    }

    public void setPrimaryXPosition(XAxisPosition primaryXPosition) {
        this.primaryXPosition = primaryXPosition;
    }

    public YAxisPosition getPrimaryYPosition() {
        return primaryYPosition;
    }

    public void setPrimaryYPosition(YAxisPosition primaryYPosition) {
        this.primaryYPosition = primaryYPosition;
    }

    public int getDefaultStackWeight() {
        return defaultStackWeight;
    }

    public void setDefaultStackWeight(int defaultStackWeight) {
        this.defaultStackWeight = defaultStackWeight;
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
