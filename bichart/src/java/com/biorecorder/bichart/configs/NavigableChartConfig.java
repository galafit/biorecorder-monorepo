package com.biorecorder.bichart.configs;

import com.biorecorder.bichart.graphics.BColor;

/**
 * Created by galafit on 31/8/18.
 */
public class NavigableChartConfig {
    private ChartConfig chartConfig;
    private ChartConfig navigatorConfig;
    private ScrollConfig scrollConfig;

    private BColor backgroundColor = BColor.WHITE_OBSCURE;

    public NavigableChartConfig() {
        BColor navigatorBgColor = BColor.WHITE_OBSCURE;
        BColor navigatorMarginColor = navigatorBgColor;

        chartConfig = new ChartConfig();
        navigatorConfig = new ChartConfig();
        scrollConfig = new ScrollConfig();

        chartConfig.getYAxisConfig().setTickLabelOutside(false);

        navigatorConfig.getYAxisConfig().setTickLabelOutside(false);
        navigatorConfig.setBackgroundColor(navigatorBgColor);
        navigatorConfig.setMarginColor(navigatorMarginColor);
        navigatorConfig.getLegendConfig().setBackgroundColor(navigatorBgColor);

        BColor scrollColor = BColor.GRAY_LIGHT;
        scrollConfig.setColor(scrollColor);
    }


    public NavigableChartConfig(ChartConfig chartConfig, ChartConfig navigatorConfig, ScrollConfig scrollConfig) {
        this.chartConfig = chartConfig;
        this.navigatorConfig = navigatorConfig;
        this.scrollConfig = scrollConfig;
    }

    public NavigableChartConfig(NavigableChartConfig config) {
        chartConfig = new ChartConfig(config.chartConfig);
        navigatorConfig = new ChartConfig(config.navigatorConfig);
        scrollConfig = new ScrollConfig(config.scrollConfig);
        backgroundColor = config.backgroundColor;
    }

    public ChartConfig getChartConfig() {
        return chartConfig;
    }

    public ChartConfig getNavigatorConfig() {
        return navigatorConfig;
    }

    public ScrollConfig getScrollConfig() {
        return scrollConfig;
    }

    public BColor getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(BColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
