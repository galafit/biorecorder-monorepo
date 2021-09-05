package com.biorecorder.bichart.swing;

import com.biorecorder.bichart.graphics.RenderContext;
import com.biorecorder.bichart.graphics.TextMetric;
import com.biorecorder.bichart.graphics.TextStyle;

import javax.swing.*;
import java.awt.*;

public class SwingRenderContext implements RenderContext {
    public static Font getFont(TextStyle textStyle) {
        String fontName = textStyle.getFontName();
        if (fontName == TextStyle.DEFAULT) {
            fontName = new JLabel().getFont().getFontName();
        }
        int style = Font.PLAIN;
        if (textStyle.isBold()) {
            style += Font.BOLD;
        }
        if (textStyle.isItalic()) {
            style += Font.ITALIC;
        }
        return new Font(fontName, style, textStyle.getSize());
    }

    @Override
    public TextMetric getTextMetric(TextStyle textStyle) {
        return new TextMetric() {
            FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont(textStyle));
            @Override
            public int ascent() {
                return fm.getAscent();
            }

            @Override
            public int descent() {
                return fm.getDescent();
            }

            @Override
            public int height() {
                return fm.getHeight();
            }

            @Override
            public int stringWidth(String str) {
                if (str != null) {
                    return fm.stringWidth(str);
                }
                return 0;
            }
        };
    }
}
