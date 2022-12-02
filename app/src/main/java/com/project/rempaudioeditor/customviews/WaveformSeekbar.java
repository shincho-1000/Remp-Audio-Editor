package com.project.rempaudioeditor.customviews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AudioPlayerData;
import com.project.rempaudioeditor.activities.MainActivity;
import com.project.rempaudioeditor.utils.UnitConverter;
import com.project.rempaudioeditor.infos.AudioInfo;

import java.util.ArrayList;

public class WaveformSeekbar extends HorizontalScrollView {
    private AudioPlayerData audio_player_data;
    private final Handler seekHandler = new Handler();

    private int barColor = Color.BLACK;
    private int pinColor = Color.MAGENTA;
    private Drawable waveform_background;
    private EditText currentPosView;

    private int total_duration;
    private ArrayList<AudioInfo> audio_tracks;

    private int flingPreviousPosition;

    private OnWaveFormCreatedListener waveform_created_listener;

    OnSeekHoldListener holdListener;

    public interface OnSeekHoldListener {
        void onHold();
    }

    public void setSeekHoldListener(OnSeekHoldListener eventListener) {
        holdListener = eventListener;
    }

    private final RectF pin = new RectF();
    private final Paint pin_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final float PIN_WIDTH = (float) UnitConverter.convertDpToPx(getContext(), 3);

    Runnable waveformUpdater = this::updateWaveformSeekbar;

    Runnable flingChecker = this::checkFling;

    public WaveformSeekbar(Context context) {
        super(context);
        init(null);
    }

