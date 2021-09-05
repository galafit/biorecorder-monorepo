package com.biorecorder.bichart.graphics;


/**
 * Created by galafit on 30/12/17.
 */
public interface BPath {

    public void moveTo(float x, float y);

    public void lineTo(float x, float y);

    public void quadTo(float x1, float y1, float x2, float y2);

    public void close();

}
