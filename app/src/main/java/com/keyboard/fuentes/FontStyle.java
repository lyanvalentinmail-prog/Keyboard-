package com.keyboard.fuentes;

import java.text.Normalizer;

/**
 * Un IME no puede cambiar la tipografía de otra app, pero sí puede enviar
 * caracteres Unicode con apariencia de negrita, cursiva, etc.
 */
enum FontStyle {
    NORMAL("Normal", null, null, null),
    BOLD("Negrita", FontAlphabets.BOLD_UPPER, FontAlphabets.BOLD_LOWER, FontAlphabets.BOLD_DIGITS),
    ITALIC("Cursiva", FontAlphabets.ITALIC_UPPER, FontAlphabets.ITALIC_LOWER, null),
    BOLD_ITALIC("Negrita cursiva", FontAlphabets.BOLD_ITALIC_UPPER, FontAlphabets.BOLD_ITALIC_LOWER, null),
    SCRIPT("Manuscrita", FontAlphabets.SCRIPT_UPPER, FontAlphabets.SCRIPT_LOWER, null),
    MONOSPACE("Monoespacio", FontAlphabets.MONOSPACE_UPPER, FontAlphabets.MONOSPACE_LOWER,
            FontAlphabets.MONOSPACE_DIGITS),
    CIRCLED("Círculos", FontAlphabets.CIRCLED_UPPER, FontAlphabets.CIRCLED_LOWER,
            FontAlphabets.CIRCLED_DIGITS),
    FULLWIDTH("Ancho completo", FontAlphabets.FULLWIDTH_UPPER, FontAlphabets.FULLWIDTH_LOWER,
            FontAlphabets.FULLWIDTH_DIGITS);

    private final String label;
    private final String[] upper;
    private final String[] lower;
    private final String[] digits;

    FontStyle(String label, String[] upper, String[] lower, String[] digits) {
        this.label = label;
        this.upper = upper;
        this.lower = lower;
        this.digits = digits;
    }

    String getLabel() {
        return label;
    }

    String preview() {
        return transform("Aa 123");
    }

    String transform(CharSequence input) {
        if (input == null) {
            return "";
        }
        if (this == NORMAL) {
            return input.toString();
        }

        StringBuilder result = new StringBuilder(Math.max(16, input.length() * 2));
        int offset = 0;
        while (offset < input.length()) {
            int codePoint = Character.codePointAt(input, offset);
            offset += Character.charCount(codePoint);
            result.append(transformCodePoint(codePoint));
        }
        return result.toString();
    }

    private String transformCodePoint(int codePoint) {
        String direct = mapAscii(codePoint);
        if (direct != null) {
            return direct;
        }

        String original = new String(Character.toChars(codePoint));
        String decomposed = Normalizer.normalize(original, Normalizer.Form.NFD);

        // Letras acentuadas y ñ: se transforma la base y se conserva la marca combinante.
        if (decomposed.length() > Character.charCount(codePoint)) {
            int base = Character.codePointAt(decomposed, 0);
            String mappedBase = mapAscii(base);
            if (mappedBase != null) {
                StringBuilder result = new StringBuilder(mappedBase);
                int offset = Character.charCount(base);
                while (offset < decomposed.length()) {
                    int mark = Character.codePointAt(decomposed, offset);
                    result.appendCodePoint(mark);
                    offset += Character.charCount(mark);
                }
                return result.toString();
            }
        }
        return original;
    }

    private String mapAscii(int codePoint) {
        if (upper != null && codePoint >= 'A' && codePoint <= 'Z') {
            return upper[codePoint - 'A'];
        }
        if (lower != null && codePoint >= 'a' && codePoint <= 'z') {
            return lower[codePoint - 'a'];
        }
        if (digits != null && codePoint >= '0' && codePoint <= '9') {
            return digits[codePoint - '0'];
        }
        return null;
    }

    static FontStyle fromOrdinal(int ordinal) {
        FontStyle[] styles = values();
        if (ordinal < 0 || ordinal >= styles.length) {
            return NORMAL;
        }
        return styles[ordinal];
    }
}
