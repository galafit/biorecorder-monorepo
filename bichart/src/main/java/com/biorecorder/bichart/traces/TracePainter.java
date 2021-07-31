package com.biorecorder.bichart.traces;

import com.biorecorder.bichart.ChartData;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;

/**
 * https://js.devexpress.com/Documentation/ApiReference/Data_Visualization_Widgets/dxChart/Configuration/argumentAxis/.
 * Normally xAxis is argument axis and yAxis - value axis.
 * And only for inverted traces vice versa
 */
public interface TracePainter {
      /**
       * LineTrace, AreaTrace, ColumnTrace... - TraceType.LINEAR
       * ScatterTrace, BubbleTrace... - TraceType.SCATTER
       */
      TraceType traceType();

      int markWidth();

      Range yMinMax(ChartData data);

      Range xMinMax(ChartData data);

      BPoint getCrosshairPoint(ChartData data, int dataIndex, Scale xScale, Scale yScale);

      BRectangle getHoverArea(ChartData data, int dataIndex, Scale xScale, Scale yScale);

      String[] getTooltipInfo(ChartData data, int dataIndex, Scale xtScale, Scale yScale);

      void drawTrace(BCanvas canvas, ChartData data, Scale xScale, Scale yScale, BColor traceColor);
}
