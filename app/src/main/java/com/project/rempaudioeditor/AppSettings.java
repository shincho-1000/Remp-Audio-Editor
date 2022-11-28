package com.project.rempaudioeditor;

import com.project.rempaudioeditor.enums.ThemeId;

public class AppSettings {
    private static AppSettings single_instance = null;
    private ThemeId appThemeId;
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
    public ThemeId getThemeId() {
        return appThemeId;
    }

    public String getCurrentAudioStorageDir() {
        return current_audio_storage_dir;
    }

    // Setters
    public void setTheme(ThemeId appThemeId) {
        this.appThemeId = appThemeId;
    }

    public void setCurrentAudioStorageDir(String current_audio_storage_dir) {
        this.current_audio_storage_dir = current_audio_storage_dir;
    }
}
