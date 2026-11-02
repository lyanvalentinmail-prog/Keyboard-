package com.keyboard.fuentes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Panel de accesos rápidos mostrado por el botón de tres puntos. */
class MorePanelView extends LinearLayout {
    interface Listener {
        void onOpenFonts();

        void onOpenEmojis();

        void onToggleSymbols();

        void onOpenSettings();

        void onClose();
    }

    private static final int COLOR_PANEL = Color.parseColor("#141414");
    private static final int COLOR_CARD = Color.parseColor("#222222");
    private static final int COLOR_BUTTON = Color.parseColor("#303030");
    private static final int COLOR_TEXT = Color.parseColor("#F5F5F5");
    private static final int COLOR_HINT = Color.parseColor("#A0A0A0");
    private static final int COLOR_ACCENT = Color.parseColor("#4C8DFF");

    private final Listener listener;
    private FontStyle style = FontStyle.NORMAL;
    private boolean symbols = false;

    MorePanelView(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setOrientation(VERTICAL);
        setBackgroundColor(COLOR_PANEL);
        setPadding(dp(14), dp(10), dp(14), dp(12));
        build();
    }

    void refresh(FontStyle style, boolean symbols) {
        this.style = style == null ? FontStyle.NORMAL : style;
        this.symbols = symbols;
        removeAllViews();
        build();
    }

    private void build() {
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(getContext());
        title.setText("Más opciones");
        title.setTextColor(COLOR_TEXT);
        title.setTextSize(17);
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        TextView close = new TextView(getContext());
        close.setText("✕");
        close.setTextColor(COLOR_TEXT);
        close.setTextSize(20);
        close.setGravity(Gravity.CENTER);
        close.setBackground(buttonBackground(COLOR_BUTTON));
        close.setClickable(true);
        close.setFocusable(true);
        close.setContentDescription("Cerrar más opciones");
        close.setOnClickListener(view -> listener.onClose());

        header.addView(title, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        header.addView(close, new LinearLayout.LayoutParams(dp(40), dp(40)));
        addView(header, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView status = new TextView(getContext());
        status.setText("Fuente actual: " + style.getLabel() + "\n" + style.preview());
        status.setTextColor(COLOR_TEXT);
        status.setTextSize(14);
        status.setLineSpacing(dp(2), 1f);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(12), dp(8), dp(12), dp(8));
        status.setBackground(buttonBackground(COLOR_CARD));
        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        statusParams.setMargins(0, dp(8), 0, dp(6));
        addView(status, statusParams);

        TextView hint = new TextView(getContext());
        hint.setText("Mantén pulsadas las teclas para escribir el símbolo pequeño. "
                + "Mantén pulsado ⇧ para bloquear mayúsculas.");
        hint.setTextColor(COLOR_HINT);
        hint.setTextSize(12);
        hint.setPadding(0, 0, 0, dp(6));
        addView(hint, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        addView(actionButton("Elegir fuente", () -> listener.onOpenFonts()), actionParams());
        addView(actionButton("Emojis", () -> listener.onOpenEmojis()), actionParams());
        addView(actionButton(symbols ? "Volver a letras" : "Abrir símbolos",
                () -> listener.onToggleSymbols()), actionParams());
        addView(actionButton("Activar o configurar teclado",
                () -> listener.onOpenSettings()), actionParams());
        addView(actionButton("Volver al teclado", () -> listener.onClose()), actionParams());
    }

    private TextView actionButton(String label, Runnable action) {
        TextView button = new TextView(getContext());
        button.setText(label);
        button.setTextColor(COLOR_TEXT);
        button.setTextSize(14);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        button.setBackground(buttonBackground(COLOR_BUTTON));
        button.setClickable(true);
        button.setFocusable(true);
        button.setContentDescription(label);
        button.setOnClickListener(view -> action.run());
        return button;
    }

    private LinearLayout.LayoutParams actionParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(2), 0, dp(2));
        return params;
    }

    private GradientDrawable buttonBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(10));
        drawable.setColor(color);
        drawable.setStroke((int) dp(1), COLOR_ACCENT);
        return drawable;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
