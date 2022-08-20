package com.project.rempaudioeditor.database;

import android.content.Context;

import com.project.rempaudioeditor.Settings;
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

    // Writes the json file from the settings info
    public static void writeSettingsJson(Context context, Settings settings) {

        // Theme
        ThemeId appThemeId = settings.getTheme();
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

    // Creates a settings info from the database
    public static void createSettingsInfo(Context context) {
        if (context.getFileStreamPath(SETTINGS_FILE_NAME).exists()) {
            ThemeId appThemeId = null;
            String defaultAudioStorageDir = null;
            try {
                JSONObject settings_json = new JSONObject(FileManager.readStringFileFromAppDirectory(context, SETTINGS_FILE_NAME));
                switch (settings_json.getString(SETTINGS_THEME_OBJECT)) {
                    case SETTINGS_THEME_LIGHT:
                        appThemeId = ThemeId.LIGHT;
                        break;
                    case SETTINGS_THEME_DARK:
                        appThemeId = ThemeId.DARK;
                        break;
                    case SETTINGS_THEME_SYSTEM_DEF:
                        appThemeId = ThemeId.SYSTEM_DEF;
                        break;
                }

                defaultAudioStorageDir = settings_json.getString(SETTINGS_DEFAULT_AUDIO_STORAGE_OBJECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Settings settingsInfo = Settings.getInstance();
            settingsInfo.setTheme(appThemeId);
            settingsInfo.setCurrentAudioStorageDir(defaultAudioStorageDir);
        } else {
            createDefaultSettingsInfo();
            writeSettingsJson(context, Settings.getInstance());
        }
    }

    public static void createDefaultSettingsInfo() {
        Settings.getInstance().setTheme(ThemeId.SYSTEM_DEF);
    }
}
