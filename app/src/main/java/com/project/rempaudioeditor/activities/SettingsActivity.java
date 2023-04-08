package com.project.rempaudioeditor.activities;

import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AppMethods;
import com.project.rempaudioeditor.AppSettings;
import com.project.rempaudioeditor.constants.AppConstants;
import com.project.rempaudioeditor.constants.PermissionRequestConstants;
import com.project.rempaudioeditor.constants.RecyclerViewItems;
import com.project.rempaudioeditor.customviews.ColorSelectionButton;
import com.project.rempaudioeditor.database.SettingsJsonManager;
import com.project.rempaudioeditor.dispatch.DispatchMethods;
import com.project.rempaudioeditor.enums.ColorId;
import com.project.rempaudioeditor.enums.SettingId;
import com.project.rempaudioeditor.enums.ThemeId;
import com.project.rempaudioeditor.infos.SettingsItemInfo;
import com.project.rempaudioeditor.recycleradapters.SettingsItemsAdapter;
import com.project.rempaudioeditor.utils.UriConverter;

import java.util.ArrayList;

public class SettingsActivity extends BaseActivity implements SettingsItemsAdapter.SettingsItemClickListener {
    private final ArrayList<SettingsItemInfo> settings_items = RecyclerViewItems.getSettingsItemList();
    private PopupWindow change_theme_popup = null;
    private PopupWindow change_accent_color_popup = null;
    private TextView folder_path_view;

    private final String[] STORAGE_PERMISSIONS = {PermissionRequestConstants.READ_STORAGE_PERMISSION,
            PermissionRequestConstants.WRITE_STORAGE_PERMISSION};

    private final ActivityResultLauncher<Uri> select_default_audio_directory = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri != null) {
                    Uri directory_uri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    String directory_path = UriConverter.getPath(this, directory_uri);
                    folder_path_view.setText(directory_path);
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

    @Override
    public void onSettingsItemClick(int position) {
        SettingsItemInfo item = settings_items.get(position);
        SettingId item_id = item.getId();
        LayoutInflater inflater = getLayoutInflater();
        switch (item_id) {
            case THEME:
                View change_theme_popup_view = inflater.inflate(R.layout.popup_settings_theme,  null);

                change_theme_popup = DispatchMethods.sendPopup(change_theme_popup_view, new Fade(), true);

                RadioGroup theme_radio_group = change_theme_popup_view.findViewById(R.id.theme_radio_group);

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
                ConstraintLayout change_default_audio_directory_dialog_layout = (ConstraintLayout) getLayoutInflater()
                        .inflate(R.layout.dialog_content_set_default_audio_storgae_dir, null);

                AlertDialog.Builder change_default_audio_directory_dialog_builder = DispatchMethods
                        .createDialog(this, getString(R.string.dialog_header_settings_change_default_audio_path), change_default_audio_directory_dialog_layout);

                change_default_audio_directory_dialog_builder.setPositiveButton(R.string.button_confirm, (dialog, id) -> {
                    String folder_path = folder_path_view.getText().toString();

                    if (!folder_path.isEmpty()) {
                        AppSettings.getInstance().setCurrentAudioStorageDir(folder_path);
                    } else {
                        AppSettings.getInstance().setCurrentAudioStorageDir(AppConstants.getDefaultAudioStorageDir().getAbsolutePath());
                    }

                    SettingsJsonManager.write(getApplicationContext(), AppSettings.getInstance());

                    AppMethods.reloadActivity(this);

                    overridePendingTransition(0, 0);
                });

                change_default_audio_directory_dialog_builder.show();

                folder_path_view = change_default_audio_directory_dialog_layout.findViewById(R.id.folder_path_text);

                ImageButton folder_path_select_btn = change_default_audio_directory_dialog_layout.findViewById(R.id.folder_path_select_btn);
                folder_path_select_btn
                        .setOnClickListener((view) -> select_default_audio_directory.launch(Uri.parse(DocumentsContract.EXTRA_INITIAL_URI)));
                break;
            case COLOR:
                View change_accent_color_popup_view = inflater.inflate(R.layout.popup_choose_accent_color, findViewById(R.id.container_settings), false);

                change_accent_color_popup = DispatchMethods.sendPopup(change_accent_color_popup_view, new Fade(), true);

                ColorSelectionButton accent_color_blue_btn = change_accent_color_popup_view.findViewById(R.id.accent_color_blue_btn);
                accent_color_blue_btn.setOnClickListener(v -> changeColorAccent(ColorId.BLUE));

                ColorSelectionButton accent_color_red_btn = change_accent_color_popup_view.findViewById(R.id.accent_color_red_btn);
                accent_color_red_btn.setOnClickListener(v -> changeColorAccent(ColorId.RED));

                ColorSelectionButton accent_color_green_btn = change_accent_color_popup_view.findViewById(R.id.accent_color_green_btn);
                accent_color_green_btn.setOnClickListener(v -> changeColorAccent(ColorId.GREEN));

                ColorSelectionButton accent_color_yellow_btn = change_accent_color_popup_view.findViewById(R.id.accent_color_yellow_btn);
                accent_color_yellow_btn.setOnClickListener(v -> changeColorAccent(ColorId.YELLOW));

                ColorSelectionButton accent_color_cyan_btn = change_accent_color_popup_view.findViewById(R.id.accent_color_cyan_btn);
                accent_color_cyan_btn.setOnClickListener(v -> changeColorAccent(ColorId.CYAN));

                ColorSelectionButton accent_color_pink_btn = change_accent_color_popup_view.findViewById(R.id.accent_color_pink_btn);
                accent_color_pink_btn.setOnClickListener(v -> changeColorAccent(ColorId.PINK));

                TextView accent_color_wallpaper_btn = change_accent_color_popup_view.findViewById(R.id.accent_color_wallpaper_btn);
                accent_color_wallpaper_btn.setOnClickListener(v -> changeColorAccent(ColorId.WALLPAPER));

                ColorId color_id = AppSettings.getInstance().getColorId();
                if (color_id != null) {
                    switch (color_id) {
                        case RED:
                            accent_color_red_btn.check();
                            break;
                        case GREEN:
                            accent_color_green_btn.check();
                            break;
                        case YELLOW:
                            accent_color_yellow_btn.check();
                            break;
                        case PINK:
                            accent_color_pink_btn.check();
                            break;
                        case CYAN:
                            accent_color_cyan_btn.check();
                            break;
                        case BLUE:
                            accent_color_blue_btn.check();
                            break;
                        case WALLPAPER:
                            break;
                    }
                } else {
                    accent_color_blue_btn.check();
                }
                break;
        }
    }

    private void changeColorAccent(ColorId color_id) {
        if (color_id == ColorId.WALLPAPER) {
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
        }
        AppSettings.getInstance().setAccentColor(color_id);
        SettingsJsonManager.write(getApplicationContext(), AppSettings.getInstance());
        change_accent_color_popup.dismiss();
        new Handler().postDelayed(this::recreate, AppConstants.getSetThemeDelayMilisec());
    }

    private void changeTheme(RadioGroup group, int checked_btn_id) {
        ThemeId new_mode;

        if (checked_btn_id == R.id.light_theme_radio) {
            new_mode = ThemeId.LIGHT;
        } else if (checked_btn_id == R.id.dark_theme_radio) {
            new_mode = ThemeId.DARK;
        } else {
            new_mode = ThemeId.SYSTEM_DEF;
        }

        change_theme_popup.dismiss();
        new Handler().postDelayed(() -> AppMethods.setAppTheme(new_mode), AppConstants.getSetThemeDelayMilisec());

        AppSettings.getInstance().setTheme(new_mode);
        SettingsJsonManager.write(getApplicationContext(), AppSettings.getInstance());
    }
}