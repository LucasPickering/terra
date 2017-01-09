package me.lucaspickering.terraingen.render;

import java.awt.FontFormatException;
import java.io.IOException;

import me.lucaspickering.terraingen.util.Constants;

public enum Font {

    DEBUG(Constants.FONT_BOMBARDIER, 30),
    TILE(Constants.FONT_BOMBARDIER, 60),
    UI(Constants.FONT_BOMBARDIER, 100),
    LARGE(Constants.FONT_BOMBARDIER, 150),
    TITLE(Constants.FONT_BOMBARDIER, 250);

    private final String fontName;
    private final int fontHeight;

    Font(String fontName, int fontHeight) {
        this.fontName = fontName;
        this.fontHeight = fontHeight;
    }

    public TrueTypeFont load() throws IOException, FontFormatException {
        return new TrueTypeFont(this);
    }

    public String getFontName() {
        return fontName;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    @Override
    public String toString() {
        return String.format("%s: [fontName=%s, size=%d]", name(), fontName, fontHeight);
    }
}
