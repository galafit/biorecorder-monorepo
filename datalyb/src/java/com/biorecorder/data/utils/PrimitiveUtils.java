package com.biorecorder.data.utils;

/**
 * A class that defines "round" and "convert" methods for different types
 */
public class PrimitiveUtils {

    public static int intMinValue() {
        return 1;
    }

    public static short shortMinValue() {
        return 1;
    }

    public static long longMinValue() {
        return 1;
    }

    public static float floatMinValue() {
        return Float.MIN_VALUE;
    }

    public static double doubleMinValue() {
        return Double.MIN_VALUE;
    }





    public static int long2int(long l) {
        if(l > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if(l < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) l;
    }

    public static short long2short(long l) {
        if(l > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }
        if(l < Short.MIN_VALUE) {
            return Short.MIN_VALUE;
        }
        return (short) l;
    }

    public static byte long2byte(long l) {
        if(l > Byte.MAX_VALUE) {
            return Byte.MAX_VALUE;
        }
        if(l < Byte.MIN_VALUE) {
            return Byte.MIN_VALUE;
        }
        return (byte) l;
    }

    public static long round(double d) {
        return Math.round(d);
    }

    public static int round(float d) {
        return Math.round(d);
    }

    public static long round(long d) {
        return d;
    }

    public static int round(int d) {
        return d;
    }

    public static short round(short d) {
        return d;
    }

    public static byte round(byte d) {
        return d;
    }

    public static double roundDouble2double(double d) {
        return d;
    }

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

    public static long roundDouble2long(double d) {
        return Math.round(d);
    }

    public static int roundDouble2int(double d) {
        return long2int(roundDouble2long(d));
    }

    public static short roundDouble2short(double d) {
        return long2short(roundDouble2long(d));
    }

    public static byte roundDouble2byte(double d) {
        return long2byte(roundDouble2long(d));
    }


    public static double double2double(double d) {
        return d;
    }

    public static float double2float(double d) {
        return roundDouble2float(d);
    }

    public static long double2long(double d) {return (long)(d);}

    public static int double2int(double d) {
        return long2int(double2long(d));
    }

    public static short double2short(double d) {
        return long2short(double2long(d));
    }

    public static byte double2byte(double d) {
        return long2byte(double2long(d));
    }


    public static void main(String[] args) {
 
        double d = 4786777867867868654674678346734763478673478654478967.77;
        d = -Double.MIN_VALUE;
        System.out.println("float " + (float)d);
        System.out.println("long " + (long)d);
        System.out.println("int   " + (int)d);
        System.out.println("short " + (short)d);
        System.out.println("byte  " + (byte)d);

        System.out.println();
        System.out.println("float  " + double2float(d));
        System.out.println("long   " + double2long(d));
        System.out.println("int    " +  double2int(d));
        System.out.println("short  " + double2short(d));
        System.out.println("byte   " + double2byte(d));

        d = -4786777867867868654674678346734763478673478654478967.77;
        System.out.println();
        System.out.println("float  " + double2float(d));
        System.out.println("long   " + double2long(d));
        System.out.println("int    " +  double2int(d));
        System.out.println("short  " + double2short(d));
        System.out.println("byte   " + double2byte(d));
    }
}
