package com.project.rempaudioeditor;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.rempaudioeditor.customviews.FftAudioVisualizer;
import com.project.rempaudioeditor.customviews.WaveformSeekbar;
import com.project.rempaudioeditor.infos.AudioInfo;
import com.project.rempaudioeditor.utils.FileConverter;

import java.io.IOException;
import java.util.ArrayList;

public class AudioPlayerData {
    private static AudioPlayerData single_instance = null;

    private MediaPlayer audio_player;
    private FftAudioVisualizer fft_audio_visualizer_view;
    private Visualizer visualizer;

    private boolean released = true;

    private final ArrayList<AudioInfo> audio_tracks = new ArrayList<>();
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

    public void initializePlayer(@NonNull Context context,
                                 @NonNull FftAudioVisualizer fft_audio_visualizer_view,
                                 @NonNull WaveformSeekbar seekbar,
                                 @Nullable EditText current_duration_edit_view,
                                 @Nullable TextView total_duration_edit_view) {
        this.fft_audio_visualizer_view = fft_audio_visualizer_view;

        seekbar.connectMediaPlayer(context, current_duration_edit_view, total_duration_edit_view);
    }

    public void addTrack(AudioInfo newTrack) {
        audio_tracks.add(newTrack);
    }

    public void removeTrack(int index) {
        audio_tracks.remove(index);
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
        visualizer = new Visualizer(audio_player.getAudioSessionId());
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        visualizer.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);

        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                fft_audio_visualizer_view.setBars(FileConverter.formatFft(fft));
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
                audio_player = new MediaPlayer();
                audio_player.setDataSource(app_context, audioInfo.getUriFile());
                audio_player.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
                audio_player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateCurrentAudioTrackStartAndEnd();

            audio_player.start();

            released = false;

            initializeCircleVisualizer();

            audio_player.setOnCompletionListener(mp -> {
                current_audio_track_index++;
                if (current_audio_track_index < audio_tracks.size()) {
                    startPlayer(app_context);
                } else {
                    releasePlayer();
                    current_audio_track_index = 0;
                }
            });
        } else {
            releasePlayer();
            startPlayer(app_context);
        }
    }

    public void pausePlayer() {
        if ((!getReleased()) && (audio_player.isPlaying()))
            audio_player.pause();
    }

    public void resumePlayer() {
        if ((!getReleased()) && (!audio_player.isPlaying()))
            audio_player.start();
    }

    public void endPlayer() {
        releasePlayer();
        audio_tracks.clear();
    }

    public void releasePlayer() {
        if (!released) {
            releaseVisualizer();
            audio_player.release();
            audio_player = null;
            released = true;
        }
    }

    public void releaseVisualizer() {
        visualizer.setEnabled(false);
        visualizer.release();
    }

    public MediaPlayer getPlayer() {
        return audio_player;
    }

    public boolean getReleased() {
        if (audio_player == null) {
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

    public ArrayList<AudioInfo> getTrackList() {
        return audio_tracks;
    }
}
