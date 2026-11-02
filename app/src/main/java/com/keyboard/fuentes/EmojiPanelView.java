package com.keyboard.fuentes;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Emoji grid with evenly sized cells, a working delete key and a keyboard shortcut. */
class EmojiPanelView extends LinearLayout {
    interface Listener {
        void onEmoji(String emoji);
        void onDelete();
        void onClose();
    }

    private static final String[] EMOJIS = {
            "😀", "😂", "😍", "😎", "😊", "😉", "🤔", "😢",
            "👍", "👎", "👏", "🙏", "💪", "🤝", "✌️", "👋",
            "❤️", "🔥", "✨", "🎉", "✅", "⭐", "💡", "🌟",
            "😺", "🐶", "🌸", "🍕", "☕", "🚀", "🌙", "⚡",
            "🥳", "🤩", "🥰", "😇", "🙌", "🫶", "💙", "💚",
            "🎂", "🎁", "⚽", "🎵", "🌈", "☀️", "🍀", "🦋"
    };

    EmojiPanelView(Context context, Listener listener) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(PanelUi.color(context, R.color.panel));
        int padding = PanelUi.dp(context, 12);
        setPadding(padding, PanelUi.dp(context, 10), padding, PanelUi.dp(context, 12));
        addView(PanelUi.header(context, "Emojis", "Cerrar emojis", listener::onClose));
        addView(PanelUi.hint(context, "Toca un emoji para insertarlo. El panel permanece abierto."));
        int columns = Math.max(4, Math.min(8,
                (getResources().getDisplayMetrics().widthPixels - padding * 2) / PanelUi.dp(context, 46)));
        for (int start = 0; start < EMOJIS.length; start += columns) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(HORIZONTAL);
            for (int column = 0; column < columns; column++) {
                int index = start + column;
                View cell;
                if (index < EMOJIS.length) {
                    String emoji = EMOJIS[index];
                    TextView button = new TextView(context);
                    button.setText(emoji);
                    button.setTextSize(22);
                    button.setGravity(Gravity.CENTER);
                    button.setBackground(PanelUi.background(context, PanelUi.color(context, R.color.key), false));
                    button.setFocusable(true);
                    button.setContentDescription("Insertar emoji " + emoji);
                    button.setOnClickListener(view -> listener.onEmoji(emoji));
                    cell = button;
                } else {
                    cell = new View(context);
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                        LayoutParams.MATCH_PARENT, 1f);
                params.setMargins(PanelUi.dp(context, 2), 0, PanelUi.dp(context, 2), 0);
                row.addView(cell, params);
            }
            addView(row, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, PanelUi.dp(context, 48)));
        }
        LinearLayout footer = new LinearLayout(context);
        footer.setOrientation(HORIZONTAL);
        TextView keyboard = PanelUi.actionButton(context, "Volver al teclado", R.drawable.ic_keyboard,
                listener::onClose);
        footer.addView(keyboard, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        footer.addView(PanelUi.iconButton(context, R.drawable.ic_backspace, "Borrar último carácter",
                listener::onDelete), new LinearLayout.LayoutParams(PanelUi.dp(context, 52), PanelUi.dp(context, 48)));
        LinearLayout.LayoutParams params = PanelUi.actionParams(context);
        params.setMargins(0, PanelUi.dp(context, 8), 0, 0);
        addView(footer, params);
    }
}
