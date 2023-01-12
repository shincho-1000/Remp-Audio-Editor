package com.project.rempaudioeditor.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.utils.UnitConverter;

import java.util.ArrayList;

public class ColorSelectionButton extends View {
    private final Paint btn_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean checked = false;
    private int unchecked_color;

    public ColorSelectionButton(Context context) {
        super(context);
        init(null);
    }

    public ColorSelectionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ColorSelectionButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrsSet) {
        if (attrsSet != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrsSet, R.styleable.ColorSelectionButton);

            unchecked_color = typedArray.getColor(R.styleable.ColorSelectionButton_uncheckedColor, Color.BLACK);

            typedArray.recycle();
        }

        setClickable(true);
        setFocusable(true);
    }

    public void check() {
        if (!checked) {
            checked = true;
        }
    }

    public void uncheck() {
        if (checked) {
            checked = false;
        }
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (checked)
            btn_paint.setColor(ColorUtils.blendARGB(unchecked_color, Color.BLACK, 0.3f));
        else
            btn_paint.setColor(unchecked_color);

        float width = getWidth();
        float height = getHeight();

        canvas.drawCircle(width/2, height/2, width/2, btn_paint);

        if (checked) {
            Drawable tick = getResources().getDrawable(R.drawable.icon_tick, null);
            tick.setBounds((int) width/6, (int) height/6, (int) (width - width/6), (int) (height - height/6));
            tick.draw(canvas);
        }
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }
}
