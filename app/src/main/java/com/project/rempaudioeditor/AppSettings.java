package com.project.rempaudioeditor;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.palette.graphics.Palette;

import com.project.rempaudioeditor.constants.AppConstants;
import com.project.rempaudioeditor.enums.ColorId;
import com.project.rempaudioeditor.enums.ThemeId;

import java.util.ArrayList;

public class AppSettings {
    private static AppSettings single_instance = null;
    private ThemeId appThemeId;
    private ColorId appColorId;
    private ColorId wallpaperDominantColorId;
    private String current_audio_storage_dir;

    private AppSettings() {
    }

    public static AppSettings getInstance() {
        if (single_instance == null) {
            single_instance = new AppSettings();
        }
        return single_instance;
    }

    // Getters
    public ColorId getWallpaperDominantColorId() {
        return wallpaperDominantColorId;
    }

    public ThemeId getThemeId() {
        return appThemeId;
    }

    public ColorId getColorId() {
        return appColorId;
    }

    public String getCurrentAudioStorageDir() {
        return current_audio_storage_dir;
    }

    // Setters
    public void setWallpaperDominantColor(@NonNull Context context) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            wallpaperDominantColorId = ColorId.BLUE;
            return;
        }
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();

        Palette palette = Palette.from(bitmap).generate();

        int dominant_color = palette.getDominantColor(0x000000);

        ArrayList<Integer> colorArrayList = AppConstants.getAccentColors();
        double color_distance = colorDistance(dominant_color, colorArrayList.get(0));
        int set_color_index = 0;

        for (int i = 1; i < colorArrayList.size(); i++) {
            double new_color_distance = colorDistance(dominant_color, colorArrayList.get(i));
            if (new_color_distance < color_distance) {
                color_distance = new_color_distance;
                set_color_index = i;
            }
        }

        switch (colorArrayList.get(set_color_index)) {
            case AppConstants.COLOR_RED:
                wallpaperDominantColorId = ColorId.RED;
                break;
            case AppConstants.COLOR_BLUE:
                wallpaperDominantColorId = ColorId.BLUE;
                break;
            case AppConstants.COLOR_GREEN:
                wallpaperDominantColorId = ColorId.GREEN;
                break;
            case AppConstants.COLOR_YELLOW:
                wallpaperDominantColorId = ColorId.YELLOW;
                break;
            case AppConstants.COLOR_CYAN:
                wallpaperDominantColorId = ColorId.CYAN;
                break;
            case AppConstants.COLOR_PINK:
                wallpaperDominantColorId = ColorId.PINK;
                break;
        }
    }

    double colorDistance(int c1, int c2)
    {
        int red1 = Color.red(c1);
        int red2 = Color.red(c2);
        int rmean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = Color.green(c1) - Color.green(c2);
        int b = Color.blue(c1) - Color.blue(c2);
        return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
    }

    public void setTheme(ThemeId appThemeId) {
        this.appThemeId = appThemeId;
    }

    public void setAccentColor(ColorId appColorId) {
        this.appColorId = appColorId;
    }

    public void setCurrentAudioStorageDir(String current_audio_storage_dir) {
        this.current_audio_storage_dir = current_audio_storage_dir;
    }
}
