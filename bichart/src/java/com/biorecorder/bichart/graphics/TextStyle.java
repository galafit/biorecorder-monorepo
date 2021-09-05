package com.biorecorder.bichart.graphics;


/**
 * Created by galafit on 18/8/17.
 */
public class TextStyle {
    public static final String SANS_SERIF = "SansSerif";
    public static final String SERIF = "Serif";
    public static final String MONOSPACED = "Monospaced";
    public static final String DEFAULT = "Default";

    public static final int NORMAL = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int BOLD_ITALIC = 3;

    private final int size;
    private final int style;
    private final String fontName;

    public TextStyle(String fontName, int style, int size) {
        this.size = size;
        this.style = style;
        this.fontName = fontName;
    }

    public int getSize() {
        return size;
    }

    public String getFontName() {
        return fontName;
    }

    public boolean isBold() {
        if(style == 1 || style == 3) {
            return true;
        }
        return false;
    }

    public boolean isItalic() {
        if(style == 2 || style == 3) {
            return true;
        }
        return false;
    }
}
