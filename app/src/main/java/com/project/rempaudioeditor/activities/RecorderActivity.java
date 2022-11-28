package com.project.rempaudioeditor.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AppMethods;
import com.project.rempaudioeditor.AudioPlayerData;
import com.project.rempaudioeditor.AudioRecorderData;
import com.project.rempaudioeditor.constants.AppData;
import com.project.rempaudioeditor.constants.PermissionRequestConstants;
import com.project.rempaudioeditor.customviews.RecorderVisualizerScroller;
import com.project.rempaudioeditor.dispatch.DispatchMethods;
import com.project.rempaudioeditor.infos.AudioInfo;
import com.project.rempaudioeditor.receivers.RecordingReceiver;

import java.io.File;


public class RecorderActivity extends DefaultActivity {
    private AudioRecorderData audio_recorder_data;

    private final String[] record_permissions = {PermissionRequestConstants.RECORD_AUDIO_PERMISSION};
    private final String[] storage_permissions = {PermissionRequestConstants.READ_STORAGE_PERMISSION,
            PermissionRequestConstants.WRITE_STORAGE_PERMISSION};

    private ImageView recorder_toggle_btn;
    private Chronometer recorder_timer;
    long timeWhenPaused = 0;

    final Handler timer_notification_handler = new Handler();
    final int delay = 1000;

    int NOTIFICATION_ID = 100;
    NotificationManagerCompat managerCompat;
    RecordingReceiver recordingReceiver = new RecordingReceiver();
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");

