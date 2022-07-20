package com.example.rempaudioeditor.values;

import static com.example.rempaudioeditor.enums.SettingId.ABOUT;
import static com.example.rempaudioeditor.enums.SettingId.THEME;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.rempaudioeditor.viewinfos.SettingsItemView;

import java.util.ArrayList;

public class SettingsItems {
    public static final String THEME_TEXT = "Theme";
    public static final String ABOUT_TEXT = "About";

    // To get a list of all settings
    public static ArrayList<SettingsItemView> getSettingsItemList() {
        final ArrayList<SettingsItemView> itemList = new ArrayList<>();

        // Theme
        String theme_txt;
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO)
            theme_txt = "Light";
        else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            theme_txt = "Dark";
        else
            theme_txt = "System Default";
        itemList.add(new SettingsItemView(THEME_TEXT, theme_txt, THEME));

        // About
        String app_version = "App version: " + AppConstants.app_version;
        itemList.add(new SettingsItemView(ABOUT_TEXT, app_version, ABOUT));

        return itemList;
    }
}
