package com.project.rempaudioeditor.database;

import android.content.Context;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.AppSettings;
import com.project.rempaudioeditor.enums.ColorId;
import com.project.rempaudioeditor.enums.ThemeId;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsJsonManager {
    public static final String SETTINGS_FILE_NAME = "app_settings.json";
    public static final String SETTINGS_THEME_OBJECT = "theme";
    public static final String SETTINGS_ACCENT_COLOR_OBJECT = "accent_color";
    public static final String SETTINGS_DEFAULT_AUDIO_STORAGE_OBJECT = "audio_store";
    public static final String SETTINGS_THEME_LIGHT = "light";
    public static final String SETTINGS_THEME_DARK = "dark";
    public static final String SETTINGS_THEME_SYSTEM_DEF = "def";
    public static final String SETTINGS_ACCENT_COLOR_RED = "red";
    public static final String SETTINGS_ACCENT_COLOR_BLUE = "blue";
    public static final String SETTINGS_ACCENT_COLOR_YELLOW = "yellow";
    public static final String SETTINGS_ACCENT_COLOR_GREEN = "green";
    public static final String SETTINGS_ACCENT_COLOR_CYAN = "cyan";
    public static final String SETTINGS_ACCENT_COLOR_PINK = "pink";
    public static final String SETTINGS_ACCENT_COLOR_WALLPAPER = "wallpaper";

    public static void write(@NonNull Context context,
                             @NonNull AppSettings settings) {
        // Theme
        ThemeId appThemeId = settings.getThemeId();
        String appThemeStr = null;
        switch (appThemeId) {
            case LIGHT:
                appThemeStr = SETTINGS_THEME_LIGHT;
                break;
            case DARK:
                appThemeStr = SETTINGS_THEME_DARK;
                break;
            case SYSTEM_DEF:
                appThemeStr = SETTINGS_THEME_SYSTEM_DEF;
                break;
        }

        // Accent Color
        ColorId appColorId = settings.getColorId();
        String appColorStr = null;
        switch (appColorId) {
            case RED:
                appColorStr = SETTINGS_ACCENT_COLOR_RED;
                break;
            case BLUE:
                appColorStr = SETTINGS_ACCENT_COLOR_BLUE;
                break;
            case PINK:
                appColorStr = SETTINGS_ACCENT_COLOR_PINK;
                break;
            case GREEN:
                appColorStr = SETTINGS_ACCENT_COLOR_GREEN;
                break;
            case CYAN:
                appColorStr = SETTINGS_ACCENT_COLOR_CYAN;
                break;
            case YELLOW:
                appColorStr = SETTINGS_ACCENT_COLOR_YELLOW;
                break;
            case WALLPAPER:
                appColorStr = SETTINGS_ACCENT_COLOR_WALLPAPER;
                break;
        }

        // Default audio storage
        String defaultAudioStorageDir = settings.getCurrentAudioStorageDir();

        JSONObject settings_json = new JSONObject();
        try {
            settings_json.put(SETTINGS_THEME_OBJECT, appThemeStr);
            settings_json.put(SETTINGS_ACCENT_COLOR_OBJECT, appColorStr);
            settings_json.put(SETTINGS_DEFAULT_AUDIO_STORAGE_OBJECT, defaultAudioStorageDir);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FileManager.writeStringFileToAppDirectory(context, SETTINGS_FILE_NAME, settings_json.toString());
    }

    public static void loadSettings(@NonNull Context context) {
        if (context.getFileStreamPath(SETTINGS_FILE_NAME).exists()) {
            ThemeId app_theme_id = null;
            ColorId app_color_id = null;
            String default_audio_storage_directory = null;
            try {
                JSONObject settings_json = new JSONObject(FileManager.readStringFileFromAppDirectory(context, SETTINGS_FILE_NAME));
                if (settings_json.has(SETTINGS_THEME_OBJECT)) {
                    switch (settings_json.getString(SETTINGS_THEME_OBJECT)) {
                        case SETTINGS_THEME_LIGHT:
                            app_theme_id = ThemeId.LIGHT;
                            break;
                        case SETTINGS_THEME_DARK:
                            app_theme_id = ThemeId.DARK;
                            break;
                        case SETTINGS_THEME_SYSTEM_DEF:
                            app_theme_id = ThemeId.SYSTEM_DEF;
                            break;
                    }
                }

                if (settings_json.has(SETTINGS_ACCENT_COLOR_OBJECT)) {
                    switch (settings_json.getString(SETTINGS_ACCENT_COLOR_OBJECT)) {
                        case SETTINGS_ACCENT_COLOR_RED:
                             app_color_id = ColorId.RED;
                            break;
                        case SETTINGS_ACCENT_COLOR_GREEN:
                             app_color_id = ColorId.GREEN;
                            break;
                        case SETTINGS_ACCENT_COLOR_BLUE:
                             app_color_id = ColorId.BLUE;
                            break;
                        case SETTINGS_ACCENT_COLOR_YELLOW:
                             app_color_id = ColorId.YELLOW;
                            break;
                        case SETTINGS_ACCENT_COLOR_CYAN:
                             app_color_id = ColorId.CYAN;
                            break;
                        case SETTINGS_ACCENT_COLOR_PINK:
                             app_color_id = ColorId.PINK;
                            break;
                        case SETTINGS_ACCENT_COLOR_WALLPAPER:
                             app_color_id = ColorId.WALLPAPER;
                            break;
                    }
                }

                if (settings_json.has(SETTINGS_DEFAULT_AUDIO_STORAGE_OBJECT)) {
                    default_audio_storage_directory = settings_json.getString(SETTINGS_DEFAULT_AUDIO_STORAGE_OBJECT);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            AppSettings settingsInfo = AppSettings.getInstance();
            settingsInfo.setTheme(app_theme_id);
            settingsInfo.setAccentColor(app_color_id);
            settingsInfo.setCurrentAudioStorageDir(default_audio_storage_directory);
        } else {
            loadDefaultSettings();
            write(context, AppSettings.getInstance());
        }
    }

    public static void loadDefaultSettings() {
        AppSettings.getInstance().setTheme(ThemeId.SYSTEM_DEF);
    }
}
