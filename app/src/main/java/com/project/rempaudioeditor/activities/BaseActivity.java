package com.project.rempaudioeditor.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AppSettings;
import com.project.rempaudioeditor.enums.ColorId;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ColorId color_id = AppSettings.getInstance().getColorId();
        if (color_id != null) {
            if (color_id == ColorId.WALLPAPER) {
                color_id = AppSettings.getInstance().getWallpaperDominantColorId();
            }
            switch (color_id) {
                case RED:
                    setTheme(R.style.Theme_RempAudioEditor_Red);
                    break;
                case GREEN:
                    setTheme(R.style.Theme_RempAudioEditor_Green);
                    break;
                case BLUE:
                    setTheme(R.style.Theme_RempAudioEditor_Blue);
                    break;
                case YELLOW:
                    setTheme(R.style.Theme_RempAudioEditor_Yellow);
                    break;
                case PINK:
                    setTheme(R.style.Theme_RempAudioEditor_Pink);
                    break;
                case CYAN:
                    setTheme(R.style.Theme_RempAudioEditor_Cyan);
                    break;
            }
        }
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}
