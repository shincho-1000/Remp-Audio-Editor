package com.project.rempaudioeditor.values;

import static com.project.rempaudioeditor.enums.SettingId.ABOUT;
import static com.project.rempaudioeditor.enums.SettingId.DEFAULT_AUDIO_STORAGE_DIR;
import static com.project.rempaudioeditor.enums.SettingId.THEME;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.project.rempaudioeditor.infos.SettingsItemView;

import java.util.ArrayList;

public class SettingsRecyclerItems {
    public static final String THEME_MAIN_TEXT = "Theme";
    public static final String DEFAULT_AUDIO_STORAGE_MAIN_TEXT = "Default Audio Storage Directory";
    public static final String ABOUT_MAIN_TEXT = "About";

    // To get a list of all settings
    public static ArrayList<SettingsItemView> getSettingsItemList() {
        final ArrayList<SettingsItemView> itemList = new ArrayList<>();

        // Theme
        String theme_desc;
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            theme_desc = "Light";
        } else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            theme_desc = "Dark";
        } else {
            theme_desc = "System Default";
        }
        itemList.add(new SettingsItemView(THEME_MAIN_TEXT, theme_desc, THEME));

        // Default audio storage dir
        String default_directory = AppConstants.getCurrentAudioStorageDir();
        itemList.add(new SettingsItemView(DEFAULT_AUDIO_STORAGE_MAIN_TEXT, default_directory, DEFAULT_AUDIO_STORAGE_DIR));

        // About
        String app_version = "App version: " + AppConstants.getAppVersion();
        itemList.add(new SettingsItemView(ABOUT_MAIN_TEXT, app_version, ABOUT));

        return itemList;
    }
}
