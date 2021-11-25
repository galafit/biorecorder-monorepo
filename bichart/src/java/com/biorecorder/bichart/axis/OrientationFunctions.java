package com.biorecorder.bichart.axis;

import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Tick;

import java.util.List;

/**
 * Created by galafit on 9/6/19.
 */
interface OrientationFunctions {
      int labelSizeForOverlap(TextMetric tm, String label);

      int labelSizeForWidth(TextMetric tm, String label);

      void translateCanvas(BCanvas canvas, BRectangle area);

      BText createTickLabel(TextMetric tm, int tickPosition, String tickLabel, int start, int end, int tickPixelInterval, AxisConfig config, int interLabelGap, boolean isCategory);

      BLine createTickLine(int tickPosition, int axisLineWidth, int insideSize, int outsideSize);

      BLine createGridLine(int tickPosition, BRectangle area);

      BLine createAxisLine(int start, int end);

      BText createTitle(String title, TextMetric tm, int start, int end, int width);

      boolean contains(int point, int start, int end);

      boolean isVertical();
}
