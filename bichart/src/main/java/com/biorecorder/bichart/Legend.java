package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.*;

class Legend {
    private LegendConfig config;
    private TraceList traceList;
    private LegendPainter painter;
    private int width;
    // top left corner
    int x = 0;
    int y = 0;

    public Legend(LegendConfig config, TraceList traceList, int width) {
        this.config = config;
        this.traceList = traceList;
        this.width = width;
        traceList.addChangeListener(new ChangeListener() {
            @Override
            public void onChange() {
                invalidate();
            }
        });
    }

    public boolean selectTrace(int x, int y) {
        if (!config.isEnabled()) {
            return false;
        }
        if (painter != null) {
            int index = painter.findButton(x, y);
            if (index >= 0) {
                int currentSelection = traceList.getSelection();
                if (index == currentSelection) {
                    traceList.setSelection(-1);
                } else {
                    traceList.setSelection(index);
                }
                return true;
            }
        }
        return false;
    }


    public void setWidth(int width) {
        this.width = width;
        invalidate();
    }

    public BDimension getPrefferedSize(RenderContext renderContext) {
        if (!config.isEnabled() || config.isAttachedToStacks()) {
            return new BDimension(0, 0);
        }
        revalidate(renderContext);
        return painter.getPrefferedSize();
    }

    /**
     * move top left corner to the given point (x, y)
     */
    public void moveTo(int x, int y)  {
        if(painter != null) {
            int dx = x - this.x;
            int dy = y - this.y;
            painter.moveButtons(dx, dy);
        }
        this.x = x;
        this.y = y;
    }


    public void revalidate(RenderContext renderContext) {
        if (config.isEnabled() && painter == null) {
            painter = new LegendPainter(renderContext, traceList, config, x, y, width);
        }
    }

    public boolean isTop() {
        if (config.getVerticalAlign() == VerticalAlign.TOP) {
            return true;
        }
        return false;
    }

    public boolean isBottom() {
        if (config.getVerticalAlign() == VerticalAlign.BOTTOM) {
            return true;
        }
        return false;
    }

    public void setConfig(LegendConfig legendConfig) {
        this.config = legendConfig;
        invalidate();
    }

    private void invalidate() {
        painter = null;
    }

    public void draw(BCanvas canvas) {
        if (!config.isEnabled()) {
            return;
        }
        revalidate(canvas.getRenderContext());
        painter.draw(canvas);
    }
}