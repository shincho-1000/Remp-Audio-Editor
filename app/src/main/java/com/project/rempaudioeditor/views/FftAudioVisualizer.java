package com.project.rempaudioeditor.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.converters.UnitConverter;

import java.util.ArrayList;

public class FftAudioVisualizer extends View {
    private final Paint bar_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private VectorDrawableCompat center_img;
    private ArrayList<Integer> barLengths;
    private final RectF bar = new RectF();

    public FftAudioVisualizer(Context context) {
        super(context);
        init(null);
    }

    public FftAudioVisualizer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FftAudioVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrsSet) {
        if (attrsSet != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrsSet, R.styleable.FftAudioVisualizer);

            bar_paint.setColor(typedArray.getColor(R.styleable.FftAudioVisualizer_fftViewBarColor, Color.BLACK));

            int drawableId = typedArray.getResourceId(R.styleable.FftAudioVisualizer_centralImage, 0);
            if(drawableId != 0){
                center_img = VectorDrawableCompat.create(getResources(), drawableId, null);
            }
            typedArray.recycle();
        }
    }

    public void setBars(ArrayList<Integer> barLengths) {
        this.barLengths = barLengths;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((barLengths != null) && (barLengths.size() != 0)) {
            int canvas_width = getWidth();
            int canvas_height = getHeight();
            int max_bar_length = canvas_width / 4;
            int min_bar_length = (int) UnitConverter.convertDpToPx(getContext(), 1);
            int bar_width = (int) UnitConverter.convertDpToPx(getContext(), 5);
            int circle_radius = (canvas_width - max_bar_length * 2) / 2;
            float rotationInDeg = 360f / barLengths.size();

            for (int i = 0; i < barLengths.size(); i++) {
                float bar_height = ((Math.abs(barLengths.get(i)) + min_bar_length) / 128f) * max_bar_length;
                bar.left = (canvas_width - bar_width) / 2f;
                bar.top = canvas_height / 3f - bar_height;
                bar.right = canvas_width - bar.left;
                bar.bottom = canvas_height / 3f;
                canvas.drawRoundRect(bar, bar_width /2f, bar_width /2f, bar_paint);
                canvas.rotate(rotationInDeg, canvas_width / 2f, canvas_height / 2f);
            }

            int left = (canvas_width - circle_radius) / 2;
            int top = (canvas_height - circle_radius) / 2;
            center_img.setBounds(left, top, left + circle_radius, top + circle_radius);
            center_img.draw(canvas);
        }
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }
}
