package com.falcon.car.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.falcon.car.R;

import java.util.Locale;

/**
 * Instrument-cluster style arc gauge: a 270 degree track opening at the bottom,
 * a red progress arc, tick marks and a centred readout.
 */
public class GaugeView extends View {

    private static final float START_ANGLE = 135f;
    private static final float SWEEP_ANGLE = 270f;
    private static final int TICK_COUNT = 9;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint unitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private float max = 100f;
    private float value = 0f;
    private int decimals = 0;
    private boolean showTicks = true;
    private float thickness;
    private String unit = "";
    private String label = "";

    @Nullable
    private ValueAnimator animator;

    public GaugeView(Context context) {
        this(context, null);
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        thickness = dp(10);
        int trackColor = ContextCompat.getColor(context, R.color.redline_card_raised);
        int progressColor = ContextCompat.getColor(context, R.color.redline_red);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView);
            max = a.getFloat(R.styleable.GaugeView_gaugeMax, max);
            value = a.getFloat(R.styleable.GaugeView_gaugeValue, value);
            decimals = a.getInt(R.styleable.GaugeView_gaugeDecimals, decimals);
            showTicks = a.getBoolean(R.styleable.GaugeView_gaugeTicks, true);
            thickness = a.getDimension(R.styleable.GaugeView_gaugeThickness, thickness);
            trackColor = a.getColor(R.styleable.GaugeView_gaugeTrackColor, trackColor);
            progressColor = a.getColor(R.styleable.GaugeView_gaugeProgressColor, progressColor);
            String rawUnit = a.getString(R.styleable.GaugeView_gaugeUnit);
            String rawLabel = a.getString(R.styleable.GaugeView_gaugeLabel);
            unit = rawUnit == null ? "" : rawUnit;
            label = rawLabel == null ? "" : rawLabel;
            a.recycle();
        }

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(trackColor);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);

        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeWidth(dp(1.5f));
        tickPaint.setColor(ContextCompat.getColor(context, R.color.redline_border_strong));

        valuePaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);

        unitPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        unitPaint.setTextAlign(Paint.Align.CENTER);

        labelPaint.setColor(progressColor);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        // The progress arc carries a soft glow, which needs a software layer.
        progressPaint.setShadowLayer(dp(6), 0, 0, withAlpha(progressColor, 0.55f));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultSize = (int) dp(200);
        int width = resolveSize(defaultSize, widthMeasureSpec);
        int height = resolveSize(defaultSize, heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        trackPaint.setStrokeWidth(thickness);
        progressPaint.setStrokeWidth(thickness);

        float inset = thickness / 2f + dp(6);
        arcRect.set(inset, inset, w - inset, h - inset);

        float radius = Math.min(w, h) / 2f;
        valuePaint.setTextSize(radius * 0.46f);
        unitPaint.setTextSize(radius * 0.15f);
        labelPaint.setTextSize(radius * 0.14f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE, false, trackPaint);

        if (showTicks) {
            drawTicks(canvas);
        }

        float fraction = max <= 0f ? 0f : Math.min(Math.max(value / max, 0f), 1f);
        if (fraction > 0f) {
            canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE * fraction, false, progressPaint);
        }

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        canvas.drawText(format(value), cx, cy + valuePaint.getTextSize() * 0.18f, valuePaint);

        if (!unit.isEmpty()) {
            canvas.drawText(unit, cx, cy + valuePaint.getTextSize() * 0.9f, unitPaint);
        }
        if (!label.isEmpty()) {
            canvas.drawText(label, cx, cy - valuePaint.getTextSize() * 0.62f, labelPaint);
        }
    }

    private void drawTicks(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float outer = arcRect.width() / 2f - thickness / 2f - dp(4);
        float inner = outer - dp(6);

        for (int i = 0; i < TICK_COUNT; i++) {
            double angle = Math.toRadians(START_ANGLE + (SWEEP_ANGLE / (TICK_COUNT - 1)) * i);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            canvas.drawLine(cx + cos * inner, cy + sin * inner,
                    cx + cos * outer, cy + sin * outer, tickPaint);
        }
    }

    private String format(float raw) {
        if (decimals <= 0) {
            return String.valueOf(Math.round(raw));
        }
        return String.format(Locale.getDefault(), "%." + decimals + "f", raw);
    }

    /** Sets the reading without animating, e.g. when binding a fresh screen. */
    public void setValue(float newValue) {
        cancelAnimation();
        value = newValue;
        invalidate();
    }

    /** Sweeps from the current reading to {@code target}. */
    public void animateTo(float target) {
        cancelAnimation();
        ValueAnimator anim = ValueAnimator.ofFloat(value, target);
        anim.setDuration(900L);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(animation -> {
            value = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator = anim;
        anim.start();
    }

    public void setMax(float newMax) {
        max = newMax;
        invalidate();
    }

    public void setUnit(String newUnit) {
        unit = newUnit == null ? "" : newUnit;
        invalidate();
    }

    public void setLabel(String newLabel) {
        label = newLabel == null ? "" : newLabel;
        invalidate();
    }

    public void setDecimals(int newDecimals) {
        decimals = newDecimals;
        invalidate();
    }

    private void cancelAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelAnimation();
        super.onDetachedFromWindow();
    }

    private static int withAlpha(int color, float alpha) {
        return Color.argb((int) (255 * alpha), Color.red(color), Color.green(color),
                Color.blue(color));
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }
}
