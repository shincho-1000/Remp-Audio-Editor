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
import com.project.rempaudioeditor.broadcast_receivers.RecordingReceiver;
import com.project.rempaudioeditor.constants.AppConstants;
import com.project.rempaudioeditor.constants.NotificationIds;
import com.project.rempaudioeditor.constants.PermissionRequestConstants;
import com.project.rempaudioeditor.customviews.RecorderVisualizerScroller;
import com.project.rempaudioeditor.dispatch.DispatchMethods;
import com.project.rempaudioeditor.infos.AudioInfo;

import java.io.File;


public class RecorderActivity extends BaseActivity {
    private AudioRecorderData audio_recorder_data;

    private final String[] RECORD_PERMISSIONS = {PermissionRequestConstants.RECORD_AUDIO_PERMISSION};
    private final String[] STORAGE_PERMISSIONS = {PermissionRequestConstants.READ_STORAGE_PERMISSION,
            PermissionRequestConstants.WRITE_STORAGE_PERMISSION};

    private ImageView recorder_toggle_btn;
    private Chronometer recorder_timer;
    private long time_when_paused = 0;

    private final Handler timer_notification_handler = new Handler();
    private final int NOTIFICATION_UPDATE_DELAY_MILISEC = 1000;

    private final String ACTION_TOGGLE = "toggle";
    private final String ACTION_STOP = "stop";
    private final String ACTION_DELETE = "delete";

    private NotificationManagerCompat recording_notification_manager;
    private final RecordingReceiver recording_receiver = new RecordingReceiver();
    private final BroadcastReceiver broadcast_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");

