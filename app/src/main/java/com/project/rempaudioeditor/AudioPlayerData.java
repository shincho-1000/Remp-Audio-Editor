package com.project.rempaudioeditor;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.rempaudioeditor.converters.AudioConverters;
import com.project.rempaudioeditor.infos.AudioInfo;
import com.project.rempaudioeditor.views.FftAudioVisualizer;
import com.project.rempaudioeditor.views.WaveformSeekbar;

import java.io.IOException;
import java.util.ArrayList;

public class AudioPlayerData {
    private static AudioPlayerData single_instance = null;

    private MediaPlayer mPlayer;
    private FftAudioVisualizer fftAudioVisualizerView;
    private Visualizer visualizer;

    private boolean released = true;

    private ArrayList<AudioInfo> audio_tracks = new ArrayList<>();
    private int current_audio_track_index = 0;
    private int current_track_start = 0;
    private int current_track_end = 0;

    private AudioPlayerData() {

    }

    public static AudioPlayerData getInstance() {
        if (single_instance == null) {
            single_instance = new AudioPlayerData();
        }
        return single_instance;
    }

    public void initializePlayer(@NonNull ArrayList<AudioInfo> audio_tracks, @NonNull FftAudioVisualizer fftAudioVisualizerView, @NonNull WaveformSeekbar seekbar, @Nullable EditText duration_EditView) {
        this.audio_tracks = audio_tracks;
        this.fftAudioVisualizerView = fftAudioVisualizerView;

        seekbar.connectMediaPlayer(AudioPlayerData.getInstance(), audio_tracks, duration_EditView);
    }

    public void setCurrentAudioTrackIndex(int current_audio_track_index) {
        this.current_audio_track_index = current_audio_track_index;
    }

    public void updateCurrentAudioTrackStartAndEnd() {
        current_track_start = 0;
        current_track_end = 0;
        for (int i = 0; i <= getCurrentAudioTrackIndex(); i++) {
            AudioInfo currentTrackInfo = audio_tracks.get(i);
            current_track_end += currentTrackInfo.getUriDuration();
            if (i > 0) {
                AudioInfo previousTrackInfo = audio_tracks.get(i-1);
                current_track_start += previousTrackInfo.getUriDuration();
            }
        }
    }

    public void initializeCircleVisualizer() {
        visualizer = new Visualizer(mPlayer.getAudioSessionId());
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        visualizer.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);

        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                fftAudioVisualizerView.setBars(AudioConverters.makeFftFrom(fft));
            }
        }, Visualizer.getMaxCaptureRate(), false, true);

        visualizer.setEnabled(true);
    }

    public void startPlayer(@NonNull Context app_context) {
        if (audio_tracks.size() == 0)
            return;

        AudioInfo audioInfo = audio_tracks.get(current_audio_track_index);

        if (audioInfo.getUriFile() == null)
            return;

        if (getReleased()) {
            try {
                mPlayer = new MediaPlayer();
                mPlayer.setDataSource(app_context, audioInfo.getUriFile());
                mPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateCurrentAudioTrackStartAndEnd();

            mPlayer.start();

            released = false;

            initializeCircleVisualizer();

            mPlayer.setOnCompletionListener(mp -> {
                current_audio_track_index++;
                if (current_audio_track_index < audio_tracks.size()) {
                    startPlayer(app_context);
                } else {
                    stopPlayer();
                    current_audio_track_index = 0;
                }
            });
        } else {
            stopPlayer();
            startPlayer(app_context);
        }
    }

    public void pausePlayer() {
        if ((!getReleased()) && (mPlayer.isPlaying()))
            mPlayer.pause();
    }

    public void resumePlayer() {
        if ((!getReleased()) && (!mPlayer.isPlaying()))
            mPlayer.start();
    }

    public void stopPlayer() {
        if (!released) {
            releaseVisualizer();
            releasePlayer();
            released = true;
        }
    }

    public void releasePlayer() {
        mPlayer.release();
        mPlayer = null;
    }

    public void releaseVisualizer() {
        visualizer.setEnabled(false);
        visualizer.release();
    }

    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    public boolean getReleased() {
        if (mPlayer == null) {
            released = true;
        }

        return released;
    }

    public int getPlayerTotalDuration() {
        int player_total_duration = 0;

        for (AudioInfo audio_track : audio_tracks) {
            player_total_duration += audio_track.getUriDuration();
        }

        return player_total_duration;
    }

    public int getCurrentAudioTrackIndex() {
        return current_audio_track_index;
    }

    public int getCurrentAudioTrackStart() {
        return current_track_start;
    }

    public int getCurrentAudioTrackEnd() {
        return current_track_end;
    }
}
