package com.keyboard.fuentes;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared native controls: every panel uses the same SVGs, touch feedback and targets. */
final class PanelUi {
    private PanelUi() {
    }

    static int color(Context context, int resource) {
        return context.getResources().getColor(resource, context.getTheme());
    }

    static Drawable icon(Context context, int resource, int color) {
        Drawable drawable = context.getResources().getDrawable(resource, context.getTheme()).mutate();
        drawable.setTint(color);
        return drawable;
    }

    static Drawable background(Context context, int color, boolean selected) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(context, 12));
        if (selected) {
            shape.setStroke(dp(context, 1.5f), color(context, R.color.accent));
        }
        return new RippleDrawable(ColorStateList.valueOf(color(context, R.color.ripple)),
                shape, null);
    }

    static ImageButton iconButton(Context context, int icon, String description, Runnable action) {
        ImageButton button = new ImageButton(context);
        button.setImageDrawable(icon(context, icon, color(context, R.color.text_primary)));
        button.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        button.setPadding(dp(context, 11), dp(context, 11), dp(context, 11), dp(context, 11));
        button.setBackground(background(context, color(context, R.color.key), false));
        button.setContentDescription(description);
        tooltip(button, description);
        button.setMinimumWidth(dp(context, 44));
        button.setMinimumHeight(dp(context, 44));
        button.setFocusable(true);
        button.setOnClickListener(view -> action.run());
        return button;
    }

    static LinearLayout header(Context context, String title, String closeDescription, Runnable close) {
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = new TextView(context);
        label.setText(title);
        label.setTextColor(color(context, R.color.text_primary));
        label.setTextSize(17);
        label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        header.addView(label, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(iconButton(context, R.drawable.ic_close, closeDescription, close),
                new LinearLayout.LayoutParams(dp(context, 44), dp(context, 44)));
        return header;
    }

    static void tooltip(View view, String description) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            view.setTooltipText(description);
        }
    }

    static TextView actionButton(Context context, String label, int icon, Runnable action) {
        TextView button = new TextView(context);
        button.setText(label);
        button.setTextSize(14);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setTextColor(color(context, R.color.text_primary));
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setMinHeight(dp(context, 48));
        button.setPadding(dp(context, 14), dp(context, 10), dp(context, 14), dp(context, 10));
        Drawable drawable = icon(context, icon, color(context, R.color.accent));
        drawable.setBounds(0, 0, dp(context, 22), dp(context, 22));
        button.setCompoundDrawables(drawable, null, null, null);
        button.setCompoundDrawablePadding(dp(context, 12));
        button.setBackground(background(context, color(context, R.color.key), false));
        button.setFocusable(true);
        button.setContentDescription(label);
        button.setOnClickListener(view -> action.run());
        return button;
    }

    static TextView hint(Context context, String text) {
        TextView hint = new TextView(context);
        hint.setText(text);
        hint.setTextSize(12);
        hint.setLineSpacing(dp(context, 2), 1f);
        hint.setTextColor(color(context, R.color.text_secondary));
        hint.setPadding(0, dp(context, 8), 0, dp(context, 8));
        return hint;
    }

    static LinearLayout.LayoutParams actionParams(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(context, 3), 0, dp(context, 3));
        return params;
    }

    static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
