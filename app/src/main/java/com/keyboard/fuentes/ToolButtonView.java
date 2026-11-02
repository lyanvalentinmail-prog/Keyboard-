package com.keyboard.fuentes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

/** Accessible circular toolbar button backed by a tintable SVG vector. */
class ToolButtonView extends View {
    enum Icon {
        FONT(R.drawable.ic_font), EMOJI(R.drawable.ic_emoji),
        CLIPBOARD(R.drawable.ic_clipboard), SETTINGS(R.drawable.ic_settings),
        MORE(R.drawable.ic_more);
        final int resource;
        Icon(int resource) { this.resource = resource; }
    }

    private Icon icon = Icon.FONT;
    private Drawable iconDrawable;
    private boolean active;
    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ToolButtonView(Context context, Icon icon) {
        super(context);
        init();
        configure(icon, null);
    }

    public ToolButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        configure(Icon.FONT, null);
    }

    private void init() {
        setClickable(true);
        setFocusable(true);
        setHapticFeedbackEnabled(true);
    }

    void configure(Icon value, String description) {
        icon = value == null ? Icon.FONT : value;
        iconDrawable = PanelUi.icon(getContext(), icon.resource,
                PanelUi.color(getContext(), R.color.text_primary));
        if (description != null) {
            setContentDescription(description);
            PanelUi.tooltip(this, description);
        }
        invalidate();
    }

    void setActive(boolean value) {
        active = value;
        setSelected(value);
        if (iconDrawable != null) {
            iconDrawable.setTint(PanelUi.color(getContext(), active ? R.color.accent : R.color.text_primary));
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(Math.min(getWidth(), getHeight()) * 0.46f, dp(22));
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(PanelUi.color(getContext(), isPressed() ? R.color.key_pressed
                : active ? R.color.selected : R.color.key));
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
        if (isFocused()) {
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeWidth(dp(1.5f));
            circlePaint.setColor(PanelUi.color(getContext(), R.color.accent));
            canvas.drawCircle(centerX, centerY, radius - dp(1), circlePaint);
        }
        float size = Math.min(dp(22), radius * 1.15f);
        int left = Math.round(centerX - size / 2f);
        int top = Math.round(centerY - size / 2f);
        iconDrawable.setBounds(left, top, Math.round(left + size), Math.round(top + size));
        iconDrawable.draw(canvas);
        if (active) {
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setColor(PanelUi.color(getContext(), R.color.accent));
            canvas.drawCircle(centerX, centerY + radius - dp(3), dp(1.5f), circlePaint);
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        invalidate();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.Button");
        info.setSelected(active);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
