package com.biorecorder.data.datatable;

public class FloatColumn {
    public static float roundDouble2float(double d) {
        double d_abs = Math.abs(d);
        if( d_abs >= Double.MIN_VALUE && d_abs <= Float.MIN_VALUE){
            if(d > 0) {
                return Float.MIN_VALUE;
            } else {
                return -Float.MIN_VALUE;
            }
        }
        if(d > Float.MAX_VALUE) {
            return Float.MAX_VALUE;
        }
        if(d < -Float.MAX_VALUE) {
            return -Float.MAX_VALUE;
        }
        return (float) d;
    }
}
