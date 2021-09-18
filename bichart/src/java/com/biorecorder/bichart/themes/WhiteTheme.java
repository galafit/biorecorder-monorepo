package com.biorecorder.bichart.themes;

import com.biorecorder.bichart.configs.ChartConfig;
import com.biorecorder.bichart.configs.BiChartConfig;
import com.biorecorder.bichart.axis.AxisConfig;
import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.configs.ScrollConfig;

/**
 * Created by galafit on 24/2/19.
 */
public class WhiteTheme {
    public static final BColor BLUE = new BColor(32, 120, 179);
    public static final BColor ORANGE = new BColor(255, 127, 15);
    public static final BColor GREEN = new BColor(44, 158, 44);
    public static final BColor VIOLET = new BColor(148, 104, 189);
    public static final BColor BROWN = new BColor(138, 85, 74);
    public static final BColor ROSE = new BColor(227, 120, 193);
    public static final BColor GRAY = new BColor(125, 125, 125);
    public static final BColor KHAKI = new BColor(189, 189, 34);
    public static final BColor EMERALD = new BColor(25, 191, 207);
    private static final BColor[] COLORS = {BLUE, ORANGE, GREEN, VIOLET, BROWN, ROSE, GRAY, KHAKI, EMERALD};

    private static final int X_MARK_SIZE = 4;
    private static final int Y_MARK_SIZE = 6;

    private static final BColor BG_COLOR = BColor.WHITE_OBSCURE_LIGHT;
    private static final BColor MARGIN_COLOR = BColor.WHITE_OBSCURE;

    private static final BColor TEXT_COLOR = new BColor(70, 60, 60);
    private static final BColor AXIS_COLOR = new BColor(160, 160, 160);
    private static final BColor GRID_COLOR = new BColor(215, 215, 215);
    private static final BColor CROSSHAIR_COLOR = new BColor(100, 95, 95);

    /*     final BColor BLUE = new BColor(0, 130, 230);
           final BColor ORANGE = new BColor(235, 80, 0); //new BColor(250, 100, 30);
           final BColor GREEN_DARK = new BColor(0, 130, 0);
           final BColor MAGENTA = new BColor(150, 50, 185);
           final BColor RED = new BColor(250, 60, 90); //new BColor(230, 10, 60);
           final BColor BLUE_DARK = new BColor(30, 30, 180);
           final BColor PINK = new BColor(230, 0, 230);
           final BColor RED_DARK = new BColor(180, 0, 0);
           final BColor CYAN = new BColor(0, 160, 160);
           final BColor GRAY = new BColor(120, 56, 7); //new BColor(60, 70, 100);
           */

    public static ChartConfig getChartConfig() {
        AxisConfig xAxisConfig = new AxisConfig();
        xAxisConfig.setColors(AXIS_COLOR, TEXT_COLOR, GRID_COLOR, GRID_COLOR);
        xAxisConfig.setTickMarkSize(X_MARK_SIZE, 0);
        xAxisConfig.setCrosshairLineColor(CROSSHAIR_COLOR);
        xAxisConfig.setAxisLineWidth(0);
        //xAxisConfig.setMinorTickIntervalCount(3);

        AxisConfig yAxisConfig = new AxisConfig();
        yAxisConfig.setColors(AXIS_COLOR, TEXT_COLOR, GRID_COLOR, GRID_COLOR);
        yAxisConfig.setTickMarkSize(Y_MARK_SIZE, 0);
        yAxisConfig.setCrosshairLineColor(CROSSHAIR_COLOR);
        yAxisConfig.setAxisLineWidth(0);
        //yAxisConfig.setMinorTickIntervalCount(3);

        ChartConfig chartConfig = new ChartConfig();
        chartConfig.setTraceColors(COLORS);
        chartConfig.setBackgroundColor(BG_COLOR);
        chartConfig.setMarginColor(MARGIN_COLOR);
        chartConfig.getTitleConfig().setTextColor(TEXT_COLOR);
        chartConfig.setYAxisConfig(yAxisConfig);
        chartConfig.setXAxisConfig(xAxisConfig);
        chartConfig.getLegendConfig().setBackgroundColor(BG_COLOR);
        return chartConfig;
    }

    public static BiChartConfig getNavigableChartConfig() {
        BColor navigatorBgColor = MARGIN_COLOR;
        BColor navigatorMarginColor = navigatorBgColor;
        BColor scrollColor = CROSSHAIR_COLOR;
        BColor bgColor = navigatorBgColor;

        ChartConfig navigatorConfig = getChartConfig();
        navigatorConfig.setBackgroundColor(navigatorBgColor);
        navigatorConfig.setMarginColor(navigatorMarginColor);
        navigatorConfig.getTitleConfig().setTextColor(TEXT_COLOR);
        navigatorConfig.getLegendConfig().setBackgroundColor(navigatorBgColor);
        navigatorConfig.getYAxisConfig().setTickLabelOutside(false);

        ScrollConfig scrollConfig = new ScrollConfig();
        scrollConfig.setColor(scrollColor);

        ChartConfig chartConfig1 = getChartConfig();
        chartConfig1.getYAxisConfig().setTickLabelOutside(false);

        BiChartConfig biChartConfig = new BiChartConfig(chartConfig1, navigatorConfig, scrollConfig);
        biChartConfig.setBackgroundColor(bgColor);
        return biChartConfig;
    }

}
