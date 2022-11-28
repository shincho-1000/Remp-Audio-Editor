package com.project.rempaudioeditor.infos;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.utils.FileConverter;
import com.project.rempaudioeditor.customviews.WaveForm;


public class AudioInfo {
    private OnWaveFormCreatedListener waveform_created_listener;
    private WaveForm waveForm;
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

    public void generateWaveform(@NonNull Context context) {
        waveForm = FileConverter.createWaveForm(context, uriFile);

        if (waveform_created_listener != null)
            waveform_created_listener.waveformCreated();
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


    public void setWaveFormCreatedListener(OnWaveFormCreatedListener event_listener) {
        waveform_created_listener = event_listener;
    }

    public interface OnWaveFormCreatedListener {
        void waveformCreated();
    }
}
