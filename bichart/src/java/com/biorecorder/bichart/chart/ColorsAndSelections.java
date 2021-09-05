package com.biorecorder.bichart.chart;

import com.biorecorder.bichart.graphics.BColor;

interface ColorsAndSelections {
    BColor getColor(int index);
    boolean isSelected(int index);
}
