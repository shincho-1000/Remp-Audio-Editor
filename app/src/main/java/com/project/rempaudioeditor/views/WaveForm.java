package com.project.rempaudioeditor.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.rempaudioeditor.converters.UnitConverter;

public class WaveForm extends View {
    private byte[] bytes;

    private int width;
    private int height;
    private final Paint bar_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float no_of_bars = 0;

    public final float BARS_PER_SEC = 20;
    public final double BAR_WIDTH = UnitConverter.convertDpToPx(getContext(), 1.5);
    public final double BAR_DISTANCE = UnitConverter.convertDpToPx(getContext(), 1.5);
    public final double PADDING = UnitConverter.convertDpToPx(getContext(), 5);

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
        bytes = null;
    }

    public void setBarColor(int color) {
        bar_paint.setColor(color);
    }

    public void setBars(byte[] bytes, float duration_in_milisec) {
        this.bytes = bytes;
        this.no_of_bars = (duration_in_milisec / 1000) * BARS_PER_SEC;
        requestLayout();
        postInvalidate();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (bytes == null) {
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

        if (bytes == null || width == 0) {
            return;
        }

        if (no_of_bars <= 0.1f) {
            return;
        }

        byte value;
        float samplesPerBar = bytes.length / no_of_bars;
        float barCounter = 0;
        int nextBarNum = 0;

        int barNum = 0;
        int lastBarNum;
        int drawBarCount;

        for (int i = 0; i < bytes.length; i++) {
            if (i != nextBarNum) {
                continue;
            }

            drawBarCount = 0;
            lastBarNum = nextBarNum;

            while (lastBarNum == nextBarNum) {
                barCounter += samplesPerBar;
                nextBarNum = (int) barCounter;
                drawBarCount++;
            }

            int bitPointer = i * 5;
            int byteNum = bitPointer / Byte.SIZE;
            int byteBitOffset = bitPointer - byteNum * Byte.SIZE;
            int currentByteCount = Byte.SIZE - byteBitOffset;
            int nextByteRest = 5 - currentByteCount;

            value = (byte) ((bytes[byteNum] >> byteBitOffset) & ((2 << (Math.min(5, currentByteCount) - 1)) - 1));

            if (nextByteRest > 0) {
                value <<= nextByteRest;
                value |= bytes[byteNum + 1] & ((2 << (nextByteRest - 1)) - 1);
            }

            for (int j = 0; j < drawBarCount; j++) {
                float max_height = (float) (height - (PADDING * 2));
                float bar_height = Math.max(1, max_height * value / 31.0f);
                float left = (float) (barNum * (BAR_WIDTH + BAR_DISTANCE) + PADDING);
                float top = (height - bar_height)/2;
                float right = (float) (left + BAR_WIDTH);
                float bottom = top + bar_height;

                canvas.drawRect(left, top, right, bottom, bar_paint);
                barNum++;
            }
        }
    }
}