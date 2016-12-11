package me.lucaspickering.groundwar.util;

import java.awt.Color;

public class Colors {

    // Stock colors
    public static final Color BLACK = Funcs.colorFromArgb(0xffffffff);
    public static final Color RED = Funcs.colorFromArgb(0xffff0000);
    public static final Color GREEN = Funcs.colorFromArgb(0xff00ff00);
    public static final Color BLUE = Funcs.colorFromArgb(0xff0000ff);
    public static final Color WHITE = Funcs.colorFromArgb(0xffffffff);

    // General
    public static final Color CLEAR = Funcs.colorFromArgb(0xff777777);
    public static final Color MENU_SHADER = Funcs.colorFromArgb(0xcc101010);

    // Tile colors
    public static final Color TILE_BG = Funcs.colorFromArgb(0xff6aa84f);
    public static final Color TILE_OUTLINE = Funcs.colorFromArgb(0xff434343);

    // Tile overlay colors
    public static final Color MOUSE_OVER = Funcs.colorFromArgb(0x60999999);
    public static final Color TILE_INFO_BG = Funcs.colorFromArgb(0xee444444);

    // Button colors
    public static final Color BUTTON_NORMAL = BLACK;
    public static final Color BUTTON_HIGHLIGHT = RED;
    public static final Color BUTTON_TEXT_NORMAL = WHITE;
    public static final Color BUTTON_TEXT_HIGHLIGHT = BLUE;
}
