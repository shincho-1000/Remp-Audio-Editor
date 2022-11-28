package com.project.rempaudioeditor.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.project.rempaudioeditor.utils.UnitConverter;

import java.util.ArrayList;

public class RecorderAudioVisualizer extends View {
    public final int MIN_BAR_LENGTH = (int) UnitConverter.convertDpToPx(getContext(), 1);
    public final float MAX_POSSIBLE_AMP = 32767;
    public final int BAR_WIDTH = (int) UnitConverter.convertDpToPx(getContext(), 3);
    public final int BAR_DISTANCE = (int) UnitConverter.convertDpToPx(getContext(), 3);

    private final Paint bar_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<Integer> amplitudes = new ArrayList<>();
    private final RectF bar = new RectF();

    public RecorderAudioVisualizer(Context context) {
        super(context);
        init();
    }

    public RecorderAudioVisualizer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecorderAudioVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bar_paint.setColor(Color.BLACK);
    }

    public void addAmp(int amp) {
        // Adds an amplitude to the arraylist and requests a redraw
        amplitudes.add(amp);
        requestLayout();
        postInvalidate();
    }

    public void clearAmps() {
        // Clears all amps and requests a redraw
        amplitudes.clear();
        requestLayout();
        postInvalidate();
    }

    public void setBarColor(int barColor) {
        bar_paint.setColor(barColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int canvas_height = getHeight();

        if ((amplitudes != null) && (amplitudes.size() > 0)) {
            for (int i = 0; i < amplitudes.size(); i++) {
                int amp = amplitudes.get(i);
                double ampRatio = (amp/MAX_POSSIBLE_AMP);


                float bar_height = (float) (ampRatio * canvas_height + MIN_BAR_LENGTH);
                bar.top = (canvas_height - bar_height)/2;
                bar.bottom = bar.top + bar_height;
                bar.left = (i * (BAR_WIDTH + BAR_DISTANCE));
                bar.right = bar.left + BAR_WIDTH;

                canvas.drawRect(bar, bar_paint);
            }
        }
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        if (amplitudes.size() != 0) {
            super.onMeasure(MeasureSpec
                    .makeMeasureSpec((int) (((BAR_DISTANCE + BAR_WIDTH) * amplitudes.size()) + BAR_DISTANCE), MeasureSpec.EXACTLY), heightSpec);
        } else
            super.onMeasure(widthSpec, heightSpec);
    }
}