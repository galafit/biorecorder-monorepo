package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.utils.StringUtils;

class Title {
    int x = 0;
    int y = 0;
    int width = 0;
    private String title;
    private TitleConfig config;
    private TitlePainter painter;

    public Title(String title, TitleConfig config) {
        this.config = config;
        this.title = title;
    }

    private void invalidate() {
        painter = null;
    }

    public void setWidth(int width) {
        if(this.width != width) {
            this.width = width;
            invalidate();
        }
    }

    private void revalidate(RenderContext renderContext) {
        if(painter == null) {
          painter = new TitlePainter(renderContext, config, title, x, y, width);
        }
    }

    /**
     * move top left corner to the given point (x, y)
     */
    public void moveTo(int x, int y)  {
        if(painter != null) {
            int dx = x - this.x;
            int dy = y - this.y;
            painter.move(dx, dy);
        }
        this.x = x;
        this.y = y;
    }

    public BDimension getPrefferedSize(RenderContext renderContext) {
        if (isNullOrBlank()) {
            return new BDimension(0, 0);
        }
        revalidate(renderContext);
        return painter.getPrefferedSize();
    }

    public void setConfig(TitleConfig config) {
        this.config = config;
        invalidate();
    }

    public boolean isNullOrBlank() {
        return StringUtils.isNullOrBlank(title);
    }

    public void draw(BCanvas canvas) {
        if (isNullOrBlank()) {
            return;
        }
        revalidate(canvas.getRenderContext());
        painter.draw(canvas);
    }
}