            switch (action) {
                case ACTION_TOGGLE:
                    toggleRecording();
                    break;
                case ACTION_STOP:
                    stopRecording();
                    startActivity(new Intent(getApplicationContext(), RecorderActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                    break;
                case ACTION_DELETE:
                    deleteRecording();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        registerReceiver(broadcast_receiver, new IntentFilter("MEDIA_CONTROL"));
        recording_notification_manager = NotificationManagerCompat.from(this);

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
                    RECORD_PERMISSIONS,
                    PermissionRequestConstants.REQUEST_RECORD_AUDIO_PERMISSION_CODE);
            return;
        }

        if (audio_recorder_data.getMediaRecorder() == null) {
            RecorderVisualizerScroller visualizer_scroller = findViewById(R.id.recorder_visualizer);
            audio_recorder_data.startRec(this.getApplicationContext(), visualizer_scroller);
            recorder_timer.setBase(SystemClock.elapsedRealtime());
            recorder_timer.start();
            recorder_toggle_btn.setImageResource(R.drawable.icon_pause);

            NotificationChannel app_notification_channel = new NotificationChannel("RempAudioEditorNotification", "RempChannel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notification_manager = getSystemService(NotificationManager.class);
            notification_manager.createNotificationChannel(app_notification_channel);

            timer_notification_handler.postDelayed(new Runnable() {
                public void run() {
                    if (audio_recorder_data.ifRecording()) {
                        timer_notification_handler.postDelayed(this, NOTIFICATION_UPDATE_DELAY_MILISEC);
                        sendRecordingNotification("Recording...");
                    }
                }
            }, NOTIFICATION_UPDATE_DELAY_MILISEC);
        } else {
            if (!audio_recorder_data.ifRecording()) {
                audio_recorder_data.resumeRec();
                recorder_timer.setBase(SystemClock.elapsedRealtime() + time_when_paused);
                recorder_timer.start();
                recorder_toggle_btn.setImageResource(R.drawable.icon_pause);

                timer_notification_handler.postDelayed(new Runnable() {
                    public void run() {
                        if (audio_recorder_data.ifRecording()) {
                            timer_notification_handler.postDelayed(this, NOTIFICATION_UPDATE_DELAY_MILISEC);
                            sendRecordingNotification("Recording...");
                        }
                    }
                }, NOTIFICATION_UPDATE_DELAY_MILISEC);
            } else {
                audio_recorder_data.pauseRec();
                time_when_paused = recorder_timer.getBase() - SystemClock.elapsedRealtime();
                recorder_timer.stop();
                recorder_toggle_btn.setImageResource(R.drawable.icon_mic);

                sendRecordingNotification("Recording - Paused");
            }
        }
    }

    public void stopRecording() {
        if (audio_recorder_data.getMediaRecorder() != null) {
            audio_recorder_data.stopRecording();
            recorder_timer.stop();
            recorder_timer.setBase(SystemClock.elapsedRealtime());
            recorder_toggle_btn.setImageResource(R.drawable.icon_mic);

            recording_notification_manager.cancel(NotificationIds.getRecordingNotificationId());

            if ((ContextCompat.checkSelfPermission(this, PermissionRequestConstants.WRITE_STORAGE_PERMISSION)
                    == PackageManager.PERMISSION_DENIED) ||
                (ContextCompat.checkSelfPermission(this, PermissionRequestConstants.READ_STORAGE_PERMISSION)
                    == PackageManager.PERMISSION_DENIED)) {
                DispatchMethods.createPermissionRequiredDialog(this,
                        getString(R.string.dialog_header_permission_required),
                        getString(R.string.dialog_desc_permission_storage),
                        STORAGE_PERMISSIONS,
                        PermissionRequestConstants.REQUEST_STORAGE_PERMISSION_CODE);
                return;
            }

            LinearLayout dialog_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_content_save_file, null);

            AlertDialog.Builder dialogBuilder = DispatchMethods
                    .createDialog(this, getString(R.string.dialog_header_recorder_store_rec), dialog_layout);

            dialogBuilder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                EditText file_name_view = dialog_layout.findViewById(R.id.file_name_text);
                String file_name = file_name_view.getText().toString();

                if (!file_name.isEmpty()) {
                    File directory = new File((AppConstants.getCurrentAudioStorageDir()));
                    if (directory.exists()) {
                        File source_file = new File(AppConstants.getAppAudioRecordingFilePath(this));
                        File destination_file = new File(directory, file_name);
                        if (source_file.exists()) {
                            if (source_file.renameTo(destination_file)) {
                                Toast.makeText(this, "File saved successfully!", Toast.LENGTH_SHORT).show();

                                AudioInfo new_audio = new AudioInfo(this, Uri.fromFile(destination_file));
                                AppMethods.openActivity(this, EditorActivity.class);
                                AppMethods.finishActivity(this);
                                AudioPlayerData.getInstance().addTrack(new_audio);
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
            audio_recorder_data.stopRecording();
            recorder_timer.stop();
            recorder_timer.setBase(SystemClock.elapsedRealtime());
            recorder_toggle_btn.setImageResource(R.drawable.icon_mic);
            audio_recorder_data.deleteRec();

            recording_notification_manager.cancel(NotificationIds.getRecordingNotificationId());
        }
    }

    public void sendRecordingNotification(String title) {
        NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(this, "RempAudioEditorNotification")
                .setContentTitle(title)
                .setContentText(recorder_timer.getText())
                .setSmallIcon(R.drawable.app_logo)
                .addAction(R.drawable.icon_play, "Toggle", AppMethods.makePendingIntent(this, ACTION_TOGGLE, recording_receiver))
                .addAction(R.drawable.icon_stop, "Stop", AppMethods.makePendingIntent(this, ACTION_STOP, recording_receiver))
                .addAction(R.drawable.icon_delete, "Delete", AppMethods.makePendingIntent(this, ACTION_DELETE, recording_receiver))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        recording_notification_manager.notify(NotificationIds.getRecordingNotificationId(), notification_builder.build());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcast_receiver);
        recording_notification_manager.cancel(NotificationIds.getRecordingNotificationId());

        deleteRecording();
    }
}