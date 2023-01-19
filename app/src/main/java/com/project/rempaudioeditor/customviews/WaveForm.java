package com.project.rempaudioeditor.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.rempaudioeditor.utils.UnitConverter;

import java.util.ArrayList;

public class WaveForm extends View {
    private ArrayList<Double> values;
    private ArrayList<Double> distributed_values;

    private int width;
    private int height;
    private final Paint bar_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float no_of_bars = 0;

    public final double BAR_WIDTH = UnitConverter.convertDpToPx(getContext(), 1.5);
    public final double BAR_DISTANCE = UnitConverter.convertDpToPx(getContext(), 1.2);
    public final double PADDING = UnitConverter.convertDpToPx(getContext(), 6);
    private float scaleFactor = 1.f;

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

    public void setBars(ArrayList<Double> values) {
        this.values = values;
        distributed_values = values;
        requestLayout();
        postInvalidate();
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;


        if (values != null && !values.isEmpty()) {
            distributed_values = distribute(values, (int) (scaleFactor * values.size()));
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if ((values == null) || (values.isEmpty())) {
            width = 0;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        else {
            width = (int) ((distributed_values.size() * (BAR_WIDTH + BAR_DISTANCE) - BAR_DISTANCE));
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

        for (int i = 0; i < distributed_values.size(); i++) {
            float max_height = (float) (height - (PADDING * 2));
            float bar_height = (float) (max_height * distributed_values.get(i) / Math.pow(32767, 0.7));
            float left = (float) (i * (BAR_WIDTH + BAR_DISTANCE));
            float top = (height - bar_height) / 2;
            float right = (float) (left + BAR_WIDTH);
            float bottom = top + bar_height;

            if (((left > PADDING) && (right > PADDING)) && ((left < (width - PADDING)) && (right < (width - PADDING))))
                canvas.drawRect(left, top, right, bottom, bar_paint);
        }
    }

    private static ArrayList<Double> distribute(ArrayList<Double> a, int n) {
        if (n < 1) {
            return new ArrayList<>();
        }
        ArrayList<Double> elements = new ArrayList<>();
        int totalItems = a.size();
        double interval = ((double) totalItems) / ((double) n);
        for (int i = 0; i < n; i++) {
            elements.add(a.get((int) Math.round(i * interval)));
        }
        return elements;
    }
}