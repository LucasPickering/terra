package me.lucaspickering.terraingen.util;

import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;

public class TestFuncs {

    @Test
    public void testToHsb() throws Exception {
        float[] hsb;

        hsb = Funcs.toHsb(Color.WHITE);
        assertEquals("Hue is incorrect", 0f, hsb[0], 0f);
        assertEquals("Saturation is incorrect", 0f, hsb[1], 0f);
        assertEquals("Value is incorrect", 1f, hsb[2], 0f);

        hsb = Funcs.toHsb(Color.BLACK);
        assertEquals("Hue is incorrect", 0f, hsb[0], 0f);
        assertEquals("Saturation is incorrect", 0f, hsb[1], 0f);
        assertEquals("Value is incorrect", 0f, hsb[2], 0f);

        hsb = Funcs.toHsb(Color.RED);
        assertEquals("Hue is incorrect", 0f, hsb[0], 0f);
        assertEquals("Saturation is incorrect", 1f, hsb[1], 0f);
        assertEquals("Value is incorrect", 1f, hsb[2], 0f);

        hsb = Funcs.toHsb(Color.GREEN);
        assertEquals("Hue is incorrect", 1f / 3f, hsb[0], 0.00001f);
        assertEquals("Saturation is incorrect", 1f, hsb[1], 0f);
        assertEquals("Value is incorrect", 1f, hsb[2], 0f);

        hsb = Funcs.toHsb(Color.BLUE);
        assertEquals("Hue is incorrect", 2f / 3f, hsb[0], 0.00001f);
        assertEquals("Saturation is incorrect", 1f, hsb[1], 0f);
        assertEquals("Value is incorrect", 1f, hsb[2], 0f);

        hsb = Funcs.toHsb(new Color(150, 40, 30));
        assertEquals("Hue is incorrect", 0.0139f, hsb[0], 0.0001f);
        assertEquals("Saturation is incorrect", 0.8f, hsb[1], 0f);
        assertEquals("Value is incorrect", 0.5882f, hsb[2], 0.0001f);
    }

    @Test
    public void testToRgb() throws Exception {
        Color rgb;
        final float[] hsb = new float[3];

        // White
        hsb[0] = 0f;
        hsb[1] = 0f;
        hsb[2] = 1f;
        rgb = Funcs.toRgb(hsb);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 255, rgb.getRed());
        assertEquals("Green is incorrect", 255, rgb.getGreen());
        assertEquals("Blue is incorrect", 255, rgb.getBlue());

        // Black
        hsb[0] = 0f;
        hsb[1] = 0f;
        hsb[2] = 0f;
        rgb = Funcs.toRgb(hsb);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 0, rgb.getRed());
        assertEquals("Green is incorrect", 0, rgb.getGreen());
        assertEquals("Blue is incorrect", 0, rgb.getBlue());

        // Red
        hsb[0] = 0f;
        hsb[1] = 1f;
        hsb[2] = 1f;
        rgb = Funcs.toRgb(hsb);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 255, rgb.getRed());
        assertEquals("Green is incorrect", 0, rgb.getGreen());
        assertEquals("Blue is incorrect", 0, rgb.getBlue());

        // Green
        hsb[0] = 1f / 3f;
        hsb[1] = 1f;
        hsb[2] = 1f;
        rgb = Funcs.toRgb(hsb);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 0, rgb.getRed());
        assertEquals("Green is incorrect", 255, rgb.getGreen());
        assertEquals("Blue is incorrect", 0, rgb.getBlue());

        // Blue
        hsb[0] = 2f / 3f;
        hsb[1] = 1f;
        hsb[2] = 1f;
        rgb = Funcs.toRgb(hsb);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 0, rgb.getRed());
        assertEquals("Green is incorrect", 0, rgb.getGreen());
        assertEquals("Blue is incorrect", 255, rgb.getBlue());

        // Yellowish/gold
        hsb[0] = 0.1f;
        hsb[1] = 0.5f;
        hsb[2] = 0.75f;
        rgb = Funcs.toRgb(hsb);
        assertEquals("Alpha is incorrect", 255, rgb.getAlpha());
        assertEquals("Red is incorrect", 191, rgb.getRed());
        assertEquals("Green is incorrect", 153, rgb.getGreen());
        assertEquals("Blue is incorrect", 96, rgb.getBlue());
    }

    @Test
    public void testBlendColors() throws Exception {
        Color blended;

        blended = Funcs.blendColors(Color.RED, Color.BLUE);
        assertEquals("Blended color is incorrect", 0xffff00ff, blended.getRGB());
        blended = Funcs.blendColors(Color.RED, Color.GREEN);
        assertEquals("Blended color is incorrect", 0xffffff00, blended.getRGB());
        blended = Funcs.blendColors(Color.GREEN, Color.BLUE);
        assertEquals("Blended color is incorrect", 0xff00ffff, blended.getRGB());

        blended = Funcs.blendColors(Color.RED, Color.WHITE);
        assertEquals("Blended color is incorrect", 0xffffffff, blended.getRGB());
        blended = Funcs.blendColors(Color.GREEN, Color.WHITE);
        assertEquals("Blended color is incorrect", 0xffffffff, blended.getRGB());
        blended = Funcs.blendColors(Color.GREEN, Color.WHITE);
        assertEquals("Blended color is incorrect", 0xffffffff, blended.getRGB());
    }

    @Test
    public void testOverlayColors() throws Exception {
        Color overlayed;

        overlayed = Funcs.overlayColors(Color.RED, Color.BLUE);
        assertEquals("Blended color is incorrect", 0xffff0000, overlayed.getRGB());
        overlayed = Funcs.overlayColors(Color.RED, Color.GREEN);
        assertEquals("Blended color is incorrect", 0xffff0000, overlayed.getRGB());
        overlayed = Funcs.overlayColors(Color.GREEN, Color.RED);
        assertEquals("Blended color is incorrect", 0xff00ff00, overlayed.getRGB());
        overlayed = Funcs.overlayColors(Color.GREEN, Color.BLUE);
        assertEquals("Blended color is incorrect", 0xff00ff00, overlayed.getRGB());
        overlayed = Funcs.overlayColors(Color.BLUE, Color.RED);
        assertEquals("Blended color is incorrect", 0xff0000ff, overlayed.getRGB());
        overlayed = Funcs.overlayColors(Color.BLUE, Color.GREEN);
        assertEquals("Blended color is incorrect", 0xff0000ff, overlayed.getRGB());

        overlayed = Funcs.overlayColors(Color.RED, Color.WHITE);
        assertEquals("Blended color is incorrect", 0xffff0000, overlayed.getRGB());
        overlayed = Funcs.overlayColors(Color.GREEN, Color.WHITE);
        assertEquals("Blended color is incorrect", 0xff00ff00, overlayed.getRGB());
        overlayed = Funcs.overlayColors(Color.BLUE, Color.WHITE);
        assertEquals("Blended color is incorrect", 0xff0000ff, overlayed.getRGB());
    }
}
