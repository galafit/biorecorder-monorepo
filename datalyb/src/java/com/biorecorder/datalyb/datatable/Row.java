package com.biorecorder.datalyb.datatable;

public interface Row {
    int columnCount();
    Column getColumn(int columnNumber);
    int getInt(int columnNumber);
    double getDouble(int columnNumber);
}
