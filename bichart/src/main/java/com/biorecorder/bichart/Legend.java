package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.*;

class Legend {
    private LegendConfig config;
    private TraceList traceList;
    private LegendPainter painter;
    private BRectangle area;

    public Legend(LegendConfig config, TraceList traceList, BRectangle area) {
        this.config = config;
        this.traceList = traceList;
        this.area = area;
        traceList.addChangeListener(new ChangeListener() {
            @Override
            public void onChange() {
                invalidate();
            }
        });
        traceList.addSelectionListener(new SelectionListener() {
            @Override
            public void onSelectionChanged() {
                if(painter != null) {
                    painter.setSelection(traceList.getSelection());
                }
            }
        });
    }

    public boolean selectTrace(int x, int y) {
        if(!config.isEnabled()) {
            return false;
        }
        if(painter != null) {
            int index = painter.findButton(x, y);
            if(index >= 0) {
                int currentSelection = traceList.getSelection();
                if(index == currentSelection) {
                    traceList.setSelection(-1);
                } else {
                    traceList.setSelection(index);
                }
                return true;
            }
        }
        return false;
    }


    public void setArea(BRectangle area) {
        this.area = area;
        invalidate();
    }

    public int getLegendHeight(RenderContext renderContext) {
        if(!config.isEnabled() || config.isAttachedToStacks()) {
            return 0;
        }
        revalidate(renderContext);
        return painter.getLegendHeight();
    }

    public void revalidate(RenderContext renderContext) {
        if(config.isEnabled() && painter == null) {
            painter = new LegendPainter(renderContext, traceList, config, area);
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