            switch (action) {
                case "toggle":
                    toggleRecording();
                    break;
                case "stop":
                    stopRecording();
                    startActivity(new Intent(getApplicationContext(), RecorderActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                    break;
                case "delete":
                    deleteRecording();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        registerReceiver(broadcastReceiver, new IntentFilter("MEDIA_CONTROL"));
        managerCompat = NotificationManagerCompat.from(this);

        audio_recorder_data = AudioRecorderData.getInstance();

        recorder_timer = findViewById(R.id.recorder_timer);

        final ImageButton back_from_recorder_btn = findViewById(R.id.back_from_recorder_btn);
        back_from_recorder_btn.setOnClickListener(v -> AppMethods.finishActivity(this));

        recorder_toggle_btn = findViewById(R.id.record_btn);
        recorder_toggle_btn.setOnClickListener(view -> toggleRecording());

        ImageView recorder_stop_btn = findViewById(R.id.stop_recording_btn);
        recorder_stop_btn.setOnClickListener(view -> stopRecording());

        ImageView recorder_delete_btn = findViewById(R.id.delete_recording_btn);
        recorder_delete_btn.setOnClickListener(view -> deleteRecording());
    }

    public void toggleRecording() {
        if (ContextCompat.checkSelfPermission(this, PermissionRequestConstants.RECORD_AUDIO_PERMISSION)
                == PackageManager.PERMISSION_DENIED) {
            DispatchMethods.createPermissionRequiredDialog(this,
                    getString(R.string.dialog_header_permission_required),
                    getString(R.string.dialog_desc_permission_record_audio),
                    record_permissions,
                    PermissionRequestConstants.REQUEST_RECORD_AUDIO_PERMISSION_CODE);
            return;
        }

        if (audio_recorder_data.getMediaRecorder() == null) {
            RecorderVisualizerScroller visualizerScroller = findViewById(R.id.recorder_visualizer);
            audio_recorder_data.startRec(this.getApplicationContext(), visualizerScroller);
            recorder_timer.setBase(SystemClock.elapsedRealtime());
            recorder_timer.start();
            recorder_toggle_btn.setImageResource(R.drawable.icon_pause);

            NotificationChannel channel = new NotificationChannel("RempAudioEditorNotification", "RempChannel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            timer_notification_handler.postDelayed(new Runnable() {
                public void run() {
                    if (audio_recorder_data.ifRecording()) {
                        timer_notification_handler.postDelayed(this, delay);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(RecorderActivity.this, "RempAudioEditorNotification")
                                .setContentTitle("Recording...")
                                .setContentText(recorder_timer.getText())
                                .setSmallIcon(R.drawable.app_logo)
                                .addAction(R.drawable.icon_pause, "Toggle", AppMethods.makePendingIntent(RecorderActivity.this, "toggle", recordingReceiver))
                                .addAction(R.drawable.icon_stop, "Stop", AppMethods.makePendingIntent(RecorderActivity.this, "stop", recordingReceiver))
                                .addAction(R.drawable.icon_delete, "Delete", AppMethods.makePendingIntent(RecorderActivity.this, "delete", recordingReceiver))
                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setOnlyAlertOnce(true)
                                .setOngoing(true);

                        managerCompat.notify(NOTIFICATION_ID, builder.build());
                    }
                }
            }, delay);
        } else {
            if (!audio_recorder_data.ifRecording()) {
                audio_recorder_data.resumeRec();
                recorder_timer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
                recorder_timer.start();
                recorder_toggle_btn.setImageResource(R.drawable.icon_pause);

                timer_notification_handler.postDelayed(new Runnable() {
                    public void run() {
                        if (audio_recorder_data.ifRecording()) {
                            timer_notification_handler.postDelayed(this, delay);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(RecorderActivity.this, "RempAudioEditorNotification")
                                    .setContentTitle("Recording...")
                                    .setContentText(recorder_timer.getText())
                                    .setSmallIcon(R.drawable.app_logo)
                                    .addAction(R.drawable.icon_pause, "Toggle", AppMethods.makePendingIntent(RecorderActivity.this, "toggle", recordingReceiver))
                                    .addAction(R.drawable.icon_stop, "Stop", AppMethods.makePendingIntent(RecorderActivity.this, "stop", recordingReceiver))
                                    .addAction(R.drawable.icon_delete, "Delete", AppMethods.makePendingIntent(RecorderActivity.this, "delete", recordingReceiver))
                                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2))
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setOnlyAlertOnce(true)
                                    .setOngoing(true);

                            managerCompat.notify(NOTIFICATION_ID, builder.build());
                        }
                    }
                }, delay);
            } else {
                audio_recorder_data.pauseRec();
                timeWhenPaused = recorder_timer.getBase() - SystemClock.elapsedRealtime();
                recorder_timer.stop();
                recorder_toggle_btn.setImageResource(R.drawable.icon_mic);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "RempAudioEditorNotification")
                        .setContentTitle("Recording - Paused")
                        .setContentText(recorder_timer.getText())
                        .setSmallIcon(R.drawable.app_logo)
                        .addAction(R.drawable.icon_play, "Toggle", AppMethods.makePendingIntent(this, "toggle", recordingReceiver))
                        .addAction(R.drawable.icon_stop, "Stop", AppMethods.makePendingIntent(this, "stop", recordingReceiver))
                        .addAction(R.drawable.icon_delete, "Delete", AppMethods.makePendingIntent(this, "delete", recordingReceiver))
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOnlyAlertOnce(true)
                        .setOngoing(true);

                managerCompat.notify(NOTIFICATION_ID, builder.build());
            }
        }
    }

    public void stopRecording() {
        if (audio_recorder_data.getMediaRecorder() != null) {
            audio_recorder_data.stopRec();
            recorder_timer.stop();
            recorder_timer.setBase(SystemClock.elapsedRealtime());
            recorder_toggle_btn.setImageResource(R.drawable.icon_mic);

            managerCompat.cancel(NOTIFICATION_ID);

            if ((ContextCompat.checkSelfPermission(this, PermissionRequestConstants.WRITE_STORAGE_PERMISSION)
                    == PackageManager.PERMISSION_DENIED) ||
                (ContextCompat.checkSelfPermission(this, PermissionRequestConstants.READ_STORAGE_PERMISSION)
                    == PackageManager.PERMISSION_DENIED)) {
                DispatchMethods.createPermissionRequiredDialog(this,
                        getString(R.string.dialog_header_permission_required),
                        getString(R.string.dialog_desc_permission_storage),
                        storage_permissions,
                        PermissionRequestConstants.REQUEST_STORAGE_PERMISSION_CODE);
                return;
            }

            LinearLayout dialog_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_content_save_file, null);

            AlertDialog.Builder dialogBuilder = DispatchMethods
                    .createDialog(this, getString(R.string.dialog_header_recorder_store_rec), dialog_layout);

            dialogBuilder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                EditText file_name_edit_text = dialog_layout.findViewById(R.id.file_name_text);
                String file_name = file_name_edit_text.getText().toString();

                if (!file_name.isEmpty()) {
                    File dir = new File((AppData.getCurrentAudioStorageDir()));
                    if (dir.exists()) {
                        File source_rec = new File(AppData.getAppAudioRecordingFilePath(this));
                        File final_rec = new File(dir, file_name);
                        if (source_rec.exists()) {
                            if (source_rec.renameTo(final_rec)) {
                                Toast.makeText(this, "File saved successfully!", Toast.LENGTH_SHORT).show();

                                AudioInfo newTrack = new AudioInfo(this, Uri.fromFile(final_rec));
                                newTrack.setWaveFormCreatedListener(() -> {
                                    AppMethods.openActivity(this, EditorActivity.class);
                                    AppMethods.finishActivity(this);
                                });
                                newTrack.generateWaveform(this);
                                AudioPlayerData.getInstance().addTrack(newTrack);
                            }
                        }
                    }
                }
            });

            dialogBuilder.setNegativeButton(R.string.button_cancel, (dialog, id) -> audio_recorder_data.deleteRec());
            dialogBuilder.show().setCanceledOnTouchOutside(false);
        }
    }

    public void deleteRecording() {
        if (audio_recorder_data.getMediaRecorder() != null) {
            audio_recorder_data.stopRec();
            recorder_timer.stop();
            recorder_timer.setBase(SystemClock.elapsedRealtime());
            recorder_toggle_btn.setImageResource(R.drawable.icon_mic);
            audio_recorder_data.deleteRec();

            managerCompat.cancel(NOTIFICATION_ID);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcastReceiver);
        managerCompat.cancel(NOTIFICATION_ID);

        deleteRecording();
    }

}