package com.keyboard.fuentes;

/** Alfabetos Unicode usados para simular distintas fuentes dentro de un campo de texto. */
final class FontAlphabets {
    private FontAlphabets() {
    }

    static final String[] BOLD_UPPER = range(0x1D400, 26);
    static final String[] BOLD_LOWER = range(0x1D41A, 26);
    static final String[] BOLD_DIGITS = range(0x1D7CE, 10);

    static final String[] ITALIC_UPPER = range(0x1D434, 26);
    static final String[] ITALIC_LOWER = italicLower();

    static final String[] BOLD_ITALIC_UPPER = range(0x1D468, 26);
    static final String[] BOLD_ITALIC_LOWER = range(0x1D482, 26);

    static final String[] SCRIPT_UPPER = range(0x1D4D0, 26);
    static final String[] SCRIPT_LOWER = range(0x1D4EA, 26);

    static final String[] MONOSPACE_UPPER = range(0x1D670, 26);
    static final String[] MONOSPACE_LOWER = range(0x1D68A, 26);
    static final String[] MONOSPACE_DIGITS = range(0x1D7F6, 10);

    static final String[] CIRCLED_UPPER = range(0x24B6, 26);
    static final String[] CIRCLED_LOWER = range(0x24D0, 26);
    static final String[] CIRCLED_DIGITS = circledDigits();

    static final String[] FULLWIDTH_UPPER = range(0xFF21, 26);
    static final String[] FULLWIDTH_LOWER = range(0xFF41, 26);
    static final String[] FULLWIDTH_DIGITS = range(0xFF10, 10);

    private static String[] italicLower() {
        String[] letters = range(0x1D44E, 26);
        // La h cursiva matemática está en U+210E; U+1D455 no está asignado.
        letters[7] = codePoint(0x210E);
        return letters;
    }

    private static String[] circledDigits() {
        return fromCodePoints(new int[]{
                0x24FF,
                0x2460, 0x2461, 0x2462, 0x2463, 0x2464,
                0x2465, 0x2466, 0x2467, 0x2468
        });
    }

    private static String[] range(int start, int count) {
        String[] values = new String[count];
        for (int i = 0; i < count; i++) {
            values[i] = codePoint(start + i);
        }
        return values;
    }

    private static String[] fromCodePoints(int[] codePoints) {
        String[] values = new String[codePoints.length];
        for (int i = 0; i < codePoints.length; i++) {
            values[i] = codePoint(codePoints[i]);
        }
        return values;
    }

    private static String codePoint(int value) {
        return new String(Character.toChars(value));
    }
}
