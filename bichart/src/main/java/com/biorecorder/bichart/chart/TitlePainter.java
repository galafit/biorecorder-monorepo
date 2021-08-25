package com.biorecorder.bichart.chart;

import com.biorecorder.bichart.configs.TitleConfig;
import com.biorecorder.bichart.graphics.*;

import java.util.ArrayList;
import java.util.List;

public class TitlePainter {
    private TitleConfig config;
    private List<BText> lines = new ArrayList<BText>();
    private BDimension prefferedSize;

    public TitlePainter(RenderContext renderContext, TitleConfig config, String title, int x, int y, int width) {
        this.config = config;
        String[] words = title.split(" ");
        TextMetric tm = renderContext.getTextMetric(config.getTextStyle());
        Insets margin = config.getMargin();
        int width1 =  width - margin.left() - margin.right();
        int y1 = y + margin.top();
        StringBuilder stringBuilder = new StringBuilder(words[0]);
        String lineString;
        for (int i = 1; i < words.length; i++) {
            int strWidth = tm.stringWidth(stringBuilder + " "+ words[i]);
            if ( strWidth  > width1 ){
                lineString = stringBuilder.toString();
                int x1 = x + margin.left();
                if(config.getAlign() == HorizontalAlign.CENTER) {
                    x1 += (width1 - tm.stringWidth(lineString))/ 2;
                }
                if(config.getAlign() == HorizontalAlign.RIGHT) {
                    x1 = x + width - margin.right() - tm.stringWidth(lineString);
                }
                lines.add(new BText(lineString, x1, y1 + tm.ascent()));
                y1 += config.getInterLineSpace() + tm.height();
                stringBuilder = new StringBuilder(words[i]);
            } else {
                stringBuilder.append(" ").append(words[i]);
            }
        }

        // last line
        lineString = stringBuilder.toString();
        int x1 = x + margin.left();
        if(config.getAlign() == HorizontalAlign.CENTER) {
            x1 += (width1 - tm.stringWidth(lineString))/ 2;
        }
        if(config.getAlign() == HorizontalAlign.RIGHT) {
            x1 = x + width - margin.right() - tm.stringWidth(lineString);
        }
        lines.add(new BText(lineString, x1, y1 + tm.ascent()));

        int height = tm.height() * lines.size()
                + config.getInterLineSpace() * (lines.size() - 1)
                + margin.top() + margin.bottom();
        prefferedSize = new BDimension(width, height);

    }

    public void move(int dx, int dy) {
        for (BText line : lines) {
            line.move(dx, dy);
        }
    }

    public BDimension getPrefferedSize() {
        return prefferedSize;
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