    public WaveformSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WaveformSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrsSet) {
        if (attrsSet != null) {
            TypedArray typedArray = getContext()
                    .obtainStyledAttributes(attrsSet, R.styleable.WaveformSeekbar);

            barColor = typedArray.getColor(R.styleable.WaveformSeekbar_waveBarColor, Color.BLACK);
            waveform_background = typedArray.getDrawable(R.styleable.WaveformSeekbar_waveBackground);
            pinColor = typedArray.getColor(R.styleable.WaveformSeekbar_pinColor, Color.MAGENTA);

            typedArray.recycle();
        }
        setHorizontalScrollBarEnabled(false);
    }

    public void connectMediaPlayer(@NonNull Context context, @Nullable EditText currentPosView, @Nullable TextView totalDurationView) {
        this.audio_player_data = AudioPlayerData.getInstance();
        this.audio_tracks = audio_player_data.getTrackList();

        LinearLayout child_layout;

        if (getChildCount() > 0) {
            child_layout = (LinearLayout) getChildAt(0);
        }
        else {
            child_layout = new LinearLayout(getContext());
            addView(child_layout);
        }

        Thread conversion = new Thread(() -> {
            for (int i = 0; i < audio_tracks.size(); i++) {
                WaveForm waveForm = audio_tracks.get(i).generateWaveform(getContext());
                ((Activity) context).runOnUiThread(() -> {
                    child_layout.addView(waveForm);
                    waveForm.setBarColor(barColor);
                    waveForm.setBackground(waveform_background);
                });
            }

            if (waveform_created_listener != null) {
                waveform_created_listener.waveformCreated();
            }
        });

        conversion.start();

        total_duration = audio_player_data.getPlayerTotalDuration();
        if (totalDurationView != null)
            totalDurationView.setText(UnitConverter.formatMilisec(total_duration));

        updateWaveformSeekbar();

        this.currentPosView = currentPosView;
        if (currentPosView != null) {
            currentPosView.setOnEditorActionListener((view, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if ((audio_player_data.getPlayer() != null) && (!audio_player_data.getReleased())) {
                        int edit_text_position = (int) UnitConverter
                                .formattedTimeToMilisec(view.getText().toString());
                        if ((edit_text_position < audio_player_data.getCurrentAudioTrackStart())
                                || (edit_text_position > audio_player_data.getCurrentAudioTrackEnd())) {
                            int audio_duration = 0;
                            for (int i = 0; i < audio_tracks.size(); i++) {
                                AudioInfo currentTrackInfo = audio_tracks.get(i);
                                audio_duration += currentTrackInfo.getUriDuration();
                                if (edit_text_position < audio_duration) {
                                    audio_player_data.setCurrentAudioTrackIndex(i);
                                    audio_player_data.getPlayer().reset();
                                    audio_player_data.startPlayer(getContext());
                                    audio_player_data.getPlayer()
                                            .seekTo(edit_text_position - audio_player_data.getCurrentAudioTrackStart());
                                    break;
                                }
                            }
                        } else {
                            audio_player_data.getPlayer().seekTo(edit_text_position - audio_player_data.getCurrentAudioTrackStart());
                            audio_player_data.resumePlayer();
                        }
                    }
                    view.clearFocus();
                }
                return false;
            });
        }
    }

    public void addWaveform(AudioInfo audioTrack) {
        LinearLayout child_layout;

        if (getChildCount() > 0) {
            child_layout = (LinearLayout) getChildAt(0);
        }
        else {
            child_layout = new LinearLayout(getContext());
            addView(child_layout);
        }
        WaveForm waveForm = audioTrack.generateWaveform(getContext());
        child_layout.addView(waveForm);
        waveForm.setBarColor(barColor);
        waveForm.setBackground(waveform_background);
    }

    public void removeWaveform(int waveformIndex) {
        LinearLayout child_layout;

        if (getChildCount() > 0) {
            child_layout = (LinearLayout) getChildAt(0);
            if (waveformIndex >= 0)
                child_layout.removeViewAt(waveformIndex);
            else if (waveformIndex == -1)
                child_layout.removeAllViews();
        }
    }

    private void updateWaveformSeekbar() {
        if ((!audio_player_data.getReleased()) && (audio_player_data.getPlayer().isPlaying())) {
            if (currentPosView != null && !currentPosView.hasFocus()) {
                currentPosView.setText(UnitConverter
                        .formatMilisec((audio_player_data.getPlayer().getCurrentPosition() + audio_player_data.getCurrentAudioTrackStart())));
            }
            setWaveformProgress(audio_player_data.getPlayer().getCurrentPosition());
        }

        seekHandler.postDelayed(waveformUpdater, 50);
    }

    public void setWaveformProgress(long milisec_position) {
        if (getChildCount() > 0) {
            double ratio = (double) (milisec_position + audio_player_data.getCurrentAudioTrackStart()) / total_duration;
            smoothScrollTo((int) (ratio * getChildAt(0).getWidth()), 0);
        }
    }

    private void seekPlayer() {
        double ratio = (double) getScrollX() / getChildAt(0).getWidth();

        if ((ratio * total_duration < audio_player_data.getCurrentAudioTrackStart())
                || (ratio * total_duration > audio_player_data.getCurrentAudioTrackEnd())) {
            int audio_duration = 0;
            for (int i = 0; i < audio_tracks.size(); i++) {
                AudioInfo currentTrackInfo = audio_tracks.get(i);
                audio_duration += currentTrackInfo.getUriDuration();
                if (ratio * total_duration < audio_duration) {
                    audio_player_data.setCurrentAudioTrackIndex(i);
                    audio_player_data.getPlayer().reset();
                    audio_player_data.startPlayer(getContext());
                    audio_player_data.getPlayer().seekTo((int) ((ratio * total_duration) - audio_player_data.getCurrentAudioTrackStart()));
                    break;
                }
            }
        } else {
            audio_player_data.getPlayer().seekTo((int) ((ratio * total_duration) - audio_player_data.getCurrentAudioTrackStart()));
        }

        if (currentPosView != null && !currentPosView.hasFocus()) {
            currentPosView.setText(UnitConverter
                    .formatMilisec((int) ((ratio * total_duration) - audio_player_data.getCurrentAudioTrackStart())));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (audio_player_data != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if ((!audio_player_data.getReleased()) && (audio_player_data.getPlayer().isPlaying())) {
                        audio_player_data.pausePlayer();
                        holdListener.onHold();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if ((!audio_player_data.getReleased()) && (!audio_player_data.getPlayer().isPlaying())) {
                        seekPlayer();
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void fling(int velocityX) {
        super.fling(velocityX);

        if ((!audio_player_data.getReleased()) && (audio_player_data.getPlayer().isPlaying()))
            audio_player_data.pausePlayer();

        checkFling();
    }

    private void checkFling() {
        int position = getScrollX();
        if (flingPreviousPosition - position == 0) {
            if ((!audio_player_data.getReleased()) && (!audio_player_data.getPlayer().isPlaying())) {
                seekPlayer();
            }
        } else {
            flingPreviousPosition = getScrollX();
            postDelayed(flingChecker, 50);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        pin.left = ((getWidth() - PIN_WIDTH ) / 2) + getScrollX();
        pin.right = pin.left + PIN_WIDTH;
        pin.top = 0;
        pin.bottom = getHeight();

        pin_paint.setColor(pinColor);
        setPadding(getWidth()/2, 0, getWidth()/2, 0);
        setClipToPadding(false);
        setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);

        canvas.drawRect(pin, pin_paint);
    }

    public void setWaveFormCreatedListener(OnWaveFormCreatedListener event_listener) {
        waveform_created_listener = event_listener;
    }

    public interface OnWaveFormCreatedListener {
        void waveformCreated();
    }
}
