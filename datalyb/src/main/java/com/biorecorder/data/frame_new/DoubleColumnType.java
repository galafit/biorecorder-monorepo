package com.biorecorder.data.frame_new;

public class DoubleColumnType implements ColumnType {
    private static final int BYTE_SIZE = 8;
    private static final String NAME = "DOUBLE";
    private static final BaseType baseType = BaseType.DOUBLE;
    private static DoubleColumnType INSTANCE;

    private DoubleColumnType() {
    }

    public static DoubleColumnType instance() {
        if (INSTANCE == null) {
            INSTANCE = new DoubleColumnType();
        }
        return INSTANCE;
    }

    @Override
    public BaseType getBaseType() {
        return baseType;
    }

    @Override
    public Column create(String name) {
        return new DoubleColumn(name);
    }
}
