package com.keyboard.fuentes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Panel que sustituye a las teclas cuando se pulsa el botón Aa. */
class FontPickerView extends LinearLayout {
    interface Listener {
        void onStyleSelected(FontStyle style);

        void onClose();
    }

    private static final int COLOR_PANEL = Color.parseColor("#141414");
    private static final int COLOR_BUTTON = Color.parseColor("#303030");
    private static final int COLOR_SELECTED = Color.parseColor("#263448");
    private static final int COLOR_TEXT = Color.parseColor("#F5F5F5");
    private static final int COLOR_HINT = Color.parseColor("#A0A0A0");
    private static final int COLOR_ACCENT = Color.parseColor("#4C8DFF");

    private final Listener listener;
    private FontStyle current = FontStyle.NORMAL;

    FontPickerView(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setOrientation(VERTICAL);
        setBackgroundColor(COLOR_PANEL);
        setPadding(dp(12), dp(10), dp(12), dp(10));
        build();
    }

    void setCurrentStyle(FontStyle style) {
        current = style == null ? FontStyle.NORMAL : style;
        build();
    }

    private void build() {
        removeAllViews();

        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(getContext());
        title.setText("Elige la fuente");
        title.setTextColor(COLOR_TEXT);
        title.setTextSize(17);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        TextView close = new TextView(getContext());
        close.setText("✕");
        close.setTextColor(COLOR_TEXT);
        close.setTextSize(20);
        close.setGravity(Gravity.CENTER);
        close.setBackground(roundBackground(COLOR_BUTTON, false));
        close.setClickable(true);
        close.setFocusable(true);
        close.setContentDescription("Cerrar selector de fuentes");
        close.setOnClickListener(view -> listener.onClose());

        header.addView(title, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        header.addView(close, new LinearLayout.LayoutParams(dp(40), dp(40)));
        addView(header, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView hint = new TextView(getContext());
        hint.setText("Se insertan caracteres Unicode compatibles con cualquier app que los muestre.");
        hint.setTextColor(COLOR_HINT);
        hint.setTextSize(12);
        hint.setPadding(0, dp(8), 0, dp(8));
        addView(hint, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout grid = new LinearLayout(getContext());
        grid.setOrientation(VERTICAL);
        addView(grid, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        FontStyle[] styles = FontStyle.values();
        for (int index = 0; index < styles.length; index += 2) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(HORIZONTAL);
            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
            rowParams.setMargins(0, dp(3), 0, dp(3));
            grid.addView(row, rowParams);

            row.addView(styleButton(styles[index]), buttonParams(dp(3)));
            if (index + 1 < styles.length) {
                row.addView(styleButton(styles[index + 1]), buttonParams(dp(3)));
            } else {
                row.addView(new TextView(getContext()), buttonParams(dp(3)));
            }
        }
    }

    private TextView styleButton(FontStyle style) {
        TextView button = new TextView(getContext());
        boolean selected = style == current;
        button.setText(style.getLabel() + "\n" + style.preview());
        button.setTextColor(COLOR_TEXT);
        button.setTextSize(14);
        button.setGravity(Gravity.CENTER);
        button.setLineSpacing(dp(2), 1f);
        button.setPadding(dp(8), dp(6), dp(8), dp(6));
        button.setBackground(roundBackground(selected ? COLOR_SELECTED : COLOR_BUTTON, selected));
        button.setClickable(true);
        button.setFocusable(true);
        button.setContentDescription("Usar fuente " + style.getLabel());
        button.setOnClickListener(view -> listener.onStyleSelected(style));
        return button;
    }

    private LinearLayout.LayoutParams buttonParams(int margin) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
        params.setMargins(margin, 0, margin, 0);
        return params;
    }

    private GradientDrawable roundBackground(int color, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(10));
        drawable.setColor(color);
        if (selected) {
            drawable.setStroke((int) dp(2), COLOR_ACCENT);
        }
        return drawable;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
