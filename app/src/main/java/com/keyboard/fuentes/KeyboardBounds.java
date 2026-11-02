package com.keyboard.fuentes;

/** Cálculos puros para mantener el teclado compacto y sin estirar. */
final class KeyboardBounds {
    private static final float MAX_WIDTH_DP = 560f;
    private static final float PREFERRED_HEIGHT_DP = 292f;
    private static final float MAX_HEIGHT_FRACTION = 0.58f;
    private static final float TOOLBAR_MIN_DP = 34f;
    private static final float TOOLBAR_MAX_DP = 48f;
    private static final float TOOLBAR_FRACTION = 0.15f;
    private static final float KEY_GAP_MIN_DP = 2f;
    private static final float KEY_GAP_MAX_DP = 4f;
    private static final float KEY_GAP_FRACTION = 0.012f;

    private KeyboardBounds() {
    }

    static int width(int availableWidthPx, float density) {
        float safeDensity = density > 0f ? density : 1f;
        int maxWidth = Math.round(MAX_WIDTH_DP * safeDensity);
        return Math.max(1, Math.min(availableWidthPx, maxWidth));
    }

    static int height(int screenHeightPx, float density) {
        float safeDensity = density > 0f ? density : 1f;
        int preferred = Math.round(PREFERRED_HEIGHT_DP * safeDensity);
        int max = Math.round(screenHeightPx * MAX_HEIGHT_FRACTION);
        return Math.max(1, Math.min(preferred, max));
    }

    static int toolbarHeight(int keyboardHeightPx, float density) {
        float safeDensity = density > 0f ? density : 1f;
        int min = Math.round(TOOLBAR_MIN_DP * safeDensity);
        int max = Math.round(TOOLBAR_MAX_DP * safeDensity);
        int proportional = Math.round(keyboardHeightPx * TOOLBAR_FRACTION);
        return Math.max(1, Math.max(min, Math.min(max, proportional)));
    }

    static int keyGap(int keyboardHeightPx, float density) {
        float safeDensity = density > 0f ? density : 1f;
        int min = Math.round(KEY_GAP_MIN_DP * safeDensity);
        int max = Math.round(KEY_GAP_MAX_DP * safeDensity);
        int proportional = Math.round(keyboardHeightPx * KEY_GAP_FRACTION);
        return Math.max(1, Math.max(min, Math.min(max, proportional)));
    }
}
