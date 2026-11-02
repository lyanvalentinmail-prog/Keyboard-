package com.keyboard.fuentes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/** Tecla redondeada dibujada a medida, con símbolo secundario e iconos vectoriales. */
class KeyView extends View {
    enum Icon {
        NONE, SHIFT, BACKSPACE, SEARCH
    }

    enum Appearance {
        NORMAL, SPECIAL, SPACE
    }

    private static final int COLOR_NORMAL = Color.parseColor("#303030");
    private static final int COLOR_SPECIAL = Color.parseColor("#252525");
    private static final int COLOR_SPACE = Color.parseColor("#343434");
    private static final int COLOR_PRESSED = Color.parseColor("#454545");
    private static final int COLOR_ACTIVE = Color.parseColor("#2C3F5C");
    private static final int COLOR_PRIMARY = Color.parseColor("#F5F5F5");
    private static final int COLOR_SECONDARY = Color.parseColor("#A0A0A0");
    private static final int COLOR_ACCENT = Color.parseColor("#4C8DFF");

    private String primary = "";
    private String secondary = "";
    private Icon icon = Icon.NONE;
    private Appearance appearance = Appearance.NORMAL;
    private boolean active = false;
    private float cornerRadius;

    private final RectF keyRect = new RectF();
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint primaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint secondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public KeyView(Context context) {
        super(context);
        init();
    }

    public KeyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        cornerRadius = dp(8);
        primaryPaint.setColor(COLOR_PRIMARY);
        primaryPaint.setTextAlign(Paint.Align.CENTER);
        primaryPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        secondaryPaint.setColor(COLOR_SECONDARY);
        secondaryPaint.setTextAlign(Paint.Align.RIGHT);
        secondaryPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        iconPaint.setColor(COLOR_PRIMARY);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);

        setClickable(true);
        setLongClickable(true);
        setFocusable(true);
        setHapticFeedbackEnabled(true);
    }

    void configure(String primary, String secondary, Icon icon, Appearance appearance,
                   boolean active, String description) {
        this.primary = primary == null ? "" : primary;
        this.secondary = secondary == null ? "" : secondary;
        this.icon = icon == null ? Icon.NONE : icon;
        this.appearance = appearance == null ? Appearance.NORMAL : appearance;
        this.active = active;
        setContentDescription(description == null ? this.primary : description);
        invalidate();
    }

    void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float inset = dp(0.5f);
        keyRect.set(inset, inset, width - inset, height - inset);
        backgroundPaint.setColor(backgroundColor());
        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, backgroundPaint);

        if (icon != Icon.NONE) {
            drawIcon(canvas, width, height);
            return;
        }

        primaryPaint.setTextSize(primaryTextSize());
        Paint.FontMetrics metrics = primaryPaint.getFontMetrics();
        float baseline = height / 2f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(primary, width / 2f, baseline, primaryPaint);

        if (!secondary.isEmpty()) {
            secondaryPaint.setTextSize(sp(12));
            float top = dp(6);
            canvas.drawText(secondary, width - dp(7), top - secondaryPaint.ascent(), secondaryPaint);
        }
    }

    private int backgroundColor() {
        if (isPressed()) {
            return COLOR_PRESSED;
        }
        if (active) {
            return COLOR_ACTIVE;
        }
        switch (appearance) {
            case SPECIAL:
                return COLOR_SPECIAL;
            case SPACE:
                return COLOR_SPACE;
            case NORMAL:
            default:
                return COLOR_NORMAL;
        }
    }

    private float primaryTextSize() {
        if (appearance == Appearance.SPACE || primary.length() > 3) {
            return sp(17);
        }
        if (primary.length() > 1) {
            return sp(20);
        }
        return sp(26);
    }

    private void drawIcon(Canvas canvas, float width, float height) {
        int color = COLOR_PRIMARY;
        if (icon == Icon.SEARCH) {
            color = COLOR_ACCENT;
        } else if (active && icon == Icon.SHIFT) {
            color = COLOR_ACCENT;
        }
        iconPaint.setColor(color);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(dp(2.4f));
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);

        switch (icon) {
            case SHIFT:
                drawShift(canvas, width, height);
                break;
            case BACKSPACE:
                drawBackspace(canvas, width, height);
                break;
            case SEARCH:
                drawSearch(canvas, width, height);
                break;
            case NONE:
            default:
                break;
        }
    }

    private void drawShift(Canvas canvas, float width, float height) {
        Path arrow = new Path();
        float center = width / 2f;
        float top = height * 0.23f;
        float shoulder = height * 0.48f;
        float bottom = height * 0.75f;
        arrow.moveTo(center, top);
        arrow.lineTo(width * 0.29f, shoulder);
        arrow.lineTo(width * 0.40f, shoulder);
        arrow.lineTo(width * 0.40f, bottom);
        arrow.lineTo(width * 0.60f, bottom);
        arrow.lineTo(width * 0.60f, shoulder);
        arrow.lineTo(width * 0.71f, shoulder);
        arrow.close();
        canvas.drawPath(arrow, iconPaint);

        if (active) {
            iconPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(width * 0.40f, height * 0.80f, width * 0.60f, height * 0.84f, iconPaint);
        }
    }

    private void drawBackspace(Canvas canvas, float width, float height) {
        Path outline = new Path();
        outline.moveTo(width * 0.28f, height * 0.32f);
        outline.lineTo(width * 0.78f, height * 0.32f);
        outline.lineTo(width * 0.88f, height * 0.50f);
        outline.lineTo(width * 0.78f, height * 0.68f);
        outline.lineTo(width * 0.28f, height * 0.68f);
        outline.lineTo(width * 0.18f, height * 0.50f);
        outline.close();
        canvas.drawPath(outline, iconPaint);
        canvas.drawLine(width * 0.47f, height * 0.42f, width * 0.69f, height * 0.60f, iconPaint);
        canvas.drawLine(width * 0.69f, height * 0.42f, width * 0.47f, height * 0.60f, iconPaint);
    }

    private void drawSearch(Canvas canvas, float width, float height) {
        float radius = Math.min(width, height) * 0.16f;
        float centerX = width * 0.45f;
        float centerY = height * 0.44f;
        canvas.drawCircle(centerX, centerY, radius, iconPaint);
        canvas.drawLine(centerX + radius * 0.72f, centerY + radius * 0.72f,
                width * 0.70f, height * 0.69f, iconPaint);
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

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
