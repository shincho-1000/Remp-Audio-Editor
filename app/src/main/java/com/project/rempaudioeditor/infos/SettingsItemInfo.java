package com.project.rempaudioeditor.infos;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.enums.SettingId;

public class SettingsItemInfo {
    private final String main_text_str; // The main text  of the setting
    private final String desc_text_str; // The description  of the setting
    private final SettingId setting_id; // Id of the setting

    public SettingsItemInfo(@NonNull String main_text,
                            String desc_text,
                            @NonNull SettingId setting_id) {
        main_text_str = main_text;
        desc_text_str = desc_text;
        this.setting_id = setting_id;
    }

    public String getMainText() {
        return main_text_str;
    }

    public String getDescText() {
        return desc_text_str;
    }

    public SettingId getId() {
        return setting_id;
    }
}
