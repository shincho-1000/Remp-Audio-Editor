package com.project.rempaudioeditor.customviews.waveformshadowbuilder;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.project.rempaudioeditor.utils.UnitConverter;

public class WaveformShadowBuilder extends View.DragShadowBuilder {
    private Drawable shadow_drawable;
    private Drawable icon_drawable;
    private final int SHADOW_HEIGHT = (int) UnitConverter.convertDpToPx(getView().getContext(), 40);
    private final int ICON_DIMEN = (int) UnitConverter.convertDpToPx(getView().getContext(), 20);
    private final int SHADOW_WIDTH = (int) UnitConverter.convertDpToPx(getView().getContext(), 150);

    public WaveformShadowBuilder(View view) {
        super(view);
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point touchPoint) {
        shadowSize.set(SHADOW_WIDTH, SHADOW_HEIGHT);
        touchPoint.set(SHADOW_WIDTH/2, SHADOW_HEIGHT/2);
    }

    public void setShadowDrawable(Drawable shadow_drawable) {
        this.shadow_drawable = shadow_drawable;
    }

    public void setIconDrawable(Drawable icon_drawable) {
        this.icon_drawable = icon_drawable;
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        shadow_drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        shadow_drawable.draw(canvas);

        if (icon_drawable != null) {
            icon_drawable.setBounds((canvas.getWidth() - ICON_DIMEN) / 2, (canvas.getHeight() - ICON_DIMEN) / 2, canvas.getWidth() - ((canvas.getWidth() - ICON_DIMEN) / 2), canvas.getHeight() - ((canvas.getHeight() - ICON_DIMEN) / 2));
            icon_drawable.draw(canvas);
        }
    }
}
