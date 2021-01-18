package com.biorecorder.bichart.swing;

import com.biorecorder.bichart.graphics.BPath;

import java.awt.geom.GeneralPath;

/**
 * Created by galafit on 30/8/18.
 */
public class AwtPath implements BPath {
    GeneralPath path = new GeneralPath();

    @Override
    public void moveTo(float x, float y) {
        path.moveTo(x, y);

    }

    @Override
    public void lineTo(float x, float y) {
        path.lineTo(x, y);
    }

    @Override
    public void quadTo(float x1, float y1, float x2, float y2) {
        path.quadTo(x1, y1, x2, y2);
    }

    @Override
    public void close() {
        path.closePath();
    }

    GeneralPath getGeneralPath() {
        return path;
    }
}
