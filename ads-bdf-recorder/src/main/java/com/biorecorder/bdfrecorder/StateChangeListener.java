package com.biorecorder.bdfrecorder;

import com.sun.istack.internal.Nullable;

public interface StateChangeListener {
    void onStateChanged(@Nullable Message message);
}
