package com.project.rempaudioeditor.database;

import android.content.Context;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.AppSettings;
import com.project.rempaudioeditor.enums.ThemeId;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsJsonManager {
    public static final String SETTINGS_FILE_NAME = "app_settings.json";
    public static final String SETTINGS_THEME_OBJECT = "theme";
    public static final String SETTINGS_DEFAULT_AUDIO_STORAGE_OBJECT = "audio_store";
    public static final String SETTINGS_THEME_LIGHT = "light";
    public static final String SETTINGS_THEME_DARK = "dark";
    public static final String SETTINGS_THEME_SYSTEM_DEF = "def";

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

        // Default audio storage
        String defaultAudioStorageDir = settings.getCurrentAudioStorageDir();

        JSONObject settings_json = new JSONObject();
        try {
            settings_json.put(SETTINGS_THEME_OBJECT, appThemeStr);
            settings_json.put(SETTINGS_DEFAULT_AUDIO_STORAGE_OBJECT, defaultAudioStorageDir);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FileManager.writeStringFileToAppDirectory(context, SETTINGS_FILE_NAME, settings_json.toString());
    }

    public static void loadSettings(@NonNull Context context) {
        if (context.getFileStreamPath(SETTINGS_FILE_NAME).exists()) {
            ThemeId app_theme_id = null;
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

                if (settings_json.has(SETTINGS_DEFAULT_AUDIO_STORAGE_OBJECT)) {
                    default_audio_storage_directory = settings_json.getString(SETTINGS_DEFAULT_AUDIO_STORAGE_OBJECT);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            AppSettings settingsInfo = AppSettings.getInstance();
            settingsInfo.setTheme(app_theme_id);
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
