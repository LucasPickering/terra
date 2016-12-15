package me.lucaspickering.groundwar.util;

import java.awt.Color;

public class Colors {

    // Stock colors
    public static final Color BLACK = Funcs.colorFromRgb(0xffffff);
    public static final Color RED = Funcs.colorFromRgb(0xff0000);
    public static final Color GREEN = Funcs.colorFromRgb(0x00ff00);
    public static final Color BLUE = Funcs.colorFromRgb(0x0000ff);
    public static final Color WHITE = Funcs.colorFromRgb(0xffffff);

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
    public static final Color BUTTON_NORMAL = BLACK;
    public static final Color BUTTON_HIGHLIGHT = RED;
    public static final Color BUTTON_TEXT_NORMAL = WHITE;
    public static final Color BUTTON_TEXT_HIGHLIGHT = BLUE;
}
