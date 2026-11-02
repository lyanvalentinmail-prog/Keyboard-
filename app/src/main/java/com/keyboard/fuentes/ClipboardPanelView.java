package com.keyboard.fuentes;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Only reads the system clipboard after an explicit user action; never stores a history. */
class ClipboardPanelView extends LinearLayout {
    interface Listener {
        void onPasteClipboard(boolean styled);
        void onCopySelection();
        void onRefreshClipboard();
        void onClose();
    }

    private final TextView preview;
    private final TextView pasteStyled;
    private final TextView pastePlain;
    private String clipText = "";

    ClipboardPanelView(Context context, Listener listener) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(PanelUi.color(context, R.color.panel));
        setPadding(PanelUi.dp(context, 14), PanelUi.dp(context, 10),
                PanelUi.dp(context, 14), PanelUi.dp(context, 12));
        addView(PanelUi.header(context, "Portapapeles", "Cerrar portapapeles", listener::onClose));
        addView(PanelUi.hint(context, "El texto solo se lee al abrir este panel. No se guarda un historial."));
        preview = new TextView(context);
        preview.setTextColor(PanelUi.color(context, R.color.text_primary));
        preview.setTextSize(16);
        preview.setMaxLines(5);
        preview.setEllipsize(TextUtils.TruncateAt.END);
        preview.setGravity(Gravity.CENTER_VERTICAL);
        preview.setPadding(PanelUi.dp(context, 14), PanelUi.dp(context, 12),
                PanelUi.dp(context, 14), PanelUi.dp(context, 12));
        preview.setBackground(PanelUi.background(context, PanelUi.color(context, R.color.surface), false));
        preview.setTextIsSelectable(true);
        addView(preview, PanelUi.actionParams(context));
        pasteStyled = PanelUi.actionButton(context, "Pegar con la fuente elegida", R.drawable.ic_font,
                () -> listener.onPasteClipboard(true));
        pastePlain = PanelUi.actionButton(context, "Pegar texto original", R.drawable.ic_paste,
                () -> listener.onPasteClipboard(false));
        addView(pasteStyled, PanelUi.actionParams(context));
        addView(pastePlain, PanelUi.actionParams(context));
        addView(PanelUi.actionButton(context, "Copiar texto seleccionado", R.drawable.ic_copy,
                listener::onCopySelection), PanelUi.actionParams(context));
        addView(PanelUi.actionButton(context, "Actualizar portapapeles", R.drawable.ic_refresh,
                listener::onRefreshClipboard), PanelUi.actionParams(context));
        refresh(null);
    }

    void refresh(String text) {
        clipText = text == null ? "" : text;
        boolean available = !clipText.isEmpty();
        preview.setText(available ? clipText : "No hay texto disponible. Copia texto en otra app y pulsa Actualizar.");
        preview.setContentDescription(available ? "Texto del portapapeles: " + clipText
                : "Portapapeles vacío o sin texto compatible");
        pasteStyled.setEnabled(available);
        pastePlain.setEnabled(available);
        pasteStyled.setAlpha(available ? 1f : 0.45f);
        pastePlain.setAlpha(available ? 1f : 0.45f);
    }
}
