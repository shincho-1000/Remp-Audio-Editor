package com.project.rempaudioeditor.infos;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.AudioPlayerData;

import java.io.IOException;
import java.util.ArrayList;

public class ChannelInfo {
    private MediaPlayer audio_player;

    private boolean released = true;

    private final ArrayList<AudioInfo> audio_tracks = new ArrayList<>();
    private int current_audio_track_index = 0;
    private int current_track_start = 0;
    private int current_track_end = 0;

    private OnPlayerCompletionListener player_completed_listener;

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

    public void startPlayer(@NonNull Context app_context) {
        if (audio_tracks.size() == 0)
            return;

        AudioInfo audioInfo = audio_tracks.get(current_audio_track_index);

        if (audioInfo.getUriFile() == null)
            return;

        if (getReleased()) {
            try {
                audio_player = new MediaPlayer();
                if (AudioPlayerData.getInstance().getAudioSessionId() != -1) {
                    audio_player.setAudioSessionId(AudioPlayerData.getInstance().getAudioSessionId());
                }
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

            audio_player.setOnCompletionListener(mp -> {
                current_audio_track_index++;
                if (current_audio_track_index < audio_tracks.size()) {
                    startPlayer(app_context);
                } else {
                    releasePlayer();
                    if (player_completed_listener != null)
                        player_completed_listener.playerCompleted();
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
            audio_player.release();
            audio_player = null;
            released = true;
        }
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


    public void setPlayerCompletionListener(@NonNull OnPlayerCompletionListener event_listener) {
        player_completed_listener = event_listener;
    }

    public interface OnPlayerCompletionListener {
        void playerCompleted();
    }
}
