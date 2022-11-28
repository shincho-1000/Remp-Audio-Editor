package com.project.rempaudioeditor.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AppMethods;
import com.project.rempaudioeditor.AppSettings;
import com.project.rempaudioeditor.recycleradapters.SettingsItemsAdapter;
import com.project.rempaudioeditor.utils.UriConverter;
import com.project.rempaudioeditor.database.SettingsJsonManager;
import com.project.rempaudioeditor.dispatch.DispatchMethods;
import com.project.rempaudioeditor.enums.SettingId;
import com.project.rempaudioeditor.enums.ThemeId;
import com.project.rempaudioeditor.infos.SettingsItemInfo;
import com.project.rempaudioeditor.constants.AppData;
import com.project.rempaudioeditor.constants.RecyclerViewItems;

import java.util.ArrayList;

public class SettingsActivity extends DefaultActivity implements SettingsItemsAdapter.SettingsItemClickListener {
    private final ArrayList<SettingsItemInfo> settings_items = RecyclerViewItems.getSettingsItemList();
    private PopupWindow theme_popup = null;
    private TextView folder_path_text_view;

    private final ActivityResultLauncher<Uri> select_default_audio_dir = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri != null) {
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    String path = UriConverter.getPath(this, docUri);
                    folder_path_text_view.setText(path);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LinearLayoutManager settings_list_manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        SettingsItemsAdapter settings_array_adapter = new SettingsItemsAdapter(this, settings_items, this);
        RecyclerView settings_list_view = findViewById(R.id.settings_list);
        settings_list_view.setLayoutManager(settings_list_manager);
        settings_list_view.setAdapter(settings_array_adapter);

        final ImageButton back_btn = findViewById(R.id.back_from_settings_btn);
        back_btn.setOnClickListener(view -> AppMethods.finishActivity(this));
    }

    // Recycler view clicks
    @Override
    public void onSettingsItemClick(int position) {
        SettingsItemInfo item = settings_items.get(position);
        SettingId item_id = item.getId();
        LayoutInflater inflater = getLayoutInflater();
        switch (item_id) {
            case THEME:
                View popup_view = inflater.inflate(R.layout.popup_settings_theme,  null);

                theme_popup = DispatchMethods.sendPopup(popup_view, new Fade());

                RadioGroup theme_radio_group = popup_view.findViewById(R.id.theme_radio_group);

                int current_theme_id = AppCompatDelegate.getDefaultNightMode();

                if (current_theme_id == AppCompatDelegate.MODE_NIGHT_NO) {
                    theme_radio_group.check(R.id.light_theme_radio);
                } else if (current_theme_id == AppCompatDelegate.MODE_NIGHT_YES) {
                    theme_radio_group.check(R.id.dark_theme_radio);
                } else {
                    theme_radio_group.check(R.id.default_theme_radio);
                }

                theme_radio_group.setOnCheckedChangeListener(this::changeTheme);
                break;
            case DEFAULT_AUDIO_STORAGE_DIR:
                ConstraintLayout change_default_audio_dir_dialog_layout = (ConstraintLayout) getLayoutInflater()
                        .inflate(R.layout.dialog_content_set_default_audio_storgae_dir, null);

                AlertDialog.Builder dialog_builder = DispatchMethods
                        .createDialog(this, getString(R.string.dialog_header_settings_change_default_audio_path), change_default_audio_dir_dialog_layout);

                dialog_builder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                    String folder_path = folder_path_text_view.getText().toString();

                    if (!folder_path.isEmpty()) {
                        AppSettings.getInstance().setCurrentAudioStorageDir(folder_path);
                    } else {
                        AppSettings.getInstance().setCurrentAudioStorageDir(AppData.getDefaultAudioStorageDir().getAbsolutePath());
                    }

                    SettingsJsonManager.write(getApplicationContext(), AppSettings.getInstance());

                    AppMethods.reloadActivity(this);

                    overridePendingTransition(0, 0);
                });

                dialog_builder.show();

                folder_path_text_view = change_default_audio_dir_dialog_layout.findViewById(R.id.folder_path_text);

                ImageButton folder_path_select_btn = change_default_audio_dir_dialog_layout.findViewById(R.id.folder_path_select_btn);
                folder_path_select_btn
                        .setOnClickListener((view) -> select_default_audio_dir.launch(Uri.parse(DocumentsContract.EXTRA_INITIAL_URI)));
                break;
        }
    }

    // Button actions
    private void changeTheme(RadioGroup group, int checked_btn_id) {
        ThemeId new_mode;

        if (checked_btn_id == R.id.light_theme_radio) {
            new_mode = ThemeId.LIGHT;
        } else if (checked_btn_id == R.id.dark_theme_radio) {
            new_mode = ThemeId.DARK;
        } else {
            new_mode = ThemeId.SYSTEM_DEF;
        }

        theme_popup.dismiss();
        new Handler().postDelayed(() -> AppMethods.setAppTheme(new_mode), 500);

        AppSettings.getInstance().setTheme(new_mode);
        SettingsJsonManager.write(getApplicationContext(), AppSettings.getInstance());
    }
}