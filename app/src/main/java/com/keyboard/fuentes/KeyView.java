package com.keyboard.fuentes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

/** Rounded key whose special symbols are the same SVGs used throughout the app. */
class KeyView extends View {
    enum Icon {
        NONE(0), SHIFT(R.drawable.ic_shift), SHIFT_LOCK(R.drawable.ic_shift_lock),
        BACKSPACE(R.drawable.ic_backspace), ENTER(R.drawable.ic_enter),
        SEARCH(R.drawable.ic_search), SEND(R.drawable.ic_send), DONE(R.drawable.ic_done),
        NEXT(R.drawable.ic_next), PREVIOUS(R.drawable.ic_previous), GO(R.drawable.ic_go);

        final int resource;
        Icon(int resource) { this.resource = resource; }
        boolean isAction() { return this == ENTER || this == SEARCH || this == SEND
                || this == DONE || this == NEXT || this == PREVIOUS || this == GO; }
    }

    enum Appearance { NORMAL, SPECIAL, SPACE }

    private String primary = "";
    private String secondary = "";
    private Icon icon = Icon.NONE;
    private Drawable iconDrawable;
    private Appearance appearance = Appearance.NORMAL;
    private boolean active;

    private final RectF keyRect = new RectF();
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint primaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint secondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public KeyView(Context context) {
        super(context);
        init();
    }

    public KeyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        primaryPaint.setColor(PanelUi.color(getContext(), R.color.text_primary));
        primaryPaint.setTextAlign(Paint.Align.CENTER);
        primaryPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        secondaryPaint.setColor(PanelUi.color(getContext(), R.color.text_secondary));
        secondaryPaint.setTextAlign(Paint.Align.RIGHT);
        secondaryPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        setClickable(true);
        setLongClickable(true);
        setFocusable(true);
        setHapticFeedbackEnabled(true);
    }

    void configure(String primary, String secondary, Icon icon, Appearance appearance,
                   boolean active, String description) {
        this.primary = primary == null ? "" : primary;
        this.secondary = secondary == null ? "" : secondary;
        this.appearance = appearance == null ? Appearance.NORMAL : appearance;
        setIcon(icon);
        setActive(active);
        setContentDescription(description == null ? this.primary : description);
        invalidate();
    }

    void setIcon(Icon value) {
        Icon next = value == null ? Icon.NONE : value;
        if (icon != next || (next != Icon.NONE && iconDrawable == null)) {
            icon = next;
            iconDrawable = next == Icon.NONE ? null : PanelUi.icon(getContext(), next.resource,
                    PanelUi.color(getContext(), next.isAction() || active ? R.color.accent : R.color.text_primary));
        }
        invalidate();
    }

    void setActive(boolean active) {
        this.active = active;
        setSelected(active);
        if (iconDrawable != null) {
            iconDrawable.setTint(PanelUi.color(getContext(),
                    active || icon.isAction() ? R.color.accent : R.color.text_primary));
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float inset = dp(0.5f);
        keyRect.set(inset, inset, width - inset, height - inset);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(backgroundColor());
        float radius = Math.min(dp(9), Math.min(width, height) * 0.2f);
        canvas.drawRoundRect(keyRect, radius, radius, backgroundPaint);
        if (isFocused()) {
            backgroundPaint.setStyle(Paint.Style.STROKE);
            backgroundPaint.setStrokeWidth(dp(1.5f));
            backgroundPaint.setColor(PanelUi.color(getContext(), R.color.accent));
            canvas.drawRoundRect(keyRect, radius, radius, backgroundPaint);
        }
        if (iconDrawable != null) {
            float size = Math.min(dp(24), Math.min(width * 0.6f, height * 0.56f));
            int left = Math.round((width - size) / 2f);
            int top = Math.round((height - size) / 2f);
            iconDrawable.setBounds(left, top, Math.round(left + size), Math.round(top + size));
            iconDrawable.draw(canvas);
            return;
        }
        float textSize = primaryTextSize();
        primaryPaint.setTextSize(textSize);
        float available = Math.max(1, width - dp(8));
        while (textSize > sp(9) && primaryPaint.measureText(primary) > available) {
            primaryPaint.setTextSize(--textSize);
        }
        Paint.FontMetrics metrics = primaryPaint.getFontMetrics();
        canvas.drawText(primary, width / 2f,
                height / 2f - (metrics.ascent + metrics.descent) / 2f, primaryPaint);
        if (!secondary.isEmpty()) {
            secondaryPaint.setTextSize(Math.min(sp(11), Math.min(height * 0.22f, width * 0.28f)));
            float top = Math.min(dp(5), height * 0.1f);
            canvas.drawText(secondary, width - dp(5), top - secondaryPaint.ascent(), secondaryPaint);
        }
    }

    private int backgroundColor() {
        int resource = isPressed() ? R.color.key_pressed
                : active ? R.color.selected
                : appearance == Appearance.SPECIAL ? R.color.surface
                : appearance == Appearance.SPACE ? R.color.space : R.color.key;
        return PanelUi.color(getContext(), resource);
    }

    private float primaryTextSize() {
        float desired = appearance == Appearance.SPACE || primary.length() > 3 ? sp(16)
                : primary.length() > 1 ? sp(19) : sp(25);
        return Math.min(desired, Math.max(sp(10), getHeight() * 0.46f));
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

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
