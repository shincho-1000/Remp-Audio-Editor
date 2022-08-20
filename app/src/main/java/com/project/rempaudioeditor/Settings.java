package com.project.rempaudioeditor;

import android.util.Log;

import com.project.rempaudioeditor.enums.ThemeId;

public class Settings {
    private static Settings single_instance = null;
    private ThemeId theme;
    private String current_audio_storage_dir;

    private Settings() {

    }

    public static Settings getInstance() {
        if (single_instance == null) {
            single_instance = new Settings();
        }
        return single_instance;
    }

    // Getters
    public ThemeId getTheme() {
        return theme;
    }

    public String getCurrentAudioStorageDir() {
        return current_audio_storage_dir;
    }

    // Setters
    public void setTheme(ThemeId theme) {
        this.theme = theme;
    }

    public void setCurrentAudioStorageDir(String current_audio_storage_dir) {
        this.current_audio_storage_dir = current_audio_storage_dir;
    }
}
