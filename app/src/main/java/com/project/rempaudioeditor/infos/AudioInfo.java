package com.project.rempaudioeditor.infos;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.rempaudioeditor.utils.FileConverter;
import com.project.rempaudioeditor.customviews.WaveForm;


public class AudioInfo {
    private final Uri uriFile;
    private final int duration;


    public AudioInfo(@NonNull Context context,
                     @NonNull Uri uriFile) {
        this.uriFile = uriFile;

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, uriFile);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Integer.parseInt(durationStr);
    }

    public WaveForm generateWaveform(@NonNull Context context) {
        return FileConverter.createWaveForm(context, uriFile);
    }

    public Uri getUriFile() {
        return uriFile;
    }

    public int getUriDuration() {
        return duration;
    }
}
