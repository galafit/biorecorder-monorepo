package com.biorecorder.bichart.utils;

/**
 * Created by galafit on 7/4/19.
 */
public class NormalizedNumber {
    private static final int MAX_MANTISSA_SIZE = 17;
    private long digits;
    private int exponent;
    private final double value;


    public NormalizedNumber(long digits1, int exponent1) {
        this.digits = digits1;
        this.exponent = exponent1;
        normalize();
        value = calculateValue(digits, exponent);
    }

    public NormalizedNumber(double value) {
        digits = 0;
        exponent = 0;
        this.value = value;
        if(value != 0) {
            int firstDigitPower = firstDigitExponent(value);
            for(int size = 0; size <= MAX_MANTISSA_SIZE; size++) {
                exponent = firstDigitPower - size;
                if(exponent < 0) {
                    digits = (long)(value * Math.pow(10, -exponent));
                } else {
                    digits = (long)(value / Math.pow(10, exponent));
                }

                if(value == new NormalizedNumber(digits, exponent).value()) {
                    break;
                }
            }
        }
        normalize();
    }

    private void normalize() {
        if(digits != 0) {
            while(digits % 10 == 0) {
                digits /=  10;
                exponent++;
            }
        } else {
            exponent = 0;
        }
    }

    private double calculateValue(long digits, int exponent) {
        String s = digits+"E"+exponent;
        return Double.valueOf(s);
    }

    public int compare(double d) {
        return Double.compare(value, d);
    }

    public NormalizedNumber multiply(long scaleFactor) {
        return new NormalizedNumber(digits * scaleFactor, exponent);
    }

    public long upperScaleFactor(double d) {
        //return (int) Math.ceil(value * Math.pow(10, -exponent) / digits);
        long scaleFactor = (long)(d * Math.pow(10, -exponent)) / digits;

        if(d > new NormalizedNumber(digits * scaleFactor,  exponent).value()) {
            scaleFactor++;
        }
        return scaleFactor;
    }

    public long lowerScaleFactor(double d) {
        //return (int) Math.floor(value * Math.pow(10, -exponent) / digits);
        long scaleFactor = (long)(d * Math.pow(10, -exponent)) / digits;
        if(d < new NormalizedNumber(digits * scaleFactor,  exponent).value()) {
            scaleFactor--;
        }
        return scaleFactor;
    }

    public long mantissaDigits() {
        return digits;
    }

    public int exponent() {
        return exponent;
    }

    public static int firstDigitExponent(double value){
        if (value == 0) {return 1;}
        double power = Math.log10(Math.abs(value));
        int powerInt = (int) power;
        if ((power < 0) && (power != powerInt)) {
            powerInt = powerInt - 1;
        }
        return powerInt;
    }

    public double value() {
       return value;
    }

    /**
     * Unit tests {@code NormalizedNumber}.
     *
     * String.format: https://www.dotnetperls.com/format-java
     */
    public static void main(String[] args) {
        String format = "%1$10s :   Digits = %2$9s exponent = %3$3s";
        double value = 101;
        NormalizedNumber number = new NormalizedNumber(value);
        System.out.println(String.format(format, value, number.mantissaDigits(), number.exponent()));

        value = 0.03234234;
        number = new NormalizedNumber(value);
        System.out.println(String.format(format, value, number.mantissaDigits(), number.exponent()));

        value = -7000.15;
        number = new NormalizedNumber(value);
        System.out.println(String.format(format, value, number.mantissaDigits(), number.exponent()));

        value = 0.33;
        number = new NormalizedNumber(value);
        System.out.println(String.format(format, value, number.mantissaDigits(), number.exponent()));

        value = -0.11001;
        number = new NormalizedNumber(value);
        System.out.println(String.format(format, value, number.mantissaDigits(), number.exponent()));

        value = -101.999999;
        number = new NormalizedNumber(value);
        System.out.println(String.format(format, value, number.mantissaDigits(), number.exponent()));

        value = 0;
        number = new NormalizedNumber(value);
        System.out.println(String.format(format, value, number.mantissaDigits(), number.exponent()));

        value = 100000001.0000001;
        number = new NormalizedNumber(value);
        System.out.println(String.format(format, value, number.mantissaDigits(), number.exponent()));

    }
}
