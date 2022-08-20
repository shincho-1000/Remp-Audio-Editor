package com.project.rempaudioeditor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;

import com.project.rempaudioeditor.Settings;
import com.project.rempaudioeditor.database.SettingsJsonManager;

public class SplashScreenActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Creating the activity and finishing it after main activity loads
        super.onCreate(savedInstanceState);

        // Load settings
        SettingsJsonManager.createSettingsInfo(getApplicationContext());

        switch (Settings.getInstance().getTheme()) {
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

        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        finish();
    }
}
