package com.project.rempaudioeditor.dispatch;

import android.app.Activity;
import android.content.Context;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.utils.AttrDataRetriever;

public class DispatchMethods {

    public static PopupWindow sendPopup(@NonNull View popup_view,
                                        @NonNull Transition transition) {
        PopupWindow popupWindow = new PopupWindow(popup_view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setEnterTransition(transition);
        popupWindow.setExitTransition(transition);
        popupWindow.showAtLocation(popup_view, Gravity.CENTER, 0, 0);

        // Dim the background
        View container = popupWindow.getContentView().getRootView();
        Context context = popupWindow.getContentView().getContext();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) container.getLayoutParams();
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.3f;
        windowManager.updateViewLayout(container, layoutParams);

        return popupWindow;
    }

    public static AlertDialog.Builder createDialog(@NonNull Context context,
                                                   @Nullable String dialog_header,
                                                   @Nullable View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AlertDialogTheme);

        if (dialog_header != null)
            builder.setTitle(dialog_header);

        if (view != null)
            builder.setView(view);

        return builder;
    }

    // Specifies why why the app needs a permission and requests it
    public static void createPermissionRequiredDialog(@NonNull Context context,
                                                      @NonNull String dialog_header,
                                                      @NonNull String dialog_desc,
                                                      @NonNull String[] permissions,
                                                      int permission_request_code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AlertDialogTheme);
        builder.setTitle(dialog_header);

        TextView desc_textview = new TextView(context);
        desc_textview.setText(dialog_desc);
        desc_textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        int alertDialogPadding = AttrDataRetriever
                .getDimensionFromAttr(context, com.google.android.material.R.attr.dialogPreferredPadding);
        desc_textview.setPadding(alertDialogPadding, alertDialogPadding, alertDialogPadding, alertDialogPadding);
        builder.setView(desc_textview);

        builder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
            ActivityCompat.requestPermissions((Activity) context, permissions, permission_request_code);
        });

        builder.setNegativeButton(R.string.button_cancel, (dialog, id) -> {

        });

        builder.show().setCanceledOnTouchOutside(false);
    }
}
