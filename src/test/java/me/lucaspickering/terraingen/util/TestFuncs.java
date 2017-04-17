package me.lucaspickering.terraingen.util;

import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;

public class TestFuncs {

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
