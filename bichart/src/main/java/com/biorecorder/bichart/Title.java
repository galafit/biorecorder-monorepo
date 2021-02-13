package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BText;
import com.biorecorder.bichart.graphics.TextMetric;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.utils.StringUtils;

import java.util.ArrayList;

class Title {
    private String title;
    private TitleConfig config;
    private ArrayList<BText> lines = new ArrayList<BText>();
    private int height;
    private boolean isDirty;

    public Title(TitleConfig config) {
        this.config = config;
    }
    
    private void invalidate() {
        isDirty = true;
    }
    
    public int getHeight(RenderContext renderContext, int width) {
        if(isDirty) {
            validate(renderContext, width);
        }
        return height;
    }

    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }

    public void setConfig(TitleConfig config) {
        this.config = config;
        invalidate();
    }

    public boolean isNullOrBlank() {
        return StringUtils.isNullOrBlank(title);
    }


    public void validate(RenderContext renderContext, int width){
        lines = new ArrayList<BText>();
        height = 0;
        if(StringUtils.isNullOrBlank(title)) {
            return;
        }

        String[] words = title.split(" ");
        StringBuilder stringBuilder = new StringBuilder(words[0]);
        TextMetric tm = renderContext.getTextMetric(config.getTextStyle());
        Insets margin = config.getMargin();
        int y = margin.top();
        int x;
        for (int i = 1; i < words.length; i++) {
            int strWidth = tm.stringWidth(stringBuilder + " "+ words[i]);
            if ( strWidth + margin.left() + margin.right() > width ){
                String lineString = stringBuilder.toString();
                x = width / 2 - tm.stringWidth(lineString) / 2;
                if (x < margin.left()) {
                    x = margin.left();
                }
                lines.add(new BText(lineString, x, y + tm.ascent()));

                y += config.getInterLineSpace() + tm.height();
                stringBuilder = new StringBuilder(words[i]);
            } else {
                stringBuilder.append(" ").append(words[i]);
            }
        }

        // last line
        String lineString = stringBuilder.toString();
        x = width / 2 - tm.stringWidth(lineString) / 2;
        if (x <  margin.left()) {
            x = margin.left();
        }

        lines.add(new BText(lineString, x, y + tm.ascent()));

        height = tm.height() * lines.size()
                + config.getInterLineSpace() * (lines.size() - 1)
                + margin.top() + margin.bottom();
        isDirty = false;
     }

    public void draw(BCanvas canvas){
        if (lines.size() == 0){
            return;
        }
        canvas.setTextStyle(config.getTextStyle());
        canvas.setColor(config.getTextColor());
        for (BText string : lines) {
            string.draw(canvas);
        }
    }

}
