package com.keyboard.fuentes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KeyboardBoundsTest {

    @Test
    public void portraitUsesFullWidthAndPreferredHeight() {
        assertEquals(1080, KeyboardBounds.width(1080, 3f));
        assertEquals(876, KeyboardBounds.height(1920, 3f));
    }

    @Test
    public void landscapeCapsWidthAndHeight() {
        assertEquals(1680, KeyboardBounds.width(1920, 3f));
        assertEquals(626, KeyboardBounds.height(1080, 3f));
    }

    @Test
    public void smallLandscapeNeverUsesTheWholeScreen() {
        int height = KeyboardBounds.height(480, 2f);
        assertTrue(height <= Math.round(480 * 0.58f));
        assertTrue(height > 0);
    }

    @Test
    public void toolbarAndGapsStayProportional() {
        int toolbar = KeyboardBounds.toolbarHeight(876, 3f);
        int gap = KeyboardBounds.keyGap(876, 3f);
        assertTrue(toolbar > 0 && toolbar < 876 / 2);
        assertTrue(gap > 0 && gap < 20);
    }

    @Test
    public void invalidDensityFallsBackToOne() {
        assertEquals(560, KeyboardBounds.width(800, 0f));
        assertEquals(292, KeyboardBounds.height(1000, 0f));
    }
}
