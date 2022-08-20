package com.project.rempaudioeditor.infos;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.converters.AudioConverters;
import com.project.rempaudioeditor.views.WaveForm;


public class AudioInfo {
    private final WaveForm waveForm;
    private final Uri uriFile;
    private final int duration;


    public AudioInfo(@NonNull Context context, @NonNull Uri uriFile) {
        this.uriFile = uriFile;
        waveForm = AudioConverters.createWaveForm(context, uriFile);

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, uriFile);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Integer.parseInt(durationStr);

    }

    public WaveForm getWaveForm() {
        return waveForm;
    }

    public Uri getUriFile() {
        return uriFile;
    }

    public int getUriDuration() {
        return duration;
    }
}
