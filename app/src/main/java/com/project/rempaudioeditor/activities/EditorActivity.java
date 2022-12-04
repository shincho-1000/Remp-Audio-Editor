package com.project.rempaudioeditor.activities;

import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
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
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AudioPlayerData;
import com.project.rempaudioeditor.AppMethods;
import com.project.rempaudioeditor.AudioRecorderData;
import com.project.rempaudioeditor.constants.AppConstants;
import com.project.rempaudioeditor.infos.AudioInfo;
import com.project.rempaudioeditor.recycleradapters.EditorTrayItemAdapter;
import com.project.rempaudioeditor.dispatch.DispatchMethods;
import com.project.rempaudioeditor.infos.EditorTrayItemInfo;
import com.project.rempaudioeditor.constants.RecyclerViewItems;
import com.project.rempaudioeditor.customviews.WaveformSeekbar;

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
                    AudioInfo newTrack = new AudioInfo(this, uri);
                    AudioPlayerData.getInstance().addTrack(newTrack);
                    seekbar.addWaveform(newTrack);
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

        final ImageButton back_btn = findViewById(R.id.back_from_editor_btn);
        back_btn.setOnClickListener(view -> AppMethods.finishActivity(this));

        AudioPlayerData.getInstance().initializePlayer(this, findViewById(R.id.editor_fft_visualizer), findViewById(R.id.editor_waveform_seeker), findViewById(R.id.durationText), findViewById(R.id.totalDurationText));
        // TODO: window leak here
        new Handler().postDelayed(() -> {
            PopupWindow loading_popup_window = DispatchMethods.sendPopup(loading_popup, new Fade(), false);
            AudioPlayerData.getInstance().setPlayerInitializedListener(() -> {
                runOnUiThread(loading_popup_window::dismiss);
            });
        }, AppConstants.getPopupSendDelayMilisec());
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
        MediaPlayer audio_player = audio_player_data.getPlayer();
        if (audio_player != null) {
            if (audio_player.isPlaying()) {
                audio_player_data.pausePlayer();
                play_audio_btn.setImageResource(R.drawable.icon_play);
            } else {
                audio_player_data.resumePlayer();
                play_audio_btn.setImageResource(R.drawable.icon_pause);
            }
        } else {
            audio_player_data.startPlayer(this);
            play_audio_btn.setImageResource(R.drawable.icon_pause);

            audio_player = audio_player_data.getPlayer();
            audio_player.setOnCompletionListener(mp -> play_audio_btn.setImageResource(R.drawable.icon_play));
        }
    }

    // Button actions
    private void addAudio() {
        DispatchMethods.sendPopup(add_audio_popup, new Fade(), true);
    }

    private void openFromAudioFile() {
        select_audio_file.launch("audio/*");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AudioPlayerData.getInstance().endPlayer();

        seekbar.removeWaveform(-1);
    }

    @Override
    public void onEditorTrayItemClick(int position) {

    }
}
