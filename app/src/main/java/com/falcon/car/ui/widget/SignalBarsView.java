package com.falcon.car.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.falcon.car.R;

/** Four rising bars showing link quality, 0 to {@code barCount}. */
public class SignalBarsView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private int level = 0;
    private int barCount = 4;
    private int activeColor;
    private int inactiveColor;

    public SignalBarsView(Context context) {
        this(context, null);
    }

    public SignalBarsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalBarsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        activeColor = ContextCompat.getColor(context, R.color.redline_red);
        inactiveColor = ContextCompat.getColor(context, R.color.redline_border_strong);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SignalBarsView);
            level = a.getInt(R.styleable.SignalBarsView_signalLevel, level);
            barCount = a.getInt(R.styleable.SignalBarsView_signalBarCount, barCount);
            activeColor = a.getColor(R.styleable.SignalBarsView_signalActiveColor, activeColor);
            inactiveColor = a.getColor(R.styleable.SignalBarsView_signalInactiveColor, inactiveColor);
            a.recycle();
        }

        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize((int) dp(22), widthMeasureSpec);
        int height = resolveSize((int) dp(18), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (barCount <= 0) {
            return;
        }

        float gap = dp(2);
        float barWidth = (getWidth() - gap * (barCount - 1)) / barCount;
        float radius = barWidth / 3f;
        float minHeight = getHeight() * 0.3f;
        float step = (getHeight() - minHeight) / Math.max(barCount - 1, 1);

        for (int i = 0; i < barCount; i++) {
            float barHeight = minHeight + step * i;
            float left = i * (barWidth + gap);
            barRect.set(left, getHeight() - barHeight, left + barWidth, getHeight());
            paint.setColor(i < level ? activeColor : inactiveColor);
            canvas.drawRoundRect(barRect, radius, radius, paint);
        }
    }

    public void setLevel(int newLevel) {
        level = Math.max(0, Math.min(newLevel, barCount));
        invalidate();
    }

    public int getLevel() {
        return level;
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }
}
