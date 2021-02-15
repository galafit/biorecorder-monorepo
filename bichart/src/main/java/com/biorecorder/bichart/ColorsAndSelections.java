package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BColor;

interface ColorsAndSelections {
    BColor getColor(int index);
    boolean isSelected(int index);
}
