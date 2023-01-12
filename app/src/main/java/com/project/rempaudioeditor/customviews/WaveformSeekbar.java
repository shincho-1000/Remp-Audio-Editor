package com.project.rempaudioeditor.customviews;

// TODO: recorder and extractor audio are producing smaller waveforms
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AudioPlayerData;
import com.project.rempaudioeditor.infos.AudioInfo;
import com.project.rempaudioeditor.utils.UnitConverter;

import java.util.ArrayList;

public class WaveformSeekbar extends HorizontalScrollView {
    private AudioPlayerData audio_player_data;
    private final Handler seekHandler = new Handler();

    private int bar_color = Color.BLACK;
    private int pin_color = Color.MAGENTA;
    private Drawable waveform_background;
    private EditText current_position_view;

    private int selected_view_index = -1;

    private int total_duration;
    private ArrayList<AudioInfo> audio_tracks;

    private int fling_previous_position;

    private TextView total_duration_view;

    private OnWaveFormsInitializedListener waveforms_initialized_listener;
    private OnWaveFormAddedListener waveform_added_listener;

    private OnSeekHoldListener holdListener;

    public interface OnSeekHoldListener {
        void onHold();
    }

    public void setSeekHoldListener(OnSeekHoldListener eventListener) {
        holdListener = eventListener;
    }

    private final RectF pin = new RectF();
    private final Paint pin_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final float PIN_WIDTH = (float) UnitConverter.convertDpToPx(getContext(), 3);

    Runnable waveform_updater = this::updateWaveformSeekbar;

    Runnable fling_checker = this::checkFling;

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

            bar_color = typedArray.getColor(R.styleable.WaveformSeekbar_waveBarColor, Color.BLACK);
            waveform_background = typedArray.getDrawable(R.styleable.WaveformSeekbar_waveBackground);
            pin_color = typedArray.getColor(R.styleable.WaveformSeekbar_pinColor, Color.MAGENTA);

