package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.Insets;

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
        chartConfig.setPrimaryYPosition(YAxisPosition.RIGHT);
        chartConfig.setPrimaryXPosition(XAxisPosition.TOP);

        navigatorConfig.getYAxisConfig().setTickLabelOutside(false);
        navigatorConfig.setPrimaryYPosition(YAxisPosition.RIGHT);
        navigatorConfig.setBackgroundColor(navigatorBgColor);
        navigatorConfig.setMarginColor(navigatorMarginColor);
        navigatorConfig.setStackGap(0);
        navigatorConfig.setDefaultStackWeight(2);
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
