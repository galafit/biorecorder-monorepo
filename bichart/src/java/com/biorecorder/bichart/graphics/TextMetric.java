package com.biorecorder.bichart.graphics;

/**
 * Created by galafit on 30/12/17.
 */
public interface TextMetric {
    public int ascent();
    public int descent();
    public int height(); // height = -ascent + descent  https://plus.google.com/u/0/photos/photo/101678222547144493932/6505396100106832050
    public int stringWidth(String str);
}
