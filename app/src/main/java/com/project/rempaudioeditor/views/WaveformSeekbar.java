package com.project.rempaudioeditor.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.rempaudioeditor.AudioPlayerData;
import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.converters.UnitConverter;
import com.project.rempaudioeditor.infos.AudioInfo;

import java.util.ArrayList;


public class WaveformSeekbar extends HorizontalScrollView {

    private AudioPlayerData audio_player_data;
    private MediaPlayer media_player;
    private final Handler seekHandler = new Handler();

    private int barColor = Color.BLACK;
    private int pinColor = Color.MAGENTA;
    private Drawable waveform_background;
    private EditText currentPosView;

    private int total_duration;
    private ArrayList<AudioInfo> audio_tracks;

    private int flingPreviousPosition;

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
            TypedArray typedArray = getContext().obtainStyledAttributes(attrsSet, R.styleable.WaveformSeekbar);

            barColor = typedArray.getColor(R.styleable.WaveformSeekbar_waveBarColor, Color.BLACK);
            waveform_background = typedArray.getDrawable(R.styleable.WaveformSeekbar_waveBackground);
            pinColor = typedArray.getColor(R.styleable.WaveformSeekbar_pinColor, Color.MAGENTA);

            typedArray.recycle();
        }
        setHorizontalScrollBarEnabled(false);
    }

    public void connectMediaPlayer(@NonNull AudioPlayerData audio_player, @NonNull ArrayList<AudioInfo> audio_tracks, @Nullable EditText currentPosView) {
        this.audio_player_data = audio_player;
        this.media_player = audio_player.getPlayer();
        this.audio_tracks = audio_tracks;

        setTotalDuration(audio_player.getPlayerTotalDuration());

        updateWaveformSeekbar();

        this.currentPosView = currentPosView;
        if (currentPosView != null) {
            currentPosView.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if ((media_player != null) && (!audio_player.getReleased())) {
                        int edit_text_position = (int) UnitConverter.formattedTimeToMilisec(v.getText().toString());
                        if ((edit_text_position < audio_player.getCurrentAudioTrackStart()) || (edit_text_position > audio_player.getCurrentAudioTrackEnd())) {
                            int audio_duration = 0;
                            for (int i = 0; i < audio_tracks.size(); i++) {
                                AudioInfo currentTrackInfo = audio_tracks.get(i);
                                audio_duration += currentTrackInfo.getUriDuration();
                                if (edit_text_position < audio_duration) {
                                    audio_player.setCurrentAudioTrackIndex(i);
                                    media_player.reset();
                                    audio_player.startPlayer(getContext());
                                    media_player.seekTo(edit_text_position - audio_player.getCurrentAudioTrackStart());
                                    break;
                                }
                            }
                        } else {
                            media_player.seekTo(edit_text_position - audio_player.getCurrentAudioTrackStart());
                            audio_player.resumePlayer();
                        }
                    }
                    v.clearFocus();
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

        WaveForm waveForm = audioTrack.getWaveForm();
        child_layout.addView(waveForm);
        waveForm.setBarColor(barColor);
        waveForm.setBackground(waveform_background);
    }

    public void setTotalDuration(int total_duration) {
        this.total_duration = total_duration;
    }

    private void updateWaveformSeekbar() {
        if ((!audio_player_data.getReleased()) && (media_player.isPlaying())) {
            if (currentPosView != null && !currentPosView.hasFocus()) {
                currentPosView.setText(UnitConverter.formatMilisec((media_player.getCurrentPosition() + audio_player_data.getCurrentAudioTrackStart())));
            }
            setWaveformProgress(media_player.getCurrentPosition());
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

        if ((ratio * total_duration < audio_player_data.getCurrentAudioTrackStart()) || (ratio * total_duration > audio_player_data.getCurrentAudioTrackEnd())) {
            int audio_duration = 0;
            for (int i = 0; i < audio_tracks.size(); i++) {
                AudioInfo currentTrackInfo = audio_tracks.get(i);
                audio_duration += currentTrackInfo.getUriDuration();
                if (ratio * total_duration < audio_duration) {
                    audio_player_data.setCurrentAudioTrackIndex(i);
                    media_player.reset();
                    audio_player_data.startPlayer(getContext());
                    media_player.seekTo((int) ((ratio * total_duration) - audio_player_data.getCurrentAudioTrackStart()));
                    break;
                }
            }
        } else {
            media_player.seekTo((int) ((ratio * total_duration) - audio_player_data.getCurrentAudioTrackStart()));
            audio_player_data.resumePlayer();
        }

        if (currentPosView != null && !currentPosView.hasFocus()) {
            currentPosView.setText(UnitConverter.formatMilisec((int) ((ratio * total_duration) - audio_player_data.getCurrentAudioTrackStart())));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (audio_player_data != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if ((!audio_player_data.getReleased()) && (media_player.isPlaying()))
                        audio_player_data.pausePlayer();
                    break;
                case MotionEvent.ACTION_UP:
                    if ((!audio_player_data.getReleased()) && (!media_player.isPlaying())) {
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

        if ((!audio_player_data.getReleased()) && (media_player.isPlaying()))
            audio_player_data.pausePlayer();

        checkFling();
    }

    private void checkFling() {
        int position = getScrollX();
        if (flingPreviousPosition - position == 0) {
            if ((media_player != null) && (!audio_player_data.getReleased())) {
                if ((!audio_player_data.getReleased()) && (!media_player.isPlaying())) {
                    seekPlayer();
                }
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
}
