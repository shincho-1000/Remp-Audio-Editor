package com.example.rempaudioeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Creating the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button event listeners
        final ImageButton open_setting_btn = findViewById(R.id.settings_btn);
        open_setting_btn.setOnClickListener(this::openSettingsActivity);
    }

    // Button actions
    private void openSettingsActivity(View view) {
        Intent settings_intent = new Intent(this, SettingsActivity.class);
        startActivity(settings_intent);
    }
}