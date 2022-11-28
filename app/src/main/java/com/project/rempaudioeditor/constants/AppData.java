package com.project.rempaudioeditor.constants;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.AppSettings;

import java.io.File;

public class AppData {
    private static final String APP_VERSION = "0.0";
    private static final String AUDIO_RECORDING_FILE_NAME = "audioRecord.3gp";
    private static final String APP_EXTERNAL_STORAGE_FOLDER_NAME = "RempAudioEditor";
    private static final String APP_AUDIO_FOLDER_NAME = "RecordedAudio";

    public static String getAppVersion() {
        return APP_VERSION;
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
