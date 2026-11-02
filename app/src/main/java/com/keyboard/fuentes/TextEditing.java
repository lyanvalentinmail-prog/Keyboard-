package com.keyboard.fuentes;

/** Grapheme-aware fallback for Android 6; newer versions use the platform ICU. */
final class TextEditing {
    private TextEditing() {
    }

    static int previousGraphemeLength(CharSequence text) {
        if (text == null || text.length() == 0) {
            return 0;
        }
        int end = text.length();
        if (end >= 2 && text.charAt(end - 2) == '\r' && text.charAt(end - 1) == '\n') {
            return 2;
        }
        int start = end - Character.charCount(Character.codePointBefore(text, end));
        // A variation selector, accent or skin tone belongs to the preceding base.
        if (isExtending(Character.codePointAt(text, start))) {
            while (start > 0 && isExtending(Character.codePointAt(text, start))) {
                start -= Character.charCount(Character.codePointBefore(text, start));
            }
        }
        // Walk complete segments backwards across emoji joiners.
        while (start > 0 && Character.codePointBefore(text, start) == 0x200d) {
            start--; // ZWJ is always one UTF-16 unit.
            if (start > 0) {
                start -= Character.charCount(Character.codePointBefore(text, start));
                while (start > 0 && isExtending(Character.codePointAt(text, start))) {
                    start -= Character.charCount(Character.codePointBefore(text, start));
                }
            }
        }
        if (isRegionalIndicator(Character.codePointAt(text, start))) {
            int runStart = start;
            int count = 1;
            while (runStart > 0 && isRegionalIndicator(Character.codePointBefore(text, runStart))) {
                runStart -= 2;
                count++;
            }
            // Flags are pairs; an unpaired final indicator must not eat its neighbor.
            if (count % 2 == 0) {
                start -= 2;
            }
        }
        return end - start;
    }

    /** Never let an editor or ICU implementation split a surrogate pair. */
    static int safeLength(CharSequence text, int requested) {
        if (text == null || text.length() == 0 || requested <= 0) {
            return 0;
        }
        int count = Math.min(requested, text.length());
        int boundary = text.length() - count;
        if (boundary > 0 && Character.isLowSurrogate(text.charAt(boundary))
                && Character.isHighSurrogate(text.charAt(boundary - 1))) {
            count++;
        }
        return count;
    }

    private static boolean isExtending(int cp) {
        int type = Character.getType(cp);
        return type == Character.NON_SPACING_MARK || type == Character.COMBINING_SPACING_MARK
                || type == Character.ENCLOSING_MARK
                || (cp >= 0xfe00 && cp <= 0xfe0f)
                || (cp >= 0x1f3fb && cp <= 0x1f3ff)
                || (cp >= 0xe0020 && cp <= 0xe007f);
    }

    private static boolean isRegionalIndicator(int cp) {
        return cp >= 0x1f1e6 && cp <= 0x1f1ff;
    }
}
