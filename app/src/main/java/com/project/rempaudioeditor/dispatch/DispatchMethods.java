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
                                        @NonNull Transition transition,
                                        @NonNull Boolean focusable) {
        PopupWindow popup_window = new PopupWindow(popup_view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                focusable);
        popup_window.setEnterTransition(transition);
        popup_window.setExitTransition(transition);
        popup_window.showAtLocation(popup_view, Gravity.CENTER, 0, 0);

        // Dim the background
        View container = popup_window.getContentView().getRootView();
        Context context = popup_window.getContentView().getContext();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) container.getLayoutParams();
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.3f;
        windowManager.updateViewLayout(container, layoutParams);

        return popup_window;
    }

    public static AlertDialog.Builder createDialog(@NonNull Context context,
                                                   @Nullable String dialog_header,
                                                   @Nullable View view) {
        AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(context, R.style.Theme_AlertDialogTheme);

        if (dialog_header != null)
            alert_dialog_builder.setTitle(dialog_header);

        if (view != null)
            alert_dialog_builder.setView(view);

        return alert_dialog_builder;
    }

    // Specifies why the app needs a permission and requests it
    public static void createPermissionRequiredDialog(@NonNull Context context,
                                                      @NonNull String dialog_header,
                                                      @NonNull String dialog_desc,
                                                      @NonNull String[] permissions,
                                                      int permission_request_code) {
        AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(context, R.style.Theme_AlertDialogTheme);
        alert_dialog_builder.setTitle(dialog_header);

        TextView desc_textview = new TextView(context);
        desc_textview.setText(dialog_desc);
        desc_textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        int alertDialogPadding = AttrDataRetriever
                .getDimensionFromAttr(context, com.google.android.material.R.attr.dialogPreferredPadding);
        desc_textview.setPadding(alertDialogPadding, alertDialogPadding, alertDialogPadding, alertDialogPadding);
        alert_dialog_builder.setView(desc_textview);

        alert_dialog_builder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
            ActivityCompat.requestPermissions((Activity) context, permissions, permission_request_code);
        });

        alert_dialog_builder.setNegativeButton(R.string.button_cancel, (dialog, id) -> {

        });

        alert_dialog_builder.show().setCanceledOnTouchOutside(false);
    }
}
