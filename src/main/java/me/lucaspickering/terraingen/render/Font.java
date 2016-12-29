package me.lucaspickering.terraingen.render;

import java.awt.FontFormatException;
import java.io.IOException;

import me.lucaspickering.terraingen.util.Constants;

public enum Font {

    DEBUG(Constants.FONT_BOMBARDIER, 30f),
    TILE(Constants.FONT_BOMBARDIER, 60f),
    UI(Constants.FONT_BOMBARDIER, 100f),
    LARGE(Constants.FONT_BOMBARDIER, 150f),
    TITLE(Constants.FONT_BOMBARDIER, 250f);

    private final String fontName;
    private final float size;

    Font(String fontName, float size) {
        this.fontName = fontName;
        this.size = size;
    }

    public TrueTypeFont load() throws IOException, FontFormatException {
        return new TrueTypeFont(fontName, size);
    }

    @Override
    public String toString() {
        return String.format("%s: [fontName=%s, size=%f]", name(), fontName, size);
    }
}
