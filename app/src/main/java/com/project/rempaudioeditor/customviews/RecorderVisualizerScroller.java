package com.project.rempaudioeditor.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import androidx.annotation.Nullable;

import com.example.rempaudioeditor.R;

public class RecorderVisualizerScroller extends HorizontalScrollView {
    private int bar_color = 0;
    private RecorderAudioVisualizer recorderAudioVisualizer;

    public RecorderVisualizerScroller(Context context) {
        super(context);
        init(null);
    }

    public RecorderVisualizerScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RecorderVisualizerScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    private void init(@Nullable AttributeSet attrsSet) {
        // Gets the attributes of the view
        if (attrsSet != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrsSet, R.styleable.RecorderVisualizerScroller);

            bar_color = typedArray.getColor(R.styleable.RecorderVisualizerScroller_recorderViewBarColor, Color.BLACK);

            typedArray.recycle();
        }

        setHorizontalScrollBarEnabled(false);

        recorderAudioVisualizer = new RecorderAudioVisualizer(getContext().getApplicationContext());

        if (bar_color != 0)
            recorderAudioVisualizer.setBarColor(bar_color);

        addView(recorderAudioVisualizer);
    }

    public void addAmplitude(int amp) {
        recorderAudioVisualizer.addAmplitude(amp);
        fullScroll(HorizontalScrollView.FOCUS_RIGHT);
    }

    public void clearAmplitudes() {
        recorderAudioVisualizer.clearAmplitudes();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
