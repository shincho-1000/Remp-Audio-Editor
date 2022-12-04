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
    private final Uri uri_file;
    private final int duration;


    public AudioInfo(@NonNull Context context,
                     @NonNull Uri uri_file) {
        this.uri_file = uri_file;

        MediaMetadataRetriever metadata_retriever = new MediaMetadataRetriever();
        metadata_retriever.setDataSource(context, AudioInfo.this.uri_file);
        String duration = metadata_retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        this.duration = Integer.parseInt(duration);
    }

    public WaveForm generateWaveform(@NonNull Context context) {
        return FileConverter.createWaveForm(context, uri_file);
    }

    public Uri getUriFile() {
        return uri_file;
    }

    public int getUriDuration() {
        return duration;
    }
}
