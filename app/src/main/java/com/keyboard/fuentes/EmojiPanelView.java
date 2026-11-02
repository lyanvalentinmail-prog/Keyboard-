package com.keyboard.fuentes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Panel sencillo de emojis frecuentes, accesible desde la barra superior. */
class EmojiPanelView extends LinearLayout {
    interface Listener {
        void onEmoji(String emoji);

        void onClose();
    }

    private static final String[] EMOJIS = {
            "😀", "😂", "😍", "😎", "😊", "😉", "🤔", "😢",
            "👍", "👎", "👏", "🙏", "💪", "🤝", "✌️", "👋",
            "❤️", "🔥", "✨", "🎉", "✅", "⭐", "💡", "🌟",
            "😺", "🐶", "🌸", "🍕", "☕", "🚀", "🌙", "⚡"
    };

    private static final int COLOR_PANEL = Color.parseColor("#141414");
    private static final int COLOR_BUTTON = Color.parseColor("#303030");
    private static final int COLOR_TEXT = Color.parseColor("#F5F5F5");
    private static final int COLOR_HINT = Color.parseColor("#A0A0A0");

    private final Listener listener;

    EmojiPanelView(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setOrientation(VERTICAL);
        setBackgroundColor(COLOR_PANEL);
        setPadding(dp(12), dp(10), dp(12), dp(10));
        build();
    }

    private void build() {
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(getContext());
        title.setText("Emojis");
        title.setTextColor(COLOR_TEXT);
        title.setTextSize(17);
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        TextView close = new TextView(getContext());
        close.setText("✕");
        close.setTextColor(COLOR_TEXT);
        close.setTextSize(20);
        close.setGravity(Gravity.CENTER);
        close.setBackground(roundBackground());
        close.setClickable(true);
        close.setFocusable(true);
        close.setContentDescription("Cerrar emojis");
        close.setOnClickListener(view -> listener.onClose());

        header.addView(title, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        header.addView(close, new LinearLayout.LayoutParams(dp(40), dp(40)));
        addView(header, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView hint = new TextView(getContext());
        hint.setText("Toca un emoji para insertarlo; el panel permanece abierto.");
        hint.setTextColor(COLOR_HINT);
        hint.setTextSize(12);
        hint.setPadding(0, dp(6), 0, dp(6));
        addView(hint, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout grid = new LinearLayout(getContext());
        grid.setOrientation(VERTICAL);
        addView(grid, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        int columns = 8;
        for (int start = 0; start < EMOJIS.length; start += columns) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(HORIZONTAL);
            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(44));
            rowParams.setMargins(0, dp(2), 0, dp(2));
            grid.addView(row, rowParams);

            for (int column = 0; column < columns; column++) {
                int index = start + column;
                if (index >= EMOJIS.length) {
                    break;
                }
                String emoji = EMOJIS[index];
                TextView button = new TextView(getContext());
                button.setText(emoji);
                button.setTextSize(20);
                button.setGravity(Gravity.CENTER);
                button.setBackground(roundBackground());
                button.setClickable(true);
                button.setFocusable(true);
                button.setContentDescription("Insertar emoji " + emoji);
                button.setOnClickListener(view -> listener.onEmoji(emoji));

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
                params.setMargins(dp(2), 0, dp(2), 0);
                row.addView(button, params);
            }
        }
    }

    private GradientDrawable roundBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(9));
        drawable.setColor(COLOR_BUTTON);
        return drawable;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
