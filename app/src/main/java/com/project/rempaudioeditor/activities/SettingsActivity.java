package com.project.rempaudioeditor.activities;

import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.Settings;
import com.project.rempaudioeditor.arrayadapters.SettingsItemsAdapter;
import com.project.rempaudioeditor.database.SettingsJsonManager;
import com.project.rempaudioeditor.dispatch.DispatchPopup;
import com.project.rempaudioeditor.enums.SettingId;
import com.project.rempaudioeditor.enums.ThemeId;
import com.project.rempaudioeditor.infos.SettingsItemView;
import com.project.rempaudioeditor.values.SettingsRecyclerItems;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity implements SettingsItemsAdapter.SettingsItemClickListener {
    private final ArrayList<SettingsItemView> settingsItems = SettingsRecyclerItems.getSettingsItemList();
    private EditText folder_path_edit_text;

    ActivityResultLauncher<Uri> selectAudioDir = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri != null) {
                    folder_path_edit_text.setText(uri.getPath());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Making the recycler view of settings
        LinearLayoutManager settingsListManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        SettingsItemsAdapter settingsArrayAdapter = new SettingsItemsAdapter(this, settingsItems, this);
        RecyclerView settingsListView = findViewById(R.id.settings_list);
        settingsListView.setLayoutManager(settingsListManager);
        settingsListView.setAdapter(settingsArrayAdapter);

        // Button event listeners

        // Back button
        final ImageButton back_from_settings_btn = findViewById(R.id.back_from_settings_btn);
        back_from_settings_btn.setOnClickListener(view -> returnToHomeActivity());
    }

    // Recycler view clicks
    @Override
    public void onSettingsItemClick(int position) {
        SettingsItemView item = settingsItems.get(position);
        SettingId item_id = item.getId();
        LayoutInflater inflater = getLayoutInflater();
        switch (item_id) {
            case THEME:
                View popup_view = inflater.inflate(R.layout.popup_settings_theme, null);

                DispatchPopup.sendPopupAtCenter(popup_view, new Fade());

                RadioGroup radio_group = popup_view.findViewById(R.id.theme_radio_group);

                int current_theme_id = AppCompatDelegate.getDefaultNightMode();

                if (current_theme_id == AppCompatDelegate.MODE_NIGHT_NO) {
                    radio_group.check(R.id.light_theme_radio);
                } else if (current_theme_id == AppCompatDelegate.MODE_NIGHT_YES) {
                    radio_group.check(R.id.dark_theme_radio);
                } else {
                    radio_group.check(R.id.default_theme_radio);
                }

                radio_group.setOnCheckedChangeListener(this::changeTheme);
                break;
            case DEFAULT_AUDIO_STORAGE_DIR:
                ConstraintLayout dialog_layout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_content_set_default_audio_storgae_dir, null);

                AlertDialog.Builder dialogBuilder = DispatchPopup.createDialog(this, "Do you want to change the path?", dialog_layout);

                dialogBuilder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                    String folder_path = folder_path_edit_text.getText().toString();

                    if (!folder_path.isEmpty()) {
                        Settings.getInstance().setCurrentAudioStorageDir(folder_path);
                        SettingsJsonManager.writeSettingsJson(getApplicationContext(), Settings.getInstance());
                    }
                });

                dialogBuilder.show();

                folder_path_edit_text = dialog_layout.findViewById(R.id.folder_path_text);

                ImageButton folder_path_select_btn = dialog_layout.findViewById(R.id.folder_path_select_btn);
                folder_path_select_btn.setOnClickListener((view) -> {
                    selectAudioDir.launch(Uri.parse(DocumentsContract.EXTRA_INITIAL_URI));
                });

                break;
        }
    }

    // Button actions
    private void returnToHomeActivity() {
        finish();
    }

    private void changeTheme(RadioGroup group, int checked_btn_id) {
        ThemeId new_mode;

        if (checked_btn_id == R.id.light_theme_radio) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            new_mode = ThemeId.LIGHT;
        } else if (checked_btn_id == R.id.dark_theme_radio) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            new_mode = ThemeId.DARK;
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            new_mode = ThemeId.SYSTEM_DEF;
        }

        Settings.getInstance().setTheme(new_mode);
        SettingsJsonManager.writeSettingsJson(getApplicationContext(), Settings.getInstance());
    }
}