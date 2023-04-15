package com.project.rempaudioeditor;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.rempaudioeditor.customviews.FftAudioVisualizer;
import com.project.rempaudioeditor.customviews.WaveformSeekbar;
import com.project.rempaudioeditor.infos.AudioInfo;
import com.project.rempaudioeditor.infos.ChannelInfo;
import com.project.rempaudioeditor.utils.FileConverter;

import java.util.ArrayList;

public class AudioPlayerData {
    private static AudioPlayerData single_instance = null;

    private FftAudioVisualizer fft_audio_visualizer_view;
    private Visualizer visualizer;

    private int audio_session_id = -1;
    private boolean initialized = false;

    private final ArrayList<ChannelInfo> audio_channels = new ArrayList<>();

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

    public void addTrack(int channel_index, int track_index, AudioInfo newTrack) {
        if (audio_channels.size() <= channel_index) {
            audio_channels.add(channel_index, new ChannelInfo());
        }
        if (track_index < 0)
            audio_channels.get(channel_index).addTrack(-1, newTrack);
        else
            audio_channels.get(channel_index).addTrack(track_index, newTrack);
    }

    public void moveTrack(int original_track_channel_index, int original_track_index, int new_track_channel_index , int new_track_index) {
        if (!((original_track_channel_index == new_track_channel_index) && (original_track_index == new_track_index))) {
            AudioInfo track = audio_channels.get(original_track_channel_index).getTrackList().get(original_track_index);
            addTrack(new_track_channel_index, new_track_index, track);
            removeTrack(original_track_channel_index, original_track_index);
        }
    }

    public void removeTrack(int channel_index, int track_index) {
        if (audio_channels.get(channel_index).getCurrentAudioTrackIndex() == track_index) {
            audio_channels.get(channel_index).releasePlayer();
        }

        audio_channels.get(channel_index).removeTrack(track_index);

        if (audio_channels.get(channel_index).getTrackList().size() == 0) {
            audio_channels.remove(channel_index);
        }
    }

    public void initializeCircleVisualizer() {
        visualizer = new Visualizer(audio_session_id);
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

    public void startPlayers(@NonNull Context app_context) {
        for (ChannelInfo channelInfo : audio_channels) {
            channelInfo.startPlayer(app_context);

            audio_session_id = channelInfo.getPlayer().getAudioSessionId();
        }
        initializeCircleVisualizer();
        initialized = true;
    }

    public int getAudioSessionId() {
        return audio_session_id;
    }

    public void pausePlayers() {
        for (ChannelInfo channelInfo : audio_channels) {
            MediaPlayer audio_player = channelInfo.getPlayer();
            if ((!channelInfo.getReleased()) && (audio_player.isPlaying()))
                audio_player.pause();
        }
    }

    public void resumePlayers() {
        for (ChannelInfo channelInfo : audio_channels) {
            MediaPlayer audio_player = channelInfo.getPlayer();
            if ((!channelInfo.getReleased()) && (!audio_player.isPlaying()))
                audio_player.start();
        }
    }

    public void endPlayers() {
        for (ChannelInfo channelInfo : audio_channels) {
            channelInfo.endPlayer();
        }
        releaseVisualizer();
        initialized = false;
    }

    public void releasePlayers() {
        for (ChannelInfo channelInfo : audio_channels) {
            channelInfo.releasePlayer();
        }
    }

    public void releaseVisualizer() {
        if (initialized && visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
        }
    }

    public int getPlayerTotalDuration() {
        int player_total_duration = 0;

        for (int i = 0; i < audio_channels.size(); i++) {
            ChannelInfo channelInfo = audio_channels.get(i);
            if (channelInfo.getPlayerTotalDuration() > player_total_duration) {
                player_total_duration = channelInfo.getPlayerTotalDuration();
            }
        }

        return player_total_duration;
    }

    public int noOfTracks() {
        int no_of_tracks = 0;

        for (int i = 0; i < audio_channels.size(); i++) {
            ChannelInfo channelInfo = audio_channels.get(i);
            no_of_tracks += channelInfo.getTrackList().size();
        }

        return no_of_tracks;
    }

    public ArrayList<ChannelInfo> getChannelList() {
        return audio_channels;
    }

    public int getLongestChannelIndex() {
        int player_total_duration = 0;
        int longest_channel_index = -1;

        for (int i = 0; i < audio_channels.size(); i++) {
            ChannelInfo channelInfo = audio_channels.get(i);
            if (channelInfo.getPlayerTotalDuration() > player_total_duration) {
                player_total_duration = channelInfo.getPlayerTotalDuration();
                longest_channel_index = i;
            }
        }

        return longest_channel_index;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
