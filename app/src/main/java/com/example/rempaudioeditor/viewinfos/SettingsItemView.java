package com.example.rempaudioeditor.viewinfos;

import com.example.rempaudioeditor.enums.SettingId;

public class SettingsItemView {
    private final String main_text_str; // The main text  of the item
    private final String desc_text_str; // The description  of the item (Optional)
    private final SettingId setting_id; // Id of the setting

    public SettingsItemView(String main_text, String desc_text, SettingId setting_id) {
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
