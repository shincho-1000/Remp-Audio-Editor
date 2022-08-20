package com.project.rempaudioeditor.dispatch;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.transition.Transition;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.rempaudioeditor.R;

public class DispatchPopup {

    public static void sendPopupAtCenter(@NonNull View popup_view, @NonNull Transition transition) {
        PopupWindow popupWindow = new PopupWindow(popup_view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
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
    }

    public static AlertDialog.Builder createDialog(@NonNull Context context, @NonNull String dialog_header, @NonNull View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AlertDialogTheme);
        builder.setTitle(dialog_header);

        builder.setView(view);

        return builder;
    }
}
