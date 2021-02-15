package com.biorecorder.bichart;

import com.biorecorder.bichart.button.SwitchButton;
import com.biorecorder.bichart.graphics.*;

import java.util.ArrayList;
import java.util.List;

class Legend {
    private LegendConfig config;
    private TraceList traceList;
    private LegendPainter painter;
    private int width = 0;
    // top left corner
    int x = 0;
    int y = 0;
    private List<SizeChangeListener> listeners = new ArrayList<>(1);
    public Legend(LegendConfig config, TraceList traceList){
        this.config = config;
        this.traceList = traceList;
        traceList.addChangeListener(new ChangeListener() {
            @Override
            public void onChange() {
                if(!config.isAttachedToStacks()) {
                    invalidate(true);
                } else {
                    invalidate(false);
                }
            }
        });
    }

    public void onParentContainerRearranged() {
        if(config.isAttachedToStacks()) {
            invalidate(false);
        }
    }

    public void addSizeChangeListener(SizeChangeListener l) {
        listeners.add(l);
    }

    private void invalidate(boolean isSizeChanged) {
        painter = null;
        if(isSizeChanged) {
            for (SizeChangeListener l : listeners) {
                l.onSizeChanged();
            }
        }
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
        if(this.width != width) {
            this.width = width;
            if(!config.isAttachedToStacks()) {
                invalidate(true);
            } else {
                invalidate(false);
            }
        }
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

    public void setConfig(LegendConfig newConfig) {
        this.config = newConfig;
        if(config.isAttachedToStacks() && newConfig.isAttachedToStacks()) {
            invalidate(false);
        } else {
            invalidate(true);
        }
    }

    public void draw(BCanvas canvas) {
        if (!config.isEnabled()) {
            return;
        }
        revalidate(canvas.getRenderContext());
        painter.draw(canvas, new TraceColorsAndSelections(traceList));
    }

    class TraceColorsAndSelections implements ColorsAndSelections {
        private final TraceList traceList;

        public TraceColorsAndSelections(TraceList traceList) {
            this.traceList = traceList;
        }

        @Override
        public BColor getColor(int index) {
            return traceList.getColor(index);
        }

        @Override
        public boolean isSelected(int index) {
            return index == traceList.getSelection();
        }
    }
}