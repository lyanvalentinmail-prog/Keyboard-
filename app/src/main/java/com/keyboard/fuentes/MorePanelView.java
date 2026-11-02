package com.keyboard.fuentes;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Functional shortcuts, with consistent vector icons and room for larger text. */
class MorePanelView extends LinearLayout {
    interface Listener {
        void onOpenFonts();
        void onOpenEmojis();
        void onOpenClipboard();
        void onToggleSymbols();
        void onOpenSettings();
        void onHideKeyboard();
        void onClose();
    }

    private final Listener listener;
    private final LinearLayout content;
    private FontStyle style = FontStyle.NORMAL;
    private boolean symbols;

    MorePanelView(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setOrientation(VERTICAL);
        setBackgroundColor(PanelUi.color(context, R.color.panel));
        int padding = PanelUi.dp(context, 14);
        setPadding(padding, PanelUi.dp(context, 10), padding, PanelUi.dp(context, 12));
        addView(PanelUi.header(context, "Más opciones", "Cerrar más opciones", listener::onClose));
        content = new LinearLayout(context);
        content.setOrientation(VERTICAL);
        addView(content, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        refresh(style, symbols);
    }

    void refresh(FontStyle style, boolean symbols) {
        this.style = style == null ? FontStyle.NORMAL : style;
        this.symbols = symbols;
        content.removeAllViews();
        TextView status = new TextView(getContext());
        status.setText("Fuente actual: " + this.style.getLabel() + "\n" + this.style.preview());
        status.setTextColor(PanelUi.color(getContext(), R.color.text_primary));
        status.setTextSize(15);
        status.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, PanelUi.dp(getContext(), 12), 0, PanelUi.dp(getContext(), 12));
        status.setBackground(PanelUi.background(getContext(), PanelUi.color(getContext(), R.color.surface), false));
        content.addView(status, PanelUi.actionParams(getContext()));
        content.addView(PanelUi.hint(getContext(), "Mantén pulsadas las teclas para los símbolos pequeños. "
                + "Mantén pulsado Mayúsculas para bloquearlas."));
        action("Elegir fuente", R.drawable.ic_font, listener::onOpenFonts);
        action("Emojis", R.drawable.ic_emoji, listener::onOpenEmojis);
        action("Portapapeles", R.drawable.ic_clipboard, listener::onOpenClipboard);
        action(symbols ? "Volver a letras" : "Abrir símbolos", R.drawable.ic_symbols, listener::onToggleSymbols);
        action("Activar o configurar teclado", R.drawable.ic_settings, listener::onOpenSettings);
        action("Ocultar teclado", R.drawable.ic_keyboard_hide, listener::onHideKeyboard);
        action("Volver al teclado", R.drawable.ic_keyboard, listener::onClose);
    }

    private void action(String label, int icon, Runnable callback) {
        content.addView(PanelUi.actionButton(getContext(), label, icon, callback),
                PanelUi.actionParams(getContext()));
    }
}
