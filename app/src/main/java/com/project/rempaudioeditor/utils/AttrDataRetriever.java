package com.project.rempaudioeditor.utils;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.NonNull;

public class AttrDataRetriever {
    public static int getDimensionFromAttr(@NonNull Context context, int attr) {
         TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, typedValue, true))
            return TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
        else
            return 0;
    }
}
