package com.project.rempaudioeditor.constants;

import static com.project.rempaudioeditor.enums.EditorTrayId.DELETE;
import static com.project.rempaudioeditor.enums.EditorTrayId.MUTE;
import static com.project.rempaudioeditor.enums.EditorTrayId.RECORD;
import static com.project.rempaudioeditor.enums.EditorTrayId.SPLIT;
import static com.project.rempaudioeditor.enums.EditorTrayId.TRIM;
import static com.project.rempaudioeditor.enums.EditorTrayId.VOLUME;
import static com.project.rempaudioeditor.enums.SettingId.ABOUT;
import static com.project.rempaudioeditor.enums.SettingId.DEFAULT_AUDIO_STORAGE_DIR;
import static com.project.rempaudioeditor.enums.SettingId.THEME;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.infos.EditorTrayItemInfo;
import com.project.rempaudioeditor.infos.SettingsItemInfo;

import java.util.ArrayList;

public class RecyclerViewItems {
    public static final String SETTING_THEME_MAIN_TEXT = "Theme";
    public static final String SETTING_DEFAULT_AUDIO_STORAGE_MAIN_TEXT = "Default Audio Storage Directory";
    public static final String SETTING_ABOUT_MAIN_TEXT = "About";

    public static ArrayList<SettingsItemInfo> getSettingsItemList() {
        final ArrayList<SettingsItemInfo> itemList = new ArrayList<>();

        // Theme
        String theme_desc;
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            theme_desc = "Light";
        } else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            theme_desc = "Dark";
        } else {
            theme_desc = "System Default";
        }
        itemList.add(new SettingsItemInfo(SETTING_THEME_MAIN_TEXT, theme_desc, THEME));

        // Default audio storage dir
        String default_directory = AppConstants.getCurrentAudioStorageDir();
        itemList.add(new SettingsItemInfo(SETTING_DEFAULT_AUDIO_STORAGE_MAIN_TEXT, default_directory, DEFAULT_AUDIO_STORAGE_DIR));

        // About
        String app_version = "App version: " + AppConstants.getAppVersion();
        itemList.add(new SettingsItemInfo(SETTING_ABOUT_MAIN_TEXT, app_version, ABOUT));

        return itemList;
    }

    public static ArrayList<EditorTrayItemInfo> getEditorTrayItemList() {
        final ArrayList<EditorTrayItemInfo> itemList = new ArrayList<>();

        itemList.add(new EditorTrayItemInfo("Volume", R.drawable.icon_volume, VOLUME));
        itemList.add(new EditorTrayItemInfo("Mute", R.drawable.icon_mute, MUTE));
        itemList.add(new EditorTrayItemInfo("Trim", R.drawable.icon_trim, TRIM));
        itemList.add(new EditorTrayItemInfo("Record", R.drawable.icon_audio, RECORD));
        itemList.add(new EditorTrayItemInfo("Delete", R.drawable.icon_delete_editor, DELETE));
        itemList.add(new EditorTrayItemInfo("Split", null, SPLIT));

        return itemList;
    }
}
