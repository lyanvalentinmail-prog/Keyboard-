package com.keyboard.fuentes;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.EnumMap;
import java.util.Map;

/** Font selector with visible selection, checkmark and accessible touch targets. */
class FontPickerView extends LinearLayout {
    interface Listener {
        void onStyleSelected(FontStyle style);
        void onClose();
    }

    private final Map<FontStyle, TextView> buttons = new EnumMap<>(FontStyle.class);
    private FontStyle current = FontStyle.NORMAL;

    FontPickerView(Context context, Listener listener) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(PanelUi.color(context, R.color.panel));
        int padding = PanelUi.dp(context, 14);
        setPadding(padding, PanelUi.dp(context, 10), padding, PanelUi.dp(context, 12));
        addView(PanelUi.header(context, "Elige la fuente", "Cerrar selector de fuentes", listener::onClose));
        addView(PanelUi.hint(context, "El estilo se aplica al texto nuevo. Los emojis y los signos se conservan."));
        FontStyle[] styles = FontStyle.values();
        for (int index = 0; index < styles.length; index += 2) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(HORIZONTAL);
            row.addView(styleButton(styles[index], listener), buttonParams());
            if (index + 1 < styles.length) {
                row.addView(styleButton(styles[index + 1], listener), buttonParams());
            } else {
                row.addView(new View(context), buttonParams());
            }
            addView(row, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
        }
        setCurrentStyle(current);
    }

    void setCurrentStyle(FontStyle style) {
        current = style == null ? FontStyle.NORMAL : style;
        for (Map.Entry<FontStyle, TextView> entry : buttons.entrySet()) {
            boolean selected = entry.getKey() == current;
            TextView button = entry.getValue();
            button.setSelected(selected);
            button.setBackground(PanelUi.background(getContext(), PanelUi.color(getContext(),
                    selected ? R.color.selected : R.color.key), selected));
            Drawable check = selected ? PanelUi.icon(getContext(), R.drawable.ic_check,
                    PanelUi.color(getContext(), R.color.accent)) : null;
            if (check != null) {
                int size = PanelUi.dp(getContext(), 18);
                check.setBounds(0, 0, size, size);
            }
            button.setCompoundDrawables(null, null, check, null);
            button.setContentDescription("Usar fuente " + entry.getKey().getLabel()
                    + (selected ? ", seleccionada" : ""));
        }
    }

    private TextView styleButton(FontStyle style, Listener listener) {
        TextView button = new TextView(getContext());
        button.setText(style.getLabel() + "\n" + style.preview());
        button.setTextColor(PanelUi.color(getContext(), R.color.text_primary));
        button.setTextSize(14);
        button.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setMinHeight(PanelUi.dp(getContext(), 62));
        button.setPadding(PanelUi.dp(getContext(), 12), PanelUi.dp(getContext(), 8),
                PanelUi.dp(getContext(), 10), PanelUi.dp(getContext(), 8));
        button.setCompoundDrawablePadding(PanelUi.dp(getContext(), 4));
        button.setLineSpacing(PanelUi.dp(getContext(), 3), 1f);
        button.setFocusable(true);
        button.setOnClickListener(view -> listener.onStyleSelected(style));
        buttons.put(style, button);
        return button;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LayoutParams.WRAP_CONTENT, 1f);
        int margin = PanelUi.dp(getContext(), 3);
        params.setMargins(margin, margin, margin, margin);
        return params;
    }
}
