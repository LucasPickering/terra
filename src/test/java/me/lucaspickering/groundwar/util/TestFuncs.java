package me.lucaspickering.groundwar.util;

import org.junit.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestFuncs {

    @Test
    public void testRandomFromCollection() throws Exception {
        final int size = 10;
        final Collection<Integer> coll = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            coll.add(i);
        }

        final int randomInt = Funcs.randomFromCollection(new Random(), coll);
        assertTrue("Should be in range [0, 9]", 0 <= randomInt && randomInt < size);
    }

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
}
