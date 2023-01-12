package com.project.rempaudioeditor.constants;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.AppSettings;

import java.io.File;
import java.util.ArrayList;

public class AppConstants {
    private static final String APP_VERSION = "0.0";
    private static final int POPUP_SEND_DELAY_MILISEC = 100;
    private static final int SET_THEME_DELAY_MILISEC = 500;
    private static final String AUDIO_RECORDING_FILE_NAME = "audioRecord.3gp";
    private static final String APP_EXTERNAL_STORAGE_FOLDER_NAME = "RempAudioEditor";
    private static final String APP_AUDIO_FOLDER_NAME = "RecordedAudio";
    public static final int COLOR_RED = Color.RED;
    public static final int COLOR_BLUE = Color.BLUE;
    public static final int COLOR_GREEN = Color.GREEN;
    public static final int COLOR_YELLOW = Color.YELLOW;
    public static final int COLOR_PINK = Color.MAGENTA;
    public static final int COLOR_CYAN = Color.CYAN;

    public static String getAppVersion() {
        return APP_VERSION;
    }

    public static int getPopupSendDelayMilisec() {
        return POPUP_SEND_DELAY_MILISEC;
    }

    public static ArrayList<Integer> getAccentColors() {
        ArrayList<Integer> accent_colors = new ArrayList<>();
        accent_colors.add(COLOR_BLUE);
        accent_colors.add(COLOR_RED);
        accent_colors.add(COLOR_YELLOW);
        accent_colors.add(COLOR_GREEN);
        accent_colors.add(COLOR_PINK);
        accent_colors.add(COLOR_CYAN);
        return accent_colors;
    }

    public static int getSetThemeDelayMilisec() {
        return SET_THEME_DELAY_MILISEC;
    }

    public static File getAndroidExternalStorageDir() {
        return Environment.getExternalStorageDirectory();
    }

    public static File getAppExternalStorageDir() {
        File android_external_storage = getAndroidExternalStorageDir();
        File default_app_storage = new File(android_external_storage + "/" + APP_EXTERNAL_STORAGE_FOLDER_NAME);
        if (!default_app_storage.exists()) {
            default_app_storage.mkdir();
        }
        return default_app_storage;
    }

    public static File getDefaultAudioStorageDir() {
        File app_external_storage = getAppExternalStorageDir();
        File default_app_audio_storage = new File(app_external_storage + "/" + APP_AUDIO_FOLDER_NAME);
        if (!default_app_audio_storage.exists()) {
            default_app_audio_storage.mkdir();
        }
        return default_app_audio_storage;
    }

    public static String getCurrentAudioStorageDir() {
        String currentAudioStorageDirectory = AppSettings.getInstance().getCurrentAudioStorageDir();
        if (!((currentAudioStorageDirectory == null) || (currentAudioStorageDirectory.isEmpty())))
            return currentAudioStorageDirectory;
        else
            return getDefaultAudioStorageDir().getAbsolutePath();
    }

    public static String getAppAudioRecordingFilePath(@NonNull Context context) {
        return context.getExternalCacheDir().getAbsolutePath() + AUDIO_RECORDING_FILE_NAME;
    }
}
