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

/** Botón circular de la barra superior, con iconos dibujados sin dependencias externas. */
class ToolButtonView extends View {
    enum Icon {
        FONT, EMOJI, CLIPBOARD, SETTINGS, MORE
    }

    private static final int COLOR_CIRCLE = Color.parseColor("#2B2B2B");
    private static final int COLOR_CIRCLE_PRESSED = Color.parseColor("#3B3B3B");
    private static final int COLOR_CIRCLE_ACTIVE = Color.parseColor("#263448");
    private static final int COLOR_ICON = Color.parseColor("#F0F0F0");
    private static final int COLOR_ACCENT = Color.parseColor("#4C8DFF");

    private Icon icon;
    private boolean active = false;

    private final RectF shape = new RectF();
    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ToolButtonView(Context context, Icon icon) {
        super(context);
        this.icon = icon;
        init();
    }

    public ToolButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.icon = Icon.FONT;
        init();
    }

    private void init() {
        iconPaint.setColor(COLOR_ICON);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(dp(1.8f));
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);

        textPaint.setColor(COLOR_ICON);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp(16));
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));

        dotPaint.setColor(COLOR_ACCENT);
        dotPaint.setStyle(Paint.Style.FILL);

        setClickable(true);
        setFocusable(true);
        setHapticFeedbackEnabled(true);
    }

    void configure(Icon icon, String description) {
        this.icon = icon == null ? Icon.FONT : icon;
        setContentDescription(description);
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

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(Math.min(getWidth(), getHeight()) * 0.36f, dp(20));
        float scale = radius * 0.58f;

        circlePaint.setColor(isPressed() ? COLOR_CIRCLE_PRESSED
                : active ? COLOR_CIRCLE_ACTIVE : COLOR_CIRCLE);
        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        iconPaint.setColor(COLOR_ICON);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(dp(1.8f));
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);

        switch (icon) {
            case FONT:
                drawFont(canvas, centerX, centerY);
                break;
            case EMOJI:
                drawEmoji(canvas, centerX, centerY, scale);
                break;
            case CLIPBOARD:
                drawClipboard(canvas, centerX, centerY, scale);
                break;
            case SETTINGS:
                drawSettings(canvas, centerX, centerY, scale);
                break;
            case MORE:
            default:
                drawMore(canvas, centerX, centerY, scale);
                break;
        }

        if (active) {
            canvas.drawCircle(centerX + radius * 0.62f, centerY + radius * 0.62f, dp(2.6f), dotPaint);
        }
    }

    private void drawFont(Canvas canvas, float centerX, float centerY) {
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText("Aa", centerX, baseline, textPaint);
    }

    private void drawEmoji(Canvas canvas, float centerX, float centerY, float scale) {
        canvas.drawCircle(centerX, centerY, scale * 0.88f, iconPaint);

        iconPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX - scale * 0.34f, centerY - scale * 0.22f, dp(1.7f), iconPaint);
        canvas.drawCircle(centerX + scale * 0.34f, centerY - scale * 0.22f, dp(1.7f), iconPaint);
        iconPaint.setStyle(Paint.Style.STROKE);

        shape.set(centerX - scale * 0.48f, centerY - scale * 0.14f,
                centerX + scale * 0.48f, centerY + scale * 0.66f);
        canvas.drawArc(shape, 20f, 140f, false, iconPaint);
    }

    private void drawClipboard(Canvas canvas, float centerX, float centerY, float scale) {
        shape.set(centerX - scale * 0.62f, centerY - scale * 0.72f,
                centerX + scale * 0.62f, centerY + scale * 0.82f);
        canvas.drawRoundRect(shape, dp(2), dp(2), iconPaint);

        iconPaint.setStyle(Paint.Style.FILL);
        shape.set(centerX - scale * 0.28f, centerY - scale * 0.88f,
                centerX + scale * 0.28f, centerY - scale * 0.58f);
        canvas.drawRoundRect(shape, dp(1.5f), dp(1.5f), iconPaint);
    }

    private void drawSettings(Canvas canvas, float centerX, float centerY, float scale) {
        Path gear = new Path();
        int teeth = 8;
        double step = Math.PI * 2 / teeth;
        float outer = scale * 0.98f;
        float inner = scale * 0.66f;

        for (int i = 0; i < teeth; i++) {
            double start = -Math.PI / 2 + i * step;
            addPoint(gear, centerX, centerY, inner, start, i == 0);
            addPoint(gear, centerX, centerY, outer, start + step * 0.22, false);
            addPoint(gear, centerX, centerY, outer, start + step * 0.72, false);
            addPoint(gear, centerX, centerY, inner, start + step * 0.94, false);
        }
        gear.close();
        canvas.drawPath(gear, iconPaint);
        canvas.drawCircle(centerX, centerY, scale * 0.30f, iconPaint);
    }

    private void addPoint(Path path, float centerX, float centerY, float radius,
                          double angle, boolean moveTo) {
        float x = (float) (centerX + Math.cos(angle) * radius);
        float y = (float) (centerY + Math.sin(angle) * radius);
        if (moveTo) {
            path.moveTo(x, y);
        } else {
            path.lineTo(x, y);
        }
    }

    private void drawMore(Canvas canvas, float centerX, float centerY, float scale) {
        iconPaint.setStyle(Paint.Style.FILL);
        float dotRadius = dp(2.2f);
        canvas.drawCircle(centerX - scale * 0.62f, centerY, dotRadius, iconPaint);
        canvas.drawCircle(centerX, centerY, dotRadius, iconPaint);
        canvas.drawCircle(centerX + scale * 0.62f, centerY, dotRadius, iconPaint);
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
