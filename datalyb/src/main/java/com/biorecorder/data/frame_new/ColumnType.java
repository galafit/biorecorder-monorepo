package com.biorecorder.data.frame_new;

public interface ColumnType {
    BaseType getBaseType();
    Column create(String name);
}
