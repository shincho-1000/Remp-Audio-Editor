package com.project.rempaudioeditor;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.project.rempaudioeditor.enums.ThemeId;

public class AppMethods {
    public static void setAppTheme(@NonNull ThemeId theme_id) {
        switch (theme_id) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SYSTEM_DEF:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void openActivity(@NonNull Context context,
                                    @NonNull Class<?> activity_class) {
        Intent activity_intent = new Intent(context, activity_class);
        context.startActivity(activity_intent);
    }

    public static void finishActivity(@NonNull Activity activity) {
        activity.finish();
    }

    public static void reloadActivity(@NonNull Activity activity) {
        activity.finish();
        activity.startActivity(activity.getIntent());
    }

    public static PendingIntent makePendingIntent(@NonNull Context context,
                                           @NonNull String name,
                                           @NonNull BroadcastReceiver receiver) {
        Intent intent = new Intent(context, receiver.getClass());
        intent.setAction(name);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

}
