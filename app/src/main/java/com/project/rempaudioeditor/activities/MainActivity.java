package com.project.rempaudioeditor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.dispatch.DispatchPopup;

public class MainActivity extends AppCompatActivity {

    private View popup_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inflating the popup view
        LayoutInflater layoutInflater = getLayoutInflater();
        popup_view = layoutInflater.inflate(R.layout.popup_new_project, null);

        // Button event listeners
        final ImageButton open_setting_btn = findViewById(R.id.settings_btn);
        open_setting_btn.setOnClickListener(this::openSettingsActivity);

        final ImageButton new_project_btn = findViewById(R.id.new_project_btn);
        new_project_btn.setOnClickListener(this::createNewProject);

        final Button record_new_audio_btn = popup_view.findViewById(R.id.new_project_recording_btn);
        record_new_audio_btn.setOnClickListener(this::recordNewAudio);
    }

    // Button actions
    private void openSettingsActivity(View view) {
        Intent settings_intent = new Intent(this, SettingsActivity.class);
        startActivity(settings_intent);
    }

    private void createNewProject(View view) {
        DispatchPopup.sendPopupAtCenter(popup_view, new Fade());
    }

    private void recordNewAudio(View view) {
        Intent new_recording_intent = new Intent(this, RecorderActivity.class);
        startActivity(new_recording_intent);
    }

    private void openEditorFromExistingAudio(View view) {

    }
}