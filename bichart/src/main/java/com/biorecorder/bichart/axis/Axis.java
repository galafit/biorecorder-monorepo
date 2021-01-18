package com.biorecorder.bichart.axis;

import com.biorecorder.bichart.graphics.BText;
import com.biorecorder.bichart.graphics.TextMetric;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.*;
import com.biorecorder.bichart.utils.StringUtils;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.data.sequence.StringSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Axis is visual representation of Scale that generates and draws
 * visual elements such as axis lines, labels, and ticks.
 * Also it is a wrapper class that gives simplified access to the Scale methods
 * and some advanced functionality such as Zooming and Translating. See
 * {@link #setConfig(AxisConfig)}
 */
public class Axis {
    public static int MIN_TICK_COUNT = 11;
    private static final int MAX_TICKS_COUNT = 500; // if bigger it means that there is some error

    private Scale scale;
    private String title;
    private AxisConfig config;
    private Orientation orientation;

    private List<BText> tickLabels = new ArrayList<>();
    private IntArrayList tickPositions = new IntArrayList();
    private IntArrayList minorTickPositions = new IntArrayList();
    private BText titleText;
    private boolean isDirty = true;
    private int widthOut;

    public Axis(Scale scale, AxisConfig axisConfig, XAxisPosition xAxisPosition) {
        this.scale = scale.copy();
        this.config = axisConfig;
        switch (xAxisPosition) {
            case TOP:
                orientation = new TopOrientation();
                break;
            case BOTTOM:
                orientation = new BottomOrientation();
        }
    }

    public Axis(Scale scale, AxisConfig axisConfig, YAxisPosition yAxisPosition) {
        this.scale = scale.copy();
        this.config = axisConfig;
        switch (yAxisPosition) {
            case LEFT:
                orientation = new LeftOrientation();
                break;
            case RIGHT:
                orientation = new RightOrientation();
        }
    }

    public int getWidthOut() {
        return widthOut;
    }

    public void invalidate() {
        isDirty = true;
    }

    private boolean isTooShort() {
        int lengthMin = config.getTickLabelTextStyle().getSize() * 3;
        if (length() > lengthMin) {
            return false;
        }
        return true;
    }


    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }

    /**
     * set Axis scale. Inner scale is a COPY of the given scale
     * to prevent direct access from outside
     *
     * @param scale
     */
    public void setScale(Scale scale) {
        this.scale = scale.copy();
        invalidate();
    }

    /**
     * get the COPY of inner scale
     *
     * @return copy of inner scale
     */
    public Scale getScale() {
        return scale.copy();
    }

    /**
     * get the COPY of inner config. To change axis config
     * use {@link #setConfig(AxisConfig)}
     *
     * @return copy of inner config
     */
    public AxisConfig getConfig() {
        return new AxisConfig(config);
    }

    /**
     * set Axis config. Inner config is a COPY of the given config
     * to prevent direct access from outside
     *
     * @param config
     */
    public void setConfig(AxisConfig config) {
        // set a copy to safely change
        this.config = new AxisConfig(config);
        titleText = null;
        invalidate();
    }

    public boolean isTickLabelOutside() {
        return config.isTickLabelOutside();
    }

    public String getTitle() {
        return title;
    }

    public boolean isRoundingEnabled() {
        return config.isRoundingEnabled();
    }


    /**
     * Zoom does not affect the axis scale!
     * It copies the axis scales and transforms its domain respectively.
     * Note: zoom affects only max value, min value does not change!!!
     *
     * @param zoomFactor
     * @return new scale with transformed domain
     */
    public Scale zoom(double zoomFactor) {
        if (zoomFactor <= 0) {
            String errMsg = "Zoom factor = " + zoomFactor + "  Expected > 0";
            throw new IllegalArgumentException(errMsg);
        }
        Scale zoomedScale = scale.copy();

        double start = getStart();
        double end = getEnd();

        double zoomedLength = (end - start) * zoomFactor;
        double zoomedEnd = start + zoomedLength;
        zoomedScale.setStartEnd(start, zoomedEnd);
        double maxNew = zoomedScale.invert(end);
        zoomedScale.setMinMax(getMin(), maxNew);
        return zoomedScale;
    }


    /**
     * Zoom does not affect the axis scale!
     * It copies the axis scales and transforms its domain respectively.
     *
     * @param translation
     * @return new scale with transformed domain
     */
    public Scale translate(int translation) {
        Scale translatedScale = scale.copy();

        double start = getStart();
        double end = getEnd();
        double minNew = translatedScale.invert(start + translation);
        double maxNew = translatedScale.invert(end + translation);
        translatedScale.setMinMax(minNew, maxNew);
        return translatedScale;
    }

    /**
     * Format domain value according to the minMax one "point precision"
     * cutting unnecessary double digits that exceeds that "point precision"
     */
    public String formatDomainValue(double value) {
        return scale.formatDomainValue(value);
    }

    public boolean setMinMax(double min, double max) {
        scale.setMinMax(min, max);
        invalidate();
        return true;
    }

    public boolean setStartEnd(double start, double end) {
        scale.setStartEnd(start, end);
        invalidate();
        return true;
    }

    public double getMin() {
        return scale.getMin();
    }

    public double getMax() {
        return scale.getMax();
    }

    public double getStart() {
        return scale.getStart();
    }

    public double getEnd() {
        return scale.getEnd();
    }

    public double scale(double value) {
        return scale.scale(value);
    }

    public double invert(double value) {
        return scale.invert(value);
    }

    public double length() {
        return Math.abs(getEnd() - getStart());
    }

    public double getBestExtent(RenderContext renderContext, int length) {
        if (scale instanceof CategoryScale) {
            TextMetric tm = renderContext.getTextMetric(config.getTickLabelTextStyle());
            StringSequence labels = ((CategoryScale) scale).getLabels();
            if(labels != null && labels.size() > 0) {
                List<Tick> ticks = new ArrayList<>(labels.size());
                for (int i = 0; i < labels.size(); i++) {
                    ticks.add(new Tick(i, labels.get(i)));
                }

                int requiredSpaceForTickLabel = getRequiredSpaceForTickLabel(tm, ticks);
                int bestLength = labels.size() * requiredSpaceForTickLabel + getInterLabelGap();
                bestLength = Math.max(bestLength, length);
                Scale s = new CategoryScale(labels);
                s.setMinMax(0, labels.size());
                s.setStartEnd(0, bestLength);
                return s.invert(length);
            }
        }
        return -1;
    }


    public void drawCrosshair(BCanvas canvas, BRectangle area, int position) {
        canvas.save();
        orientation.translateCanvas(canvas, area);
        canvas.setColor(config.getCrosshairLineColor());
        canvas.setStroke(config.getCrosshairLineWidth(), config.getCrosshairLineDashStyle());
        canvas.drawLine(orientation.createGridLine(position, area));
        canvas.restore();
    }

    public void drawGrid(BCanvas canvas, BRectangle area) {
        if (isTooShort()) {
            return;
        }
        canvas.save();
        orientation.translateCanvas(canvas, area);

        canvas.setColor(config.getGridColor());
        canvas.setStroke(config.getGridLineWidth(), config.getGridLineDashStyle());
        for (int i = 0; i < tickPositions.size(); i++) {
            canvas.drawLine(orientation.createGridLine(tickPositions.get(i), area));
        }

        canvas.setColor(config.getMinorGridColor());
        canvas.setStroke(config.getMinorGridLineWidth(), config.getMinorGridLineDashStyle());
        for (int i = 0; i < minorTickPositions.size(); i++) {
            canvas.drawLine(orientation.createGridLine(minorTickPositions.get(i), area));
        }

        canvas.restore();
    }

    public void drawAxis(BCanvas canvas, BRectangle area) {
        canvas.save();
        orientation.translateCanvas(canvas, area);

        if (!isTooShort()) {
            if (config.getTickMarkInsideSize() > 0 || config.getTickMarkOutsideSize() > 0) {
                canvas.setColor(config.getTickMarkColor());
                canvas.setStroke(config.getTickMarkWidth(), DashStyle.SOLID);
                for (int i = 0; i < tickPositions.size(); i++) {
                    canvas.drawLine(orientation.createTickLine(tickPositions.get(i), config.getAxisLineWidth(),config.getTickMarkInsideSize(), config.getTickMarkOutsideSize()));
                }
            }

            if (config.getMinorTickMarkInsideSize() > 0 || config.getMinorTickMarkOutsideSize() > 0) {
                canvas.setColor(config.getMinorTickMarkColor());
                canvas.setStroke(config.getMinorTickMarkWidth(), DashStyle.SOLID);
                for (int i = 0; i < minorTickPositions.size(); i++) {
                    canvas.drawLine(orientation.createTickLine(minorTickPositions.get(i), config.getAxisLineWidth(), config.getMinorTickMarkInsideSize(), config.getMinorTickMarkOutsideSize()));
                }
            }

            canvas.setStroke(1, DashStyle.SOLID);
            canvas.setColor(config.getTickLabelColor());
            canvas.setTextStyle(config.getTickLabelTextStyle());
            for (BText tickLabel : tickLabels) {
                tickLabel.draw(canvas);
            }

            if(titleText != null) {
                canvas.setColor(config.getTitleColor());
                canvas.setTextStyle(config.getTitleTextStyle());
                titleText.draw(canvas);
            }
        }

        if (config.getAxisLineWidth() > 0) {
            canvas.setColor(config.getAxisLineColor());
            canvas.setStroke(config.getAxisLineWidth(), config.getAxisLineDashStyle());
            canvas.drawLine(orientation.createAxisLine((int)Math.round(getStart()), (int)Math.round(getEnd())));
        }

        canvas.restore();
    }


    private boolean isTickIntervalSpecified() {
        return config.getTickInterval() > 0;
    }


    private List<Tick> generateTicks(TickProvider tickProvider) {
        double min = getMin();
        double max = getMax();

        Tick tickMax;
        Tick tickMin;
        if (config.isRoundingEnabled()) {
            tickMax = tickProvider.getUpperTick(max);
            tickMin = tickProvider.getLowerTick(min);
        } else {
            tickMax = tickProvider.getLowerTick(max);
            tickMin = tickProvider.getUpperTick(min);
        }
        Tick tickMinNext = tickProvider.getNextTick();

        int tickCount = 0;
        if (tickMax.getValue() > tickMin.getValue()) {
            double tickPixelInterval = scale(tickMinNext.getValue()) - scale(tickMin.getValue());
            int tickIntervalCount = (int) Math.abs(Math.round(Math.abs(scale(tickMax.getValue()) - scale(tickMin.getValue())) / tickPixelInterval));
            tickCount = tickIntervalCount + 1;
        }

        if (tickCount > MAX_TICKS_COUNT) {
            String errMsg = "Too many ticks: " + tickCount + ". Expected < " + MAX_TICKS_COUNT;
            throw new RuntimeException(errMsg);
        }

        List<Tick> ticks = new ArrayList<>();
        if (tickCount >= 2) {
            ticks.add(tickMin);
            ticks.add(tickMinNext);
            for (int i = 2; i < tickCount; i++) {
                ticks.add(tickProvider.getNextTick());
            }
        }
        return ticks;
    }

    // create ticks and fix overlapping
    private List<Tick> createValidTicks(TextMetric tm) {
        if (isTooShort()) {
            return new ArrayList<>(0);
        }

        TickProvider tickProvider;
        if (isTickIntervalSpecified()) {
            tickProvider = scale.getTickProviderByInterval(config.getTickInterval(), config.getTickLabelPrefixAndSuffix());
        } else {
            int tickIntervalCount;
            int fontFactor = 4;
            double tickPixelInterval = fontFactor * config.getTickLabelTextStyle().getSize();
            tickIntervalCount = (int) (length() / tickPixelInterval);
            tickIntervalCount = Math.max(tickIntervalCount, MIN_TICK_COUNT);
            tickProvider = scale.getTickProviderByIntervalCount(tickIntervalCount, config.getTickLabelPrefixAndSuffix());
        }

        double min = getMin();
        double max = getMax();
        List<Tick> ticks = generateTicks(tickProvider);

        // Calculate how many ticks need to be skipped to avoid labels overlapping.
        // When ticksSkipStep = n, only every n'th label on the axis will be shown.
        // For example if ticksSkipStep = 2 every other label will be shown.
        int ticksSkipStep = 1;

        int tickIntervalCount = ticks.size() - 1;
        if (ticks.size() >= 2) {
            double tickPixelInterval = Math.abs(scale(ticks.get(1).getValue()) - scale(ticks.get(0).getValue()));
            // calculate tick distance to avoid labels overlapping.
            int requiredSpaceForTickLabel = getRequiredSpaceForTickLabel(tm, ticks);

            if (tickPixelInterval < requiredSpaceForTickLabel) {
                if(isRoundingEnabled()) {
                    // need to take into account that some extra ticks will be added
                    int n = Math.max(1, (int)length() / requiredSpaceForTickLabel);
                    ticksSkipStep = (tickIntervalCount + tickIntervalCount % n) / n;
                } else {
                    ticksSkipStep = (int) (requiredSpaceForTickLabel / tickPixelInterval);
                    if (ticksSkipStep * tickPixelInterval < requiredSpaceForTickLabel) {
                        ticksSkipStep++;
                    }
                }

                if (ticksSkipStep > tickIntervalCount) {
                    ticksSkipStep = tickIntervalCount;
                }
            }

            if (!config.isRoundingEnabled() && ticksSkipStep > 1 && (ticks.size() - 1) / ticksSkipStep > 1) {
                tickProvider.increaseTickInterval(ticksSkipStep);
                ticks = generateTicks(tickProvider);
                ticksSkipStep = 1;
            }
        }

        // 1) skip ticks if ticksSkipStep > 1
        // 2) add 2 extra ticks: one at the beginning and one at the end to be able to create minor grid
        if (ticks.size() < 2) { // possible only if rounding disabled
            Tick tickMin = tickProvider.getUpperTick(min);

            if (tickMin.getValue() > max) {
                ticks.add(tickProvider.getPreviousTick());
                ticks.add(tickMin);
            } else {
                tickProvider.getUpperTick(min);
                ticks.add(tickProvider.getPreviousTick());
                ticks.add(tickProvider.getNextTick());
                ticks.add(tickProvider.getNextTick());
            }
        } else {
            boolean isLastExtraTickAdded = false;
            if (ticksSkipStep > 1) {
                // create extra ticks to get tickIntervalCount multiple to ticksSkipStep
                int roundExtraTicksCount = ticksSkipStep - tickIntervalCount % ticksSkipStep;
                if (roundExtraTicksCount < ticksSkipStep) {
                    for (int i = 0; i < roundExtraTicksCount; i++) {
                        ticks.add(tickProvider.getNextTick());
                    }
                    if (!config.isRoundingEnabled()) {
                        isLastExtraTickAdded = true;
                    }
                }

                List<Tick> skippedTicks = new ArrayList<>();
                for (int i = 0; i < ticks.size(); i++) {
                    if (i % ticksSkipStep == 0) {
                        skippedTicks.add(ticks.get(i));
                    }
                }
                ticks = skippedTicks;
            }

            // add extra tick at the end
            if (!isLastExtraTickAdded) {
                Tick extraTick = tickProvider.getNextTick();
                for (int i = 1; i < ticksSkipStep; i++) {
                    extraTick = tickProvider.getNextTick();
                }
                ticks.add(extraTick);
            }

            // add extra tick at the beginning
            if (config.isRoundingEnabled()) {
                tickProvider.getLowerTick(min);
            } else {
                tickProvider.getUpperTick(min);
            }
            Tick extraTick = tickProvider.getPreviousTick();
            for (int i = 1; i < ticksSkipStep; i++) {
                extraTick = tickProvider.getPreviousTick();
            }
            ticks.add(0, extraTick);
        }
        return ticks;
    }


    public void update(RenderContext renderContext) {
        tickPositions = new IntArrayList();
        minorTickPositions = new IntArrayList();
        tickLabels = new ArrayList<>();
        TextMetric labelTM = renderContext.getTextMetric(config.getTickLabelTextStyle());
        List<Tick> ticks = createValidTicks(labelTM);

        widthOut = config.getAxisLineWidth() / 2;
        if(ticks.size() == 0) {
            return;
        }

        widthOut += config.getTickMarkOutsideSize();
        if (config.isTickLabelOutside()) {
            widthOut += config.getTickPadding() + orientation.labelSizeForWidth(labelTM, ticks);

        }
        if (! StringUtils.isNullOrBlank(title)) {
            TextMetric titleTM = renderContext.getTextMetric(config.getTitleTextStyle());
            widthOut += config.getTitlePadding() + titleTM.height();
            titleText = orientation.createTitle(title,  titleTM, (int)Math.round(getStart()), (int)Math.round(getEnd()), widthOut);
        }

        int minorTickIntervalCount = config.getMinorTickIntervalCount();
        Tick currentTick = ticks.get(0);
        Tick nextTick = ticks.get(1);
        int tickPixelInterval = (int) Math.abs(scale(currentTick.getValue()) - scale(nextTick.getValue()));
        for (int tickNumber = 2; tickNumber <= ticks.size(); tickNumber++) {
            if (minorTickIntervalCount > 0) {
                // minor tick positions
                double minorTickInterval = (nextTick.getValue() - currentTick.getValue()) / minorTickIntervalCount;
                double minorTickValue = currentTick.getValue();
                for (int i = 1; i < minorTickIntervalCount; i++) {
                    minorTickValue += minorTickInterval;
                    int minorTickPosition = (int) Math.round(scale(minorTickValue));
                    if (orientation.contains(minorTickPosition, (int)Math.round(getStart()), (int)Math.round(getEnd()))) {
                        minorTickPositions.add(minorTickPosition);
                    }
                }
            }
            if (tickNumber < ticks.size()) {
                currentTick = nextTick;
                nextTick = ticks.get(tickNumber);
                int tickPosition = (int) Math.round(scale(currentTick.getValue()));
                if(orientation.contains(tickPosition, (int)Math.round(getStart()), (int)Math.round(getEnd()))) {
                    // tick position
                    tickPositions.add(tickPosition);
                    // tick label
                    tickLabels.add(orientation.createTickLabel(labelTM, tickPosition, currentTick.getLabel(), (int)Math.round(getStart()), (int)Math.round(getEnd()), tickPixelInterval, config, getInterLabelGap(), scale instanceof CategoryScale));
                }
            }
        }

        if(isRoundingEnabled()) {
            Tick tickMin = ticks.get(1);
            Tick tickMax = ticks.get(ticks.size() - 2);
            scale.setMinMax(tickMin.getValue(), tickMax.getValue());
        }

        isDirty = false;
    }

    private int getInterLabelGap() {
        return  (int)(2 * config.getTickLabelTextStyle().getSize());
    }

    private int getRequiredSpaceForTickLabel(TextMetric tm, List<Tick> ticks) {
        return  orientation.labelSizeForOverlap(tm, ticks) + getInterLabelGap();
    }
}
