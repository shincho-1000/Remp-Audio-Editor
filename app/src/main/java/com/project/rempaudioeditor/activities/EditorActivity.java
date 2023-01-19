package com.project.rempaudioeditor.activities;

import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AppMethods;
import com.project.rempaudioeditor.AudioPlayerData;
import com.project.rempaudioeditor.constants.AppConstants;
import com.project.rempaudioeditor.customviews.WaveformSeekbar;
import com.project.rempaudioeditor.enums.EditorTrayId;
import com.project.rempaudioeditor.infos.AudioInfo;
import com.project.rempaudioeditor.infos.ChannelInfo;
import com.project.rempaudioeditor.recycleradapters.EditorTrayItemAdapter;
import com.project.rempaudioeditor.dispatch.DispatchMethods;
import com.project.rempaudioeditor.infos.EditorTrayItemInfo;
import com.project.rempaudioeditor.constants.RecyclerViewItems;
import com.project.rempaudioeditor.utils.FileConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EditorActivity extends BaseActivity implements EditorTrayItemAdapter.EditorTrayItemClickListener {
    private AudioPlayerData audio_player_data;

    private final ArrayList<EditorTrayItemInfo> tray_items = RecyclerViewItems.getEditorTrayItemList();

    private ImageView play_audio_btn;
    private View add_audio_popup;
    private View loading_popup;
    private WaveformSeekbar seekbar;

    ActivityResultLauncher<String> select_audio_file = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    AudioInfo new_audio = new AudioInfo(this, uri);
                    AudioPlayerData audio_player_data = AudioPlayerData.getInstance();
                    audio_player_data.addTrack(1, new_audio);

                    new Handler().postDelayed(() -> {
                        PopupWindow loading_popup_window = DispatchMethods.sendPopup(loading_popup, new Fade(), false);
                        seekbar.setWaveFormAddedListener(() -> runOnUiThread(loading_popup_window::dismiss));
                    }, AppConstants.getPopupSendDelayMilisec());

                    Thread waveform_generation = new Thread(() -> {
                        seekbar.addWaveform(1, new_audio);
                    });
                    waveform_generation.start();
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
                                    FileConverter.extractAudioFromVideo(this, uri, destination_file.getPath(), -1, -1);

                                    AudioInfo new_audio = new AudioInfo(this, Uri.fromFile(destination_file));
                                    AudioPlayerData audio_player_data = AudioPlayerData.getInstance();
                                    audio_player_data.addTrack(2, new_audio);

                                    new Handler().postDelayed(() -> {
                                        PopupWindow loading_popup_window = DispatchMethods.sendPopup(loading_popup, new Fade(), false);
                                        seekbar.setWaveFormAddedListener(() -> runOnUiThread(loading_popup_window::dismiss));
                                    }, AppConstants.getPopupSendDelayMilisec());

                                    Thread waveform_generation = new Thread(() -> {
                                        seekbar.addWaveform(2, new_audio);
                                    });
                                    waveform_generation.start();
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
        setContentView(R.layout.activity_editor);

        LinearLayoutManager editor_tray_list_manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        EditorTrayItemAdapter editor_tray_adapter = new EditorTrayItemAdapter(this, tray_items, this);
        RecyclerView tray_list_view = findViewById(R.id.editor_tray);
        tray_list_view.setLayoutManager(editor_tray_list_manager);
        tray_list_view.setAdapter(editor_tray_adapter);

        LayoutInflater layout_inflater = getLayoutInflater();
        add_audio_popup = layout_inflater.inflate(R.layout.popup_add_audio, null);
        loading_popup = layout_inflater.inflate(R.layout.popup_importing, null);

        play_audio_btn = findViewById(R.id.play_audio_btn);
        play_audio_btn.setOnClickListener(view -> togglePlay());

        ImageButton add_audio_btn = findViewById(R.id.add_audio_btn);
        add_audio_btn.setOnClickListener(view -> addAudio());

        seekbar = findViewById(R.id.editor_waveform_seeker);
        seekbar.setSeekHoldListener(() -> {
            play_audio_btn.setImageResource(R.drawable.icon_play);
        });

        Button record_new_audio_btn = add_audio_popup.findViewById(R.id.add_audio_recording_btn);

        Button open_from_audio_file_btn = add_audio_popup.findViewById(R.id.add_audio_from_existing_file_btn);
        open_from_audio_file_btn.setOnClickListener(v -> openFromAudioFile());

        Button open_from_video_file_btn = add_audio_popup.findViewById(R.id.add_audio_from_video_btn);
        open_from_video_file_btn.setOnClickListener(v -> openFromVideoFile());

        final ImageButton back_btn = findViewById(R.id.back_from_editor_btn);
        back_btn.setOnClickListener(view -> AppMethods.finishActivity(this));

        new Handler().postDelayed(() -> {
            PopupWindow loading_popup_window = DispatchMethods.sendPopup(loading_popup, new Fade(), false);
            seekbar.setWaveFormsInitializedListener(() -> {
                runOnUiThread(loading_popup_window::dismiss);
            });
        }, AppConstants.getPopupSendDelayMilisec());

        AudioPlayerData.getInstance().initializePlayer(this, findViewById(R.id.editor_fft_visualizer), findViewById(R.id.editor_waveform_seeker), findViewById(R.id.durationText), findViewById(R.id.totalDurationText));
    }

    // Button actions

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view_in_focus = getCurrentFocus();
            if (view_in_focus instanceof EditText) {
                Rect rect = new Rect();
                view_in_focus.getGlobalVisibleRect(rect);
                int touch_x_coordinate = (int) ev.getRawX();
                int touch_y_coordinate = (int) ev.getRawY();
                if (!rect.contains(touch_x_coordinate, touch_y_coordinate)) {
                    view_in_focus.clearFocus();
                    InputMethodManager input_method_manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    input_method_manager.hideSoftInputFromWindow(view_in_focus.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void togglePlay() {
        audio_player_data = AudioPlayerData.getInstance();

        ChannelInfo longest_channel = audio_player_data.getChannelList().get(audio_player_data.getLongestChannelIndex());
        MediaPlayer audio_player = longest_channel.getPlayer();

        if (audio_player_data.isInitialized()) {
            if ((!longest_channel.getReleased()) && (audio_player.isPlaying())) {
                audio_player_data.pausePlayers();
                play_audio_btn.setImageResource(R.drawable.icon_play);
            } else {
                seekbar.seekPlayer();
                audio_player_data.resumePlayers();
                play_audio_btn.setImageResource(R.drawable.icon_pause);
            }
        } else {
            audio_player_data.startPlayers(this);
            seekbar.seekPlayer();
            audio_player_data.resumePlayers();
            play_audio_btn.setImageResource(R.drawable.icon_pause);
        }

        audio_player_data.getChannelList().get(audio_player_data.getLongestChannelIndex()).setPlayerCompletionListener(() -> play_audio_btn.setImageResource(R.drawable.icon_play));
    }

    // Button actions
    private void addAudio() {
        DispatchMethods.sendPopup(add_audio_popup, new Fade(), true);
    }

    private void openFromAudioFile() {
        select_audio_file.launch("audio/*");
    }

    private void openFromVideoFile() {
        extract_audio_from_video.launch("video/*");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AudioPlayerData.getInstance().endPlayers();

        seekbar.removeWaveform(-1);
    }

    @Override
    public void onEditorTrayItemClick(int position) {
        EditorTrayItemInfo item = RecyclerViewItems.getEditorTrayItemList().get(position);
        EditorTrayId editor_tray_id = item.getId();

        switch (editor_tray_id) {
            case DELETE:
                int selected_waveform_index = seekbar.getSelectedViewIndex();
                if (selected_waveform_index >= 0) {
                    AudioPlayerData.getInstance().removeTrack(0, selected_waveform_index);
                    seekbar.removeWaveform(selected_waveform_index);
                }
                break;
        }
    }
}
