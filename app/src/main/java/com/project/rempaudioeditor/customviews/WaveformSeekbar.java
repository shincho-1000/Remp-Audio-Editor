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
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.AudioPlayerData;
import com.project.rempaudioeditor.infos.AudioInfo;
import com.project.rempaudioeditor.infos.ChannelInfo;
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
    private int selected_channel_index = -1;

    private int total_duration;
    private ArrayList<ChannelInfo> audio_channels;

    private int fling_previous_position;

    private TextView total_duration_view;

    private OnWaveFormsInitializedListener waveforms_initialized_listener;
    private OnWaveFormAddedListener waveform_added_listener;

    private OnSeekHoldListener holdListener;

    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.f;

    private final int CHANNEL_SPACE = (int) UnitConverter.convertDpToPx(getContext(), 2);

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
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        setHorizontalScrollBarEnabled(false);
    }

    public void connectMediaPlayer(@NonNull Context context, @Nullable EditText current_position_view, @Nullable TextView total_duration_view) {
        this.audio_player_data = AudioPlayerData.getInstance();
        this.audio_channels = audio_player_data.getChannelList();

        Thread conversion = new Thread(() -> {
            for (int i = 0; i < audio_channels.size(); i++) {
                ChannelInfo audio_channel = audio_channels.get(i);
                for (AudioInfo audioInfo : audio_channel.getTrackList()) {
                    addWaveform(i, audioInfo);
                }
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
                    for (ChannelInfo channelInfo : audio_channels) {
                        if ((channelInfo.getPlayer() != null) && (!channelInfo.getReleased())) {
                            int edit_text_position = (int) UnitConverter
                                    .toMilisec(view.getText().toString());
                            if ((edit_text_position < channelInfo.getCurrentAudioTrackStart())
                                    || (edit_text_position > channelInfo.getCurrentAudioTrackEnd())) {
                                int audio_duration = 0;
                                for (int i = 0; i < channelInfo.getTrackList().size(); i++) {
                                    AudioInfo currentTrackInfo = channelInfo.getTrackList().get(i);
                                    audio_duration += currentTrackInfo.getUriDuration();
                                    if (edit_text_position < audio_duration) {
                                        channelInfo.setCurrentAudioTrackIndex(i);
                                        channelInfo.getPlayer().reset();
                                        channelInfo.startPlayer(getContext());
                                        channelInfo.getPlayer()
                                                .seekTo(edit_text_position - channelInfo.getCurrentAudioTrackStart());
                                        break;
                                    }
                                }
                            } else {
                                channelInfo.getPlayer().seekTo(edit_text_position - channelInfo.getCurrentAudioTrackStart());
                                channelInfo.resumePlayer();
                            }
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
            container_layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            container_layout.setOrientation(LinearLayout.VERTICAL);
            addView(container_layout);
        }

        return container_layout;
    }

    public LinearLayout getChannelLayout(int channel_index) {
        LinearLayout container_layout = getContainer();
        LinearLayout channel_layout;

        if (container_layout.getChildCount() > channel_index) {
            channel_layout = (LinearLayout) container_layout.getChildAt(channel_index);
        } else if (container_layout.getChildCount() == channel_index) {
            channel_layout = new LinearLayout(getContext());
            container_layout.addView(channel_layout);
        } else {
            return null;
        }

        return channel_layout;
    }



    private void setTotalDuration(@NonNull TextView total_duration_view) {
        total_duration = audio_player_data.getPlayerTotalDuration();
        total_duration_view.setText(UnitConverter.format(total_duration));
    }

    public void addWaveform(int channel_index, AudioInfo audioTrack) {
        WaveForm waveForm = audioTrack.generateWaveform(getContext());
        waveForm.setScaleFactor(scaleFactor);
        ((Activity) getContext()).runOnUiThread(() -> {
            LinearLayout channel_layout = getChannelLayout(channel_index);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (getMeasuredHeight()-(CHANNEL_SPACE*4)) / 4);
            layoutParams.setMargins(0, 0, 0, CHANNEL_SPACE);
            channel_layout.setLayoutParams(layoutParams);
            channel_layout.addView(waveForm);

            waveForm.setBarColor(bar_color);

            Drawable waveform_background_clone = waveform_background.getConstantState().newDrawable();
            waveForm.setBackground(waveform_background_clone);

            waveForm.setOnClickListener(v -> {
                if ((selected_channel_index >= 0) && (selected_view_index >= 0)) {
                    ((LinearLayout) getContainer().getChildAt(selected_channel_index)).getChildAt(selected_view_index).setSelected(false);
                }

                if ((getContainer().indexOfChild(channel_layout) == selected_channel_index)
                        && (channel_layout.indexOfChild(waveForm) == selected_view_index)) {
                    waveForm.setSelected(false);
                    selected_channel_index = -1;
                    selected_view_index = -1;
                } else {
                    waveForm.setSelected(true);
                    selected_view_index = channel_layout.indexOfChild(waveForm);
                    selected_channel_index = getContainer().indexOfChild(channel_layout);
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
        ChannelInfo channelInfo = audio_player_data.getChannelList().get(audio_player_data.getLongestChannelIndex());
        if ((!channelInfo.getReleased()) && (channelInfo.getPlayer().isPlaying())) {
            setWaveformProgress(channelInfo.getPlayer().getCurrentPosition());
        }

        if (current_position_view != null && !current_position_view.hasFocus()) {
            double progress_ratio = (double) getScrollX() / getContainer().getWidth();
            double absolute_progress = progress_ratio * total_duration;
            current_position_view.setText(UnitConverter
                    .format(Math.round(absolute_progress)));
        }

        seekHandler.postDelayed(waveform_updater, 50);
    }

    public void setWaveformProgress(long position_milisec) {
        if (getChildCount() > 0) {
            ChannelInfo channelInfo = audio_player_data.getChannelList().get(audio_player_data.getLongestChannelIndex());
            double ratio = (double) (position_milisec + channelInfo.getCurrentAudioTrackStart()) / total_duration;
            smoothScrollTo((int) (ratio * getChildAt(0).getWidth()), 0);
        }
    }

    public void seekPlayer() {
        double progress_ratio = (double) getScrollX() / getChildAt(0).getWidth();
        double absolute_progress = progress_ratio * total_duration;

        for (ChannelInfo channelInfo : audio_channels) {
            if ((absolute_progress < channelInfo.getCurrentAudioTrackStart())
                    || (absolute_progress > channelInfo.getCurrentAudioTrackEnd())
                    || (channelInfo.getReleased())) {
                int audio_duration_iterator = 0;
                for (int i = 0; i < channelInfo.getTrackList().size(); i++) {
                    AudioInfo currentTrackInfo = channelInfo.getTrackList().get(i);
                    audio_duration_iterator += currentTrackInfo.getUriDuration();
                    if (absolute_progress < audio_duration_iterator) {
                        channelInfo.setCurrentAudioTrackIndex(i);
                        if (!channelInfo.getReleased())
                            channelInfo.getPlayer().reset();
                        channelInfo.startPlayer(getContext().getApplicationContext());
                        channelInfo.pausePlayer();
                        channelInfo.getPlayer().seekTo((int) ((absolute_progress) - channelInfo.getCurrentAudioTrackStart()));
                        break;
                    }

                    if (i == channelInfo.getTrackList().size() - 1) {
                        channelInfo.releasePlayer();
                    }
                }
            } else {
                channelInfo.getPlayer().seekTo((int) ((absolute_progress) - channelInfo.getCurrentAudioTrackStart()));
            }
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
        if (ev.getPointerCount() > 1)
            scaleDetector.onTouchEvent(ev);

        if (audio_player_data != null) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                audio_player_data.pausePlayers();
                holdListener.onHold();
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.2f, Math.min(1.f, scaleFactor));

            // TODO: scaling here
            for (int i = 0; i < audio_channels.size(); i++) {
                ChannelInfo channelInfo = audio_channels.get(i);
                ArrayList<AudioInfo> trackList = channelInfo.getTrackList();
                for (int j = 0; j < trackList.size(); j++) {
                    ((WaveForm) getChannelLayout(i).getChildAt(j)).setScaleFactor(scaleFactor);
                    getChannelLayout(i).getChildAt(j).requestLayout();
                }
            }

            invalidate();
            return true;
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

        pin.left = ((getWidth() - PIN_WIDTH) / 2) + getScrollX();
        pin.right = pin.left + PIN_WIDTH;
        pin.top = getContainer().getTop();
        pin.bottom = getContainer().getBottom() - CHANNEL_SPACE;

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
