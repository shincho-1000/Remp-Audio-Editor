package com.project.rempaudioeditor.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AudioRecorderData;
import com.project.rempaudioeditor.dispatch.DispatchPopup;
import com.project.rempaudioeditor.values.AppConstants;
import com.project.rempaudioeditor.values.PermissionConstants;
import com.project.rempaudioeditor.views.RecorderVisualizerScroller;

import java.io.File;

public class RecorderActivity extends AppCompatActivity {

    private AudioRecorderData audioRecorderData;
    private boolean permissionToRecordAccepted = false;
    private final String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private ImageView recorder_btn;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionConstants.REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        audioRecorderData = AudioRecorderData.getInstance();

        // Back button
        final ImageButton back_from_recorder_btn = findViewById(R.id.back_from_recorder_btn);
        back_from_recorder_btn.setOnClickListener(this::returnToHomeActivity);

        // Recorder buttons
        recorder_btn = findViewById(R.id.record_btn);
        recorder_btn.setOnClickListener(view -> toggleRecording());

        ImageView recorder_stop_btn = findViewById(R.id.stop_recording_btn);
        recorder_stop_btn.setOnClickListener(view -> stopRecording());

        ImageView recorder_delete_btn = findViewById(R.id.delete_recording_btn);
        recorder_delete_btn.setOnClickListener(view -> deleteRecording());
    }

    private void returnToHomeActivity(View view) {
        finish();
    }

    private void toggleRecording() {
        ActivityCompat.requestPermissions(this, permissions, PermissionConstants.REQUEST_RECORD_AUDIO_PERMISSION);

        if (audioRecorderData.getMediaRecorder() == null) {
            RecorderVisualizerScroller visualizerScroller = findViewById(R.id.recorder_visualizer);
            audioRecorderData.startRec(this.getApplicationContext(), visualizerScroller);
            recorder_btn.setImageResource(R.drawable.icon_pause);
        } else {
            if (!audioRecorderData.ifRecording()) {
                audioRecorderData.resumeRec();
                recorder_btn.setImageResource(R.drawable.icon_pause);
            } else {
                audioRecorderData.pauseRec();
                recorder_btn.setImageResource(R.drawable.icon_mic);
            }
        }
    }

    private void stopRecording() {
        if (audioRecorderData.getMediaRecorder() != null) {
            audioRecorderData.stopRec();
            recorder_btn.setImageResource(R.drawable.icon_mic);

            LinearLayout dialog_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_content_save_file, null);

            AlertDialog.Builder dialogBuilder = DispatchPopup.createDialog(this, "Do you want to store the following recording?", dialog_layout);

            dialogBuilder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                EditText file_name_edit_text = dialog_layout.findViewById(R.id.file_name_text);
                String file_name = file_name_edit_text.getText().toString();

                if (!file_name.isEmpty()) {
                    File dir = new File((AppConstants.getCurrentAudioStorageDir()));
                    if (dir.exists()) {
                        File from = new File(AppConstants.getAppAudioRecordingFilePath(this));
                        File to = new File(dir, file_name);
                        if (from.exists()) {
                            from.renameTo(to);
                        }
                    }
                }
            });

            dialogBuilder.setNegativeButton(R.string.button_cancel, (dialog, id) -> {
                deleteRecording();
            });
            dialogBuilder.show().setCanceledOnTouchOutside(false);
        }
    }

    private void deleteRecording() {
        stopRecording();
        audioRecorderData.deleteRec();
    }

    private void playRecording() {
        audioRecorderData.playRec();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopRecording();
    }
}
