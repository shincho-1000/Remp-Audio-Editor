package com.example.rempaudioeditor.dispatch;

import android.content.Context;
import android.transition.Transition;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class DispatchPopup {

    // To send the popup at the center of the window
    public static void sendPopupAtCenter(View popup_view, Transition transition) {
        // Create a popup and show
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

}
