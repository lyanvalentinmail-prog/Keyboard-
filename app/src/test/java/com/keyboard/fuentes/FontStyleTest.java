package com.keyboard.fuentes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class FontStyleTest {

    @Test
    public void normalKeepsTheOriginalText() {
        assertEquals("Hola ñ 123", FontStyle.NORMAL.transform("Hola ñ 123"));
    }

    @Test
    public void everyStyleHasAPreview() {
        for (FontStyle style : FontStyle.values()) {
            String preview = style.preview();
            assertNotNull(preview);
            assertFalse(preview.isEmpty());
        }
    }

    @Test
    public void boldMapsLettersAndDigitsToSingleCodePoints() {
        String transformed = FontStyle.BOLD.transform("Aa09");
        assertNotEquals("Aa09", transformed);
        assertEquals(4, transformed.codePointCount(0, transformed.length()));
    }

    @Test
    public void italicUsesTheSpecialSmallH() {
        String transformed = FontStyle.ITALIC.transform("h");
        assertEquals("\u210E", transformed);
    }

    @Test
    public void accentedSpanishLettersKeepTheirCombiningMark() {
        String transformed = FontStyle.BOLD.transform("ñ");
        assertNotEquals("ñ", transformed);
        assertTrue(transformed.contains("\u0303"));
    }

    @Test
    public void punctuationAndSpacesArePreserved() {
        String input = "Hola, mundo! Que tal?";
        String transformed = FontStyle.MONOSPACE.transform(input);
        assertTrue(transformed.contains(", "));
        assertTrue(transformed.endsWith("?"));
        assertEquals(input.codePointCount(0, input.length()),
                transformed.codePointCount(0, transformed.length()));
    }

    @Test
    public void invalidOrdinalFallsBackToNormal() {
        assertEquals(FontStyle.NORMAL, FontStyle.fromOrdinal(-1));
        assertEquals(FontStyle.NORMAL, FontStyle.fromOrdinal(999));
    }
}
