package me.lucaspickering.groundwar.util;

import java.awt.Color;

public class Colors {

    public static class HSVColor {

        private final float hue;
        private final float saturation;
        private final float value;

        public HSVColor(float hue, float saturation, float value) {
            this.hue = hue;
            this.saturation = saturation;
            this.value = value;
        }

        public HSVColor(float[] hsv) {
            this(hsv[0], hsv[1], hsv[2]);
        }

        public float hue() {
            return hue;
        }

        public float saturation() {
            return saturation;
        }

        public float value() {
            return value;
        }
    }

    // General
    public static final Color CLEAR = Funcs.colorFromRgb(0x777777);
    public static final Color MENU_SHADER = Funcs.colorFromArgb(0xcc101010);

    // Tile colors
    public static final Color TILE_BG = Funcs.colorFromRgb(0x6aa84f);
    public static final Color TILE_OUTLINE = Funcs.colorFromRgb(0x434343);

    // Tile overlay colors
    public static final Color MOUSE_OVER = Funcs.colorFromArgb(0x60999999);
    public static final Color TILE_INFO_BG = Funcs.colorFromArgb(0xee444444);

    // Button colors
    public static final Color BUTTON_NORMAL = Color.BLACK;
    public static final Color BUTTON_HIGHLIGHT = Color.RED;
    public static final Color BUTTON_TEXT_NORMAL = Color.WHITE;
    public static final Color BUTTON_TEXT_HIGHLIGHT = Color.BLUE;
}
