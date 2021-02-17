package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.*;


class Legend {
    private LegendConfig config;
    private TraceList traceList;
    private boolean isAttachedToStacks = true;
    private LegendPainter painter;
    private int width = 0;
    // top left corner
    int x = 0;
    int y = 0;

    public Legend(LegendConfig config, TraceList traceList, boolean isAttachedToStacks){
        this.config = config;
        this.traceList = traceList;
        this.isAttachedToStacks = isAttachedToStacks;
        traceList.addChangeListener(new ChangeListener() {
            @Override
            public void onChange() {
                invalidate();
            }
        });
    }

    private void invalidate() {
        painter = null;
    }

    public void setConfig(LegendConfig config) {
        this.config = config;
        invalidate();
    }

    public boolean selectTrace(int x, int y) {
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
            invalidate();
        }
    }
    public BDimension getPrefferedSize(RenderContext renderContext) {
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
        painter = new LegendPainter(renderContext, traceList, config, isAttachedToStacks, x, y, width);
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


    public void draw(BCanvas canvas) {
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