package com.project.rempaudioeditor;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.project.rempaudioeditor.values.AppConstants;
import com.project.rempaudioeditor.views.RecorderVisualizerScroller;

import java.io.IOException;

public class AudioRecorderData {
    private static AudioRecorderData single_instance = null;

    private MediaRecorder mediaRecorder;
    private boolean recording;
    private RecorderVisualizerScroller audioVisualizerScroller;

    private final Runnable runnable = this::updateVisualizer;
    private final Handler audioRecHandler = new Handler();

    private AudioRecorderData() {

    }

    public static AudioRecorderData getInstance() {
        if (single_instance == null) {
            single_instance = new AudioRecorderData();
        }
        return single_instance;
    }

    public MediaRecorder getMediaRecorder() {
        return mediaRecorder;
    }

    public boolean ifRecording() {
        return recording;
    }

    public void startRec(@NonNull Context context, @NonNull RecorderVisualizerScroller audioVisualizerScroller) {
        this.audioVisualizerScroller = audioVisualizerScroller;

        if (mediaRecorder == null) {

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(AppConstants.getAppAudioRecordingFilePath(context));
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaRecorder.start();

            recording = true;

            clearVisualizer();
            updateVisualizer();
        }
    }

    public void pauseRec() {
        mediaRecorder.pause();
        recording = false;
    }

    public void resumeRec() {
        mediaRecorder.resume();
        recording = true;
    }

    public void stopRec() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        recording = false;
    }

    public void playRec() {

    }

    public void deleteRec() {
        clearVisualizer();
    }

    private void updateVisualizer() {
        if ((mediaRecorder != null) && (recording)) {
            int amp = mediaRecorder.getMaxAmplitude();
            audioVisualizerScroller.addAmp(amp);
        }
        audioRecHandler.postDelayed(runnable, 50);
    }

    private void clearVisualizer() {
        audioVisualizerScroller.clearAmps();
    }
}
