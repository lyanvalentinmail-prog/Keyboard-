package com.keyboard.fuentes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class TextEditingTest {
    private void deletes(String input, String expected) {
        int count = TextEditing.previousGraphemeLength(input);
        assertEquals(input, expected, input.substring(0, input.length() - count));
    }

    @Test public void emptyTextIsSafe() {
        assertEquals(0, TextEditing.previousGraphemeLength(null));
        assertEquals(0, TextEditing.previousGraphemeLength(""));
    }

    @Test public void asciiAndSpanishUseOneCharacter() {
        deletes("hola", "hol");
        deletes("ñ", "");
        deletes("e\u0301", "");
    }

    @Test public void sanitizeBadIcuBoundary() {
        assertEquals(2, TextEditing.safeLength("a😀", 1));
        assertEquals(0, TextEditing.safeLength("", 1));
        assertEquals(3, TextEditing.safeLength("abc", 9));
    }

    @Test public void surrogatePairsAreNeverSplit() {
        deletes("a😀", "a");
        deletes("𝐀", "");
    }

    @Test public void joinedFamilyDeletesAsOneEmoji() {
        deletes("x👨‍👩‍👧‍👦", "x");
    }

    @Test public void skinToneAndVariationSelectorStayWithBase() {
        deletes("x👍🏽", "x");
        deletes("x❤️", "x");
        deletes("x1️⃣", "x");
    }

    @Test public void flagsDeleteInPairs() {
        deletes("x🇪🇸", "x");
        deletes("🇪🇸🇺🇾", "🇪🇸");
        deletes("🇪🇸🇺🇾🇦", "🇪🇸🇺🇾");
    }

    @Test public void windowsNewlineIsOneCluster() {
        deletes("hola\r\n", "hola");
    }
}
