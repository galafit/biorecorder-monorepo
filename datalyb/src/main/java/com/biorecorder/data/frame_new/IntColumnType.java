package com.biorecorder.data.frame_new;

public class IntColumnType implements ColumnType {
    private static final int BYTE_SIZE = 4;
    private static final String NAME = "INTEGER";
    private static final BaseType baseType = BaseType.INT;
    private static IntColumnType INSTANCE;

    private IntColumnType() {
    }

    public static IntColumnType instance() {
        if (INSTANCE == null) {
            INSTANCE = new IntColumnType();
        }
        return INSTANCE;
    }

    @Override
    public BaseType getBaseType() {
        return baseType;
    }

    @Override
    public Column create(String name) {
        return new IntColumn(name);
    }

    @Override
    public boolean isNumberType() {
        return true;
    }
}
