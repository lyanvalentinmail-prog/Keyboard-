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
        while (start > 0) {
            int previous = Character.codePointBefore(text, start);
            if (isExtending(previous)) {
                start -= Character.charCount(previous);
            } else if (previous == 0x200d && start >= 2) {
                // Joiner and the complete preceding code point belong to this emoji.
                start -= Character.charCount(previous);
                start -= Character.charCount(Character.codePointBefore(text, start));
            } else {
                break;
            }
        }
        int codePoint = Character.codePointAt(text, start);
        if (isRegionalIndicator(codePoint) && start > 0) {
            int previous = Character.codePointBefore(text, start);
            if (isRegionalIndicator(previous)) {
                // Count the run so deleting the last of three flags does not eat its neighbor.
                int count = 1;
                int offset = start;
                while (offset > 0) {
                    int cp = Character.codePointBefore(text, offset);
                    if (!isRegionalIndicator(cp)) {
                        break;
                    }
                    count++;
                    offset -= Character.charCount(cp);
                }
                if (count % 2 == 0) {
                    start -= Character.charCount(previous);
                }
            }
        }
        return end - start;
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
