package com.project.rempaudioeditor.activities;

import android.content.Intent;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.example.rempaudioeditor.R;
import com.google.android.material.color.DynamicColors;
import com.project.rempaudioeditor.AppMethods;
import com.project.rempaudioeditor.AppSettings;
import com.project.rempaudioeditor.AudioPlayerData;
import com.project.rempaudioeditor.constants.AppConstants;
import com.project.rempaudioeditor.dispatch.DispatchMethods;
import com.project.rempaudioeditor.enums.ColorId;
import com.project.rempaudioeditor.infos.AudioInfo;
import com.project.rempaudioeditor.services.KillNotificationsService;
import com.project.rempaudioeditor.utils.FileConverter;

import java.io.File;
import java.io.IOException;

public class MainActivity extends BaseActivity {
    private ColorId current_color;
    private View new_project_popup;

    ActivityResultLauncher<String> select_audio_file = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    AudioInfo new_audio = new AudioInfo(this, uri);
                    AudioPlayerData audio_player_data = AudioPlayerData.getInstance();
                    audio_player_data.addTrack(new_audio);
                    AppMethods.openActivity(MainActivity.this, EditorActivity.class);
                }
            });

    ActivityResultLauncher<String> extract_audio_from_video = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    LinearLayout storage_dialog_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_content_save_file, null);

                    AlertDialog.Builder storage_dialog_builder = DispatchMethods
                            .createDialog(this, getString(R.string.dialog_header_video_extraction_rec), storage_dialog_layout);

                    storage_dialog_builder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                        EditText file_name_view = storage_dialog_layout.findViewById(R.id.file_name_text);
                        String destination_file_name = file_name_view.getText().toString();

                        if (!destination_file_name.isEmpty()) {
                            File directory = new File(AppConstants.getCurrentAudioStorageDir());
                            if (directory.exists()) {
                                File destination_file = new File(directory, destination_file_name);
                                Toast.makeText(this, "File saved successfully!", Toast.LENGTH_SHORT).show();
                                try {
                                    // TODO: add a loader dialog here
                                    FileConverter.extractAudioFromVideo(this, uri, destination_file.getPath(), -1, -1);

                                    AudioInfo new_audio = new AudioInfo(this, Uri.fromFile(destination_file));
                                    AudioPlayerData.getInstance().addTrack(new_audio);
                                    AppMethods.openActivity(this, EditorActivity.class);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    storage_dialog_builder.show().setCanceledOnTouchOutside(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        current_color = AppSettings.getInstance().getColorId();

        LayoutInflater layout_inflater = getLayoutInflater();
        new_project_popup = layout_inflater.inflate(R.layout.popup_new_project, null);

        ImageButton open_settings_btn = findViewById(R.id.settings_btn);
        open_settings_btn.setOnClickListener(view -> AppMethods.openActivity(this, SettingsActivity.class));

        ImageButton new_project_btn = findViewById(R.id.new_project_btn);
        new_project_btn.setOnClickListener(view -> createNewProject());

        Button record_new_audio_btn = new_project_popup.findViewById(R.id.new_project_recording_btn);
        record_new_audio_btn.setOnClickListener(view -> AppMethods.openActivity(this, RecorderActivity.class));

        Button open_from_audio_file_btn = new_project_popup.findViewById(R.id.new_project_from_existing_file_btn);
        open_from_audio_file_btn.setOnClickListener(view -> openFromAudioFile());

        Button open_from_video_file_btn = new_project_popup.findViewById(R.id.new_project_from_video_btn);
        open_from_video_file_btn.setOnClickListener(view -> openFromVideoFile());

        startService(new Intent(this, KillNotificationsService.class));
    }

    @Override
    protected void onResume() {
        if (current_color != AppSettings.getInstance().getColorId())
            recreate();
        super.onResume();
    }

    // Button actions
    private void createNewProject() {
        DispatchMethods.sendPopup(new_project_popup, new Fade(), true);
    }

    private void openFromAudioFile() {
        select_audio_file.launch("audio/*");
    }

    private void openFromVideoFile() {
        extract_audio_from_video.launch("video/*");
    }
}