            typedArray.recycle();
        }
        setHorizontalScrollBarEnabled(false);
    }

    public void connectMediaPlayer(@NonNull Context context, @Nullable EditText current_position_view, @Nullable TextView total_duration_view) {
        this.audio_player_data = AudioPlayerData.getInstance();
        this.audio_tracks = audio_player_data.getTrackList();

        Thread conversion = new Thread(() -> {
            for (int i = 0; i < audio_tracks.size(); i++) {
                AudioInfo audio_track = audio_tracks.get(i);
                addWaveform(audio_track);
            }

            if (waveforms_initialized_listener != null) {
                waveforms_initialized_listener.waveformsInitialized();
            }
        });

        conversion.start();

        if (total_duration_view != null) {
            setTotalDuration(total_duration_view);
            this.total_duration_view = total_duration_view;
        }

        updateWaveformSeekbar();

        this.current_position_view = current_position_view;
        if (current_position_view != null) {
            current_position_view.setOnEditorActionListener((view, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if ((audio_player_data.getPlayer() != null) && (!audio_player_data.getReleased())) {
                        int edit_text_position = (int) UnitConverter
                                .toMilisec(view.getText().toString());
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

    public LinearLayout getContainer() {
        LinearLayout container_layout;

        if (getChildCount() > 0) {
            container_layout = (LinearLayout) getChildAt(0);
        }
        else {
            container_layout = new LinearLayout(getContext());
            addView(container_layout);
        }

        return container_layout;
    }

    private void setTotalDuration(@NonNull TextView total_duration_view) {
        total_duration = audio_player_data.getPlayerTotalDuration();
        total_duration_view.setText(UnitConverter.format(total_duration));
    }

    public void addWaveform(AudioInfo audioTrack) {
        LinearLayout container_layout = getContainer();

        WaveForm waveForm = audioTrack.generateWaveform(getContext());
        ((Activity) getContext()).runOnUiThread(() -> {
            container_layout.addView(waveForm);
            waveForm.setBarColor(bar_color);

            Drawable waveform_background_clone = waveform_background.getConstantState().newDrawable();
            waveForm.setBackground(waveform_background_clone);

            waveForm.setOnClickListener(v -> {
                if (selected_view_index >= 0) {
                    container_layout.getChildAt(selected_view_index).setSelected(false);
                }

                if (container_layout.indexOfChild(waveForm) == selected_view_index) {
                    waveForm.setSelected(false);
                    selected_view_index = -1;
                } else {
                    waveForm.setSelected(true);
                    selected_view_index = container_layout.indexOfChild(waveForm);
                }
            });

            if (total_duration_view != null)
                setTotalDuration(total_duration_view);
        });


        if (waveform_added_listener != null) {
            waveform_added_listener.waveformAdded();
        }
    }

    public void removeWaveform(int waveformIndex) {
        LinearLayout container_layout;

        if (getChildCount() > 0) {
            container_layout = (LinearLayout) getChildAt(0);
            if (waveformIndex >= 0)
                container_layout.removeViewAt(waveformIndex);
            else if (waveformIndex == -1)
                container_layout.removeAllViews();
        }

        if (waveformIndex == selected_view_index) {
            selected_view_index = -1;
        }

        if (total_duration_view != null) {
            setTotalDuration(total_duration_view);
        }
    }

    private void updateWaveformSeekbar() {
        if ((!audio_player_data.getReleased()) && (audio_player_data.getPlayer().isPlaying())) {
            setWaveformProgress(audio_player_data.getPlayer().getCurrentPosition());
        }

        if (current_position_view != null && !current_position_view.hasFocus()) {
            double progress_ratio = (double) getScrollX() / getChildAt(0).getWidth();
            double absolute_progress = progress_ratio * total_duration;
            current_position_view.setText(UnitConverter
                    .format((long) absolute_progress));
        }

        seekHandler.postDelayed(waveform_updater, 50);
    }

    public void setWaveformProgress(long position_milisec) {
        if (getChildCount() > 0) {
            double ratio = (double) (position_milisec + audio_player_data.getCurrentAudioTrackStart()) / total_duration;
            smoothScrollTo((int) (ratio * getChildAt(0).getWidth()), 0);
        }
    }

    public void seekPlayer() {
        double progress_ratio = (double) getScrollX() / getChildAt(0).getWidth();
        double absolute_progress = progress_ratio * total_duration;

        if ((absolute_progress < audio_player_data.getCurrentAudioTrackStart())
                || (absolute_progress > audio_player_data.getCurrentAudioTrackEnd())) {
            int audio_duration_iterator = 0;
            for (int i = 0; i < audio_tracks.size(); i++) {
                AudioInfo currentTrackInfo = audio_tracks.get(i);
                audio_duration_iterator += currentTrackInfo.getUriDuration();
                if (absolute_progress < audio_duration_iterator) {
                    audio_player_data.setCurrentAudioTrackIndex(i);
                    audio_player_data.getPlayer().reset();
                    audio_player_data.startPlayer(getContext().getApplicationContext());
                    audio_player_data.pausePlayer();
                    if (audio_player_data.getPlayer() != null)
                        audio_player_data.getPlayer().seekTo((int) ((absolute_progress) - audio_player_data.getCurrentAudioTrackStart()));
                    break;
                }
            }
        } else {
            audio_player_data.getPlayer().seekTo((int) ((absolute_progress) - audio_player_data.getCurrentAudioTrackStart()));
        }

        if (current_position_view != null && !current_position_view.hasFocus()) {
            current_position_view.setText(UnitConverter.format((int) (absolute_progress)));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        handleTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    public void handleTouchEvent(MotionEvent ev) {
        if (audio_player_data != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if ((!audio_player_data.getReleased()) && (audio_player_data.getPlayer().isPlaying())) {
                        audio_player_data.pausePlayer();
                        holdListener.onHold();
                    }
                    break;
            }
        }
    }

    @Override
    public void fling(int velocityX) {
        super.fling(velocityX);

        checkFling();
    }

    private void checkFling() {
        int current_position = getScrollX();
        if (fling_previous_position - current_position == 0) {

        } else {
            fling_previous_position = getScrollX();
            postDelayed(fling_checker, 50);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        pin.left = ((getWidth() - PIN_WIDTH ) / 2) + getScrollX();
        pin.right = pin.left + PIN_WIDTH;
        pin.top = 0;
        pin.bottom = getHeight();

        pin_paint.setColor(pin_color);
        setPadding(getWidth()/2, 0, getWidth()/2, 0);
        setClipToPadding(false);
        setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);

        canvas.drawRect(pin, pin_paint);
    }

    public int getSelectedViewIndex() {
        return selected_view_index;
    }

    public void setWaveFormsInitializedListener(@NonNull OnWaveFormsInitializedListener event_listener) {
        waveforms_initialized_listener = event_listener;
    }

    public interface OnWaveFormsInitializedListener {
        void waveformsInitialized();
    }

    public void setWaveFormAddedListener(@NonNull OnWaveFormAddedListener event_listener) {
        waveform_added_listener = event_listener;
    }

    public interface OnWaveFormAddedListener {
        void waveformAdded();
    }
}
