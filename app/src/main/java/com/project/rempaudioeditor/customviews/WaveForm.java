package com.project.rempaudioeditor.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.rempaudioeditor.utils.UnitConverter;

import java.util.ArrayList;

public class WaveForm extends View {
    private ArrayList<Double> values;

    private int width;
    private int height;
    private final Paint bar_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float no_of_bars = 0;

    public final float BARS_PER_SEC = 20;
    public final double BAR_WIDTH = UnitConverter.convertDpToPx(getContext(), 1.5);
    public final double BAR_DISTANCE = UnitConverter.convertDpToPx(getContext(), 1.2);
    public final double PADDING = UnitConverter.convertDpToPx(getContext(), 12);

    public WaveForm(Context context) {
        super(context);
        init();
    }

    public WaveForm(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveForm(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        values = null;
    }

    public void setBarColor(int color) {
        bar_paint.setColor(color);
    }

    public void setBars(ArrayList<Double> values, float duration_in_milisec) {
        this.values = values;
        this.no_of_bars = (duration_in_milisec / 1000) * BARS_PER_SEC;
        requestLayout();
        postInvalidate();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if ((values == null) || (values.isEmpty())) {
            width = 0;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        else {
            width = (int) (no_of_bars * (BAR_WIDTH + BAR_DISTANCE) - BAR_DISTANCE  + PADDING * 2);
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
        }
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values == null || values.isEmpty() || width == 0) {
            return;
        }

        int barNum = 0;

        for (int i = 0; i < values.size(); i++) {
            float max_height = (float) (height - (PADDING * 2));
            float bar_height = (float) (max_height * values.get(i) / Math.pow(32767, 0.7));
            float left = (float) (barNum * (BAR_WIDTH + BAR_DISTANCE) + PADDING);
            float top = (height - bar_height)/2;
            float right = (float) (left + BAR_WIDTH);
            float bottom = top + bar_height;

            canvas.drawRect(left, top, right, bottom, bar_paint);
            barNum++;
        }
    }
}