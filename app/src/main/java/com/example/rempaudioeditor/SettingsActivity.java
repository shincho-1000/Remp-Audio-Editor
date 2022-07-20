package com.example.rempaudioeditor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rempaudioeditor.arrayadapters.SettingsItemsAdapter;
import com.example.rempaudioeditor.values.SettingsItems;
import com.example.rempaudioeditor.dispatch.DispatchPopup;
import com.example.rempaudioeditor.enums.SettingId;
import com.example.rempaudioeditor.viewinfos.SettingsItemView;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity implements SettingsItemsAdapter.SettingsItemClickListener {
    private final ArrayList<SettingsItemView> settingsItems = SettingsItems.getSettingsItemList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Creating the activity
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
        back_from_settings_btn.setOnClickListener(this::returnToHomeActivity);

        // Theme change

    }

    // Recycler view clicks
    @SuppressLint("SwitchIntDef")
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

                switch (current_theme_id) {
                    case AppCompatDelegate.MODE_NIGHT_NO:
                        radio_group.check(R.id.light_theme_radio);
                        break;
                    case AppCompatDelegate.MODE_NIGHT_YES:
                        radio_group.check(R.id.dark_theme_radio);
                        break;
                    default:
                        radio_group.check(R.id.default_theme_radio);
                        break;
                }
                radio_group.setOnCheckedChangeListener(this::changeTheme);
                break;
            case ABOUT:
                break;
        }
    }

    // Button actions
    private void returnToHomeActivity(View view) {
        finish();
    }

    private void changeTheme(RadioGroup group, int checked_btn_id) {
        if (checked_btn_id == R.id.light_theme_radio) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (checked_btn_id == R.id.dark_theme_radio) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (checked_btn_id == R.id.default_theme_radio) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
