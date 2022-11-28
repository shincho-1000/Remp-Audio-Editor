package com.project.rempaudioeditor.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;

import com.project.rempaudioeditor.AppMethods;
import com.project.rempaudioeditor.AppSettings;
import com.project.rempaudioeditor.database.SettingsJsonManager;

public class SplashScreenActivity extends DefaultActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load settings
        SettingsJsonManager.loadSettings(getApplicationContext());

        switch (AppSettings.getInstance().getThemeId()) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        AppMethods.openActivity(this, MainActivity.class);
        AppMethods.finishActivity(this);
    }
}
