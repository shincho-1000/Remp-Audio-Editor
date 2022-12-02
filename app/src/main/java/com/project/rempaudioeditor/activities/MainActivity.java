package com.project.rempaudioeditor.activities;

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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AppSettings;
import com.project.rempaudioeditor.AudioPlayerData;
import com.project.rempaudioeditor.AppMethods;
import com.project.rempaudioeditor.constants.AppData;
import com.project.rempaudioeditor.database.SettingsJsonManager;
import com.project.rempaudioeditor.utils.FileConverter;
import com.project.rempaudioeditor.dispatch.DispatchMethods;
import com.project.rempaudioeditor.infos.AudioInfo;

import java.io.File;
import java.io.IOException;

public class MainActivity extends DefaultActivity {

    private View new_project_popup;

    ActivityResultLauncher<String> select_audio_file = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    AudioInfo new_audio = new AudioInfo(this, uri);
                    AudioPlayerData.getInstance().addTrack(new_audio);

                    AppMethods.openActivity(MainActivity.this, EditorActivity.class);
                }
            });

    ActivityResultLauncher<String> extract_audio_from_video = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // TODO: start the editor when this is done

                    LinearLayout storage_query_dialog_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_content_save_file, null);

                    AlertDialog.Builder storage_query_dialog_builder = DispatchMethods
                            .createDialog(this, getString(R.string.dialog_header_video_extraction_rec), storage_query_dialog_layout);

                    storage_query_dialog_builder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                        EditText file_name_edit_text = storage_query_dialog_layout.findViewById(R.id.file_name_text);
                        String file_name = file_name_edit_text.getText().toString();

                        if (!file_name.isEmpty()) {
                            File dir = new File(AppData.getCurrentAudioStorageDir());
                            if (dir.exists()) {
                                File final_rec = new File(dir, file_name);
                                Toast.makeText(this, "File saved successfully!", Toast.LENGTH_SHORT).show();
                                try {
                                    FileConverter.extractAudioFromVideo(this, uri, final_rec.getPath(), -1, -1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    storage_query_dialog_builder.show().setCanceledOnTouchOutside(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load settings
        SettingsJsonManager.loadSettings(getApplicationContext());

        switch (AppSettings.getInstance().getThemeId()) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LayoutInflater layoutInflater = getLayoutInflater();
        new_project_popup = layoutInflater.inflate(R.layout.popup_new_project, null);

        ImageButton open_setting_btn = findViewById(R.id.settings_btn);
        open_setting_btn.setOnClickListener(v -> AppMethods.openActivity(this, SettingsActivity.class));

        ImageButton new_project_btn = findViewById(R.id.new_project_btn);
        new_project_btn.setOnClickListener(v -> createNewProject());

        Button record_new_audio_btn = new_project_popup.findViewById(R.id.new_project_recording_btn);
        record_new_audio_btn.setOnClickListener(v -> AppMethods.openActivity(this, RecorderActivity.class));

        Button open_from_audio_file_btn = new_project_popup.findViewById(R.id.new_project_from_existing_file_btn);
        open_from_audio_file_btn.setOnClickListener(v -> openFromAudioFile());

        Button open_from_video_file_btn = new_project_popup.findViewById(R.id.new_project_from_video_btn);
        open_from_video_file_btn.setOnClickListener(v -> openFromVideoFile());
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