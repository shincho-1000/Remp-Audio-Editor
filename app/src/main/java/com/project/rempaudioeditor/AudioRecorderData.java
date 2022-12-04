package com.project.rempaudioeditor;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.constants.AppConstants;
import com.project.rempaudioeditor.customviews.RecorderVisualizerScroller;

import java.io.IOException;

public class AudioRecorderData {
    private static AudioRecorderData single_instance = null;

    private MediaRecorder media_recorder;
    private RecorderVisualizerScroller recorder_visualizer_wrapper;

    private boolean recording;

    private final Runnable runnable = this::updateVisualizer;
    private final Handler audio_rec_handler = new Handler();

    private AudioRecorderData() {

    }

    public static AudioRecorderData getInstance() {
        if (single_instance == null) {
            single_instance = new AudioRecorderData();
        }
        return single_instance;
    }

    public MediaRecorder getMediaRecorder() {
        return media_recorder;
    }

    public boolean ifRecording() {
        return recording;
    }

    public void startRec(@NonNull Context context,
                         @NonNull RecorderVisualizerScroller audioVisualizerScroller) {
        this.recorder_visualizer_wrapper = audioVisualizerScroller;

        if (media_recorder == null) {
            media_recorder = new MediaRecorder();
            media_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            media_recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            media_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            media_recorder.setOutputFile(AppConstants.getAppAudioRecordingFilePath(context));
            try {
                media_recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            media_recorder.start();

            recording = true;

            clearVisualizer();
            updateVisualizer();
        }
    }

    public void pauseRec() {
        media_recorder.pause();
        recording = false;
    }

    public void resumeRec() {
        media_recorder.resume();
        recording = true;

        updateVisualizer();
    }

    public void stopRecording() {
        if (media_recorder != null) {
            media_recorder.stop();
            media_recorder.release();
            media_recorder = null;
            recording = false;
        }
    }

    public void deleteRec() {
        clearVisualizer();
    }

    private void updateVisualizer() {
        if ((media_recorder != null) && (recording)) {
            int amp = media_recorder.getMaxAmplitude();
            recorder_visualizer_wrapper.addAmplitude(amp);
            audio_rec_handler.postDelayed(runnable, 50);
        }
    }

    private void clearVisualizer() {
        recorder_visualizer_wrapper.clearAmplitudes();
    }
}
