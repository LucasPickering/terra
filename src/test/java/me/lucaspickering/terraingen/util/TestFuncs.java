package me.lucaspickering.terraingen.util;

import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

public class TestFuncs {

    @Test
    public void testColorFromArgb() throws Exception {
        final int black = 0xff000000;
        final int white = 0xffffffff;
        final int red = 0xffff0000;
        final int green = 0xff00ff00;
        final int blue = 0xff0000ff;
        final int clear = 0x00ffffff;

        Color color;

        color = Funcs.colorFromArgb(black);
        assertEquals("Incorrect alpha value", 255, color.getAlpha());
        assertEquals("Incorrect red value", 0, color.getRed());
        assertEquals("Incorrect green value", 0, color.getGreen());
        assertEquals("Incorrect blue value", 0, color.getBlue());

        color = Funcs.colorFromArgb(white);
        assertEquals("Incorrect alpha value", 255, color.getAlpha());
        assertEquals("Incorrect red value", 255, color.getRed());
        assertEquals("Incorrect green value", 255, color.getGreen());
        assertEquals("Incorrect blue value", 255, color.getBlue());

        color = Funcs.colorFromArgb(red);
        assertEquals("Incorrect alpha value", 255, color.getAlpha());
        assertEquals("Incorrect red value", 255, color.getRed());
        assertEquals("Incorrect green value", 0, color.getGreen());
        assertEquals("Incorrect blue value", 0, color.getBlue());

        color = Funcs.colorFromArgb(green);
        assertEquals("Incorrect alpha value", 255, color.getAlpha());
        assertEquals("Incorrect red value", 0, color.getRed());
        assertEquals("Incorrect green value", 255, color.getGreen());
        assertEquals("Incorrect blue value", 0, color.getBlue());

        color = Funcs.colorFromArgb(blue);
        assertEquals("Incorrect alpha value", 255, color.getAlpha());
        assertEquals("Incorrect red value", 0, color.getRed());
        assertEquals("Incorrect green value", 0, color.getGreen());
        assertEquals("Incorrect blue value", 255, color.getBlue());

        color = Funcs.colorFromArgb(clear);
        assertEquals("Incorrect alpha value", 0, color.getAlpha());
        assertEquals("Incorrect red value", 255, color.getRed());
        assertEquals("Incorrect green value", 255, color.getGreen());
        assertEquals("Incorrect blue value", 255, color.getBlue());
    }

    @Test
    public void testToHSV() throws Exception {
        float[] hsv;

        hsv = Funcs.toHsv(Color.WHITE);
        assertEquals("Hue is incorrect", 0f, hsv[0], 0f);
        assertEquals("Saturation is incorrect", 0f, hsv[1], 0f);
        assertEquals("Value is incorrect", 1f, hsv[2], 0f);

        hsv = Funcs.toHsv(Color.BLACK);
        assertEquals("Hue is incorrect", 0f, hsv[0], 0f);
        assertEquals("Saturation is incorrect", 0f, hsv[1], 0f);
        assertEquals("Value is incorrect", 0f, hsv[2], 0f);

        hsv = Funcs.toHsv(Color.RED);
        assertEquals("Hue is incorrect", 0f, hsv[0], 0f);
        assertEquals("Saturation is incorrect", 1f, hsv[1], 0f);
        assertEquals("Value is incorrect", 1f, hsv[2], 0f);

        hsv = Funcs.toHsv(Color.GREEN);
        assertEquals("Hue is incorrect", 1f / 3f, hsv[0], 0.00001f);
        assertEquals("Saturation is incorrect", 1f, hsv[1], 0f);
        assertEquals("Value is incorrect", 1f, hsv[2], 0f);

        hsv = Funcs.toHsv(Color.BLUE);
        assertEquals("Hue is incorrect", 2f / 3f, hsv[0], 0.00001f);
        assertEquals("Saturation is incorrect", 1f, hsv[1], 0f);
        assertEquals("Value is incorrect", 1f, hsv[2], 0f);

        hsv = Funcs.toHsv(new Color(150, 40, 30));
        assertEquals("Hue is incorrect", 0.0139f, hsv[0], 0.0001f);
        assertEquals("Saturation is incorrect", 0.8f, hsv[1], 0f);
        assertEquals("Value is incorrect", 0.5882f, hsv[2], 0.0001f);
    }

    @Test
    public void testToRGB() throws Exception {
        Color rgb;

        // White
        rgb = Funcs.toRgb(0f, 0f, 1f);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 255, rgb.getRed());
        assertEquals("Green is incorrect", 255, rgb.getGreen());
        assertEquals("Blue is incorrect", 255, rgb.getBlue());

        // Black
        rgb = Funcs.toRgb(0f, 0f, 0f);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 0, rgb.getRed());
        assertEquals("Green is incorrect", 0, rgb.getGreen());
        assertEquals("Blue is incorrect", 0, rgb.getBlue());

        // Red
        rgb = Funcs.toRgb(0f, 1f, 1f);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 255, rgb.getRed());
        assertEquals("Green is incorrect", 0, rgb.getGreen());
        assertEquals("Blue is incorrect", 0, rgb.getBlue());

        // Green
        rgb = Funcs.toRgb(1f / 3f, 1f, 1f);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 0, rgb.getRed());
        assertEquals("Green is incorrect", 255, rgb.getGreen());
        assertEquals("Blue is incorrect", 0, rgb.getBlue());

        // Blue
        rgb = Funcs.toRgb(2f / 3f, 1f, 1f);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 0, rgb.getRed());
        assertEquals("Green is incorrect", 0, rgb.getGreen());
        assertEquals("Blue is incorrect", 255, rgb.getBlue());

        // Yellowish/gold
        rgb = Funcs.toRgb(0.1f, 0.5f, 0.75f);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 191, rgb.getRed());
        assertEquals("Green is incorrect", 153, rgb.getGreen());
        assertEquals("Blue is incorrect", 96, rgb.getBlue());
    }
}
