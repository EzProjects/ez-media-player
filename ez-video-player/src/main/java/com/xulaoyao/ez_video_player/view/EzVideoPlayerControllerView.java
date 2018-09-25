package com.xulaoyao.ez_video_player.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xulaoyao.ez_video_player.EzVideoPlayer;
import com.xulaoyao.ez_video_player.R;
import com.xulaoyao.ez_video_player.listener.EzVideoPlayerControlListener;
import com.xulaoyao.ez_video_player.listener.SimpleOnVideoControlListener;
import com.xulaoyao.ez_video_player.model.EzVideoInfo;
import com.xulaoyao.ez_video_player.util.DisplayUtils;
import com.xulaoyao.ez_video_player.util.NetworkUtils;
import com.xulaoyao.ez_video_player.util.StringUtils;

/**
 * 视频控制器，可替换或自定义样式
 */
public class EzVideoPlayerControllerView extends FrameLayout {

    public static final int DEFAULT_SHOW_TIME = 3000;

    private View mControllerBack;
    private View mControllerTitle;
    private TextView mVideoTitle;
    private View mControllerBottom;
    private SeekBar mPlayerSeekBar;
    private ImageView mVideoPlayState;
    private TextView mVideoProgress;
    private TextView mVideoDuration;
    private ImageView mVideoFullScreen;
    private ImageView mScreenLock;
    private EzVideoPlayerErrorView mErrorView;

    private boolean isScreenLock;
    private boolean mShowing;
    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private boolean mAllowUnWifiPlay;
    private EzVideoPlayer mPlayer;
    private EzVideoInfo videoInfo;
    private EzVideoPlayerControlListener ezVideoPlayerControlListener;
    private boolean mDragging;
    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && mPlayer.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };
    private long mDraggingProgress;
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            removeCallbacks(mShowProgress);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }

            long duration = mPlayer.getDuration();
            mDraggingProgress = (duration * progress) / 1000L;

            if (mVideoProgress != null) {
                mVideoProgress.setText(StringUtils.stringForTime((int) mDraggingProgress));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mPlayer.seekTo((int) mDraggingProgress);
            play();
            mDragging = false;
            mDraggingProgress = 0;

            post(mShowProgress);
        }
    };
    private OnClickListener mOnPlayerPauseClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            doPauseResume();
        }
    };

    public EzVideoPlayerControllerView(Context context) {
        super(context);
        init();
    }

    public EzVideoPlayerControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EzVideoPlayerControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setEzVideoPlayerControlListener(EzVideoPlayerControlListener ezVideoPlayerControlListener) {
        this.ezVideoPlayerControlListener = ezVideoPlayerControlListener;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.ez_video_player_media_controller, this);

        initControllerPanel();
    }

    private void initControllerPanel() {
        // back
        mControllerBack = findViewById(R.id.video_back);
        mControllerBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ezVideoPlayerControlListener != null) {
                    ezVideoPlayerControlListener.onBack();
                }
            }
        });
        // top
        mControllerTitle = findViewById(R.id.video_controller_title);
        mVideoTitle = (TextView) mControllerTitle.findViewById(R.id.video_title);
        // bottom
        mControllerBottom = findViewById(R.id.video_controller_bottom);
        mPlayerSeekBar = (SeekBar) mControllerBottom.findViewById(R.id.player_seek_bar);
        mVideoPlayState = (ImageView) mControllerBottom.findViewById(R.id.player_pause);
        mVideoProgress = (TextView) mControllerBottom.findViewById(R.id.player_progress);
        mVideoDuration = (TextView) mControllerBottom.findViewById(R.id.player_duration);
        mVideoFullScreen = (ImageView) mControllerBottom.findViewById(R.id.video_full_screen);
        mVideoPlayState.setOnClickListener(mOnPlayerPauseClick);
        mVideoPlayState.setImageResource(R.drawable.ico_ez_video_player_pause);
        mPlayerSeekBar.setOnSeekBarChangeListener(mSeekListener);
        mVideoFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ezVideoPlayerControlListener != null) {
                    ezVideoPlayerControlListener.onFullScreen();
                }
            }
        });

        // lock
        mScreenLock = (ImageView) findViewById(R.id.player_lock_screen);
        mScreenLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScreenLock) unlock();
                else lock();
                show();
            }
        });

        // error
        mErrorView = (EzVideoPlayerErrorView) findViewById(R.id.video_controller_error);
        mErrorView.setEzVideoPlayerControlListener(new SimpleOnVideoControlListener() {
            @Override
            public void onRetry(int errorStatus) {
                retry(errorStatus);
            }
        });

        mPlayerSeekBar.setMax(1000);
    }

    public void setMediaPlayer(EzVideoPlayer player) {
        mPlayer = player;
        updatePausePlay();
    }

    public void setVideoInfo(EzVideoInfo videoInfo) {
        this.videoInfo = videoInfo;
        mVideoTitle.setText(videoInfo.getTitle());
    }

    public void toggleDisplay() {
        if (mShowing) {
            hide();
        } else {
            show();
        }
    }

    public void show() {
        show(DEFAULT_SHOW_TIME);
    }

    public void show(int timeout) {
        setProgress();

        if (!isScreenLock) {
            mControllerBack.setVisibility(VISIBLE);
            mControllerTitle.setVisibility(VISIBLE);
            mControllerBottom.setVisibility(VISIBLE);
        } else {
            if (!DisplayUtils.isPortrait(getContext())) {
                mControllerBack.setVisibility(GONE);
            }
            mControllerTitle.setVisibility(GONE);
            mControllerBottom.setVisibility(GONE);
        }

        if (!DisplayUtils.isPortrait(getContext())) {
            mScreenLock.setVisibility(VISIBLE);   //横屏时显示
        }

        mShowing = true;

        updatePausePlay();

        post(mShowProgress);

        if (timeout > 0) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    private void hide() {
        if (!mShowing) {
            return;
        }

        if (!DisplayUtils.isPortrait(getContext())) {
            // 横屏才消失
            mControllerBack.setVisibility(GONE);
        }
        mControllerTitle.setVisibility(GONE);
        mControllerBottom.setVisibility(GONE);
        mScreenLock.setVisibility(GONE);


        removeCallbacks(mShowProgress);

        mShowing = false;
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mPlayerSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mPlayerSeekBar.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mPlayerSeekBar.setSecondaryProgress(percent * 10);
        }

        mVideoProgress.setText(StringUtils.stringForTime(position));
        mVideoDuration.setText(StringUtils.stringForTime(duration));

        return position;
    }

    /**
     * 判断显示错误类型
     */
    public void checkShowError(boolean isNetChanged) {
        boolean isConnect = NetworkUtils.isNetworkConnected(getContext());
        boolean isMobileNet = NetworkUtils.isMobileConnected(getContext());
        boolean isWifiNet = NetworkUtils.isWifiConnected(getContext());

        if (isConnect) {
            // 如果已经联网
            if (mErrorView.getCurStatus() == EzVideoPlayerErrorView.STATUS_NO_NETWORK_ERROR && !(isMobileNet && !isWifiNet)) {
                // 如果之前是无网络 TODO 应该提示“网络已经重新连上，请重试”，这里暂不处理
            } else if (videoInfo == null) {
                // 优先判断是否有video数据
                showError(EzVideoPlayerErrorView.STATUS_VIDEO_DETAIL_ERROR);
            } else if (isMobileNet && !isWifiNet && !mAllowUnWifiPlay) {
                // 如果是手机流量，且未同意过播放，且非本地视频，则提示错误
                mErrorView.showError(EzVideoPlayerErrorView.STATUS_UN_WIFI_ERROR);
                mPlayer.pause();
            } else if (isWifiNet && isNetChanged && mErrorView.getCurStatus() == EzVideoPlayerErrorView.STATUS_UN_WIFI_ERROR) {
                // 如果是wifi流量，且之前是非wifi错误，则恢复播放
                playFromUnWifiError();
            } else if (!isNetChanged) {
                showError(EzVideoPlayerErrorView.STATUS_VIDEO_SRC_ERROR);
            }
        } else {
            mPlayer.pause();
            showError(EzVideoPlayerErrorView.STATUS_NO_NETWORK_ERROR);
        }
    }

    public void hideErrorView() {
        mErrorView.hideError();
    }

    private void reload() {
        mPlayer.restart();
    }

    public void release() {
        removeCallbacks(mShowProgress);
        removeCallbacks(mFadeOut);
    }

    private void retry(int status) {
        Log.i("DDD", "retry " + status);

        switch (status) {
            case EzVideoPlayerErrorView.STATUS_VIDEO_DETAIL_ERROR:
                // 传递给activity
                if (ezVideoPlayerControlListener != null) {
                    ezVideoPlayerControlListener.onRetry(status);
                }
                break;
            case EzVideoPlayerErrorView.STATUS_VIDEO_SRC_ERROR:
                reload();
                break;
            case EzVideoPlayerErrorView.STATUS_UN_WIFI_ERROR:
                allowUnWifiPlay();
                break;
            case EzVideoPlayerErrorView.STATUS_NO_NETWORK_ERROR:
                // 无网络时
                if (NetworkUtils.isNetworkConnected(getContext())) {
                    if (videoInfo == null) {
                        // 如果video为空，重新请求详情
                        retry(EzVideoPlayerErrorView.STATUS_VIDEO_DETAIL_ERROR);
                    } else if (mPlayer.isInPlaybackState()) {
                        // 如果有video，可以直接播放的直接恢复
                        mPlayer.start();
                    } else {
                        // 视频未准备好，重新加载
                        reload();
                    }
                } else {
                    Toast.makeText(getContext(), "网络未连接", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showError(int status) {
        mErrorView.showError(status);
        hide();

        // 如果提示了错误，则看需要解锁
        if (isScreenLock) {
            unlock();
        }
    }

    public boolean isLock() {
        return isScreenLock;
    }

    private void lock() {
        Log.i("DDD", "lock");
        isScreenLock = true;
        mScreenLock.setImageResource(R.drawable.ico_ez_video_player_locked);
    }

    private void unlock() {
        Log.i("DDD", "unlock");
        isScreenLock = false;
        mScreenLock.setImageResource(R.drawable.ico_ez_video_player_unlock);
    }

    private void allowUnWifiPlay() {
        Log.i("DDD", "allowUnWifiPlay");

        mAllowUnWifiPlay = true;

        playFromUnWifiError();
    }

    private void playFromUnWifiError() {
        Log.i("DDD", "playFromUnWifiError");

        // TODO: 2017/6/19 check me
        if (mPlayer.isInPlaybackState()) {
            mPlayer.start();
        } else {
            mPlayer.restart();
        }
    }

    public void updatePausePlay() {
        if (mPlayer.isPlaying()) {
            mVideoPlayState.setImageResource(R.drawable.ico_ez_video_player_pause);
        } else {
            mVideoPlayState.setImageResource(R.drawable.ico_ez_video_player_play);
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void pause() {
        mPlayer.pause();
        updatePausePlay();
        removeCallbacks(mFadeOut);
    }

    private void play() {
        mPlayer.start();
        show();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleVideoLayoutParams();
    }

    void toggleVideoLayoutParams() {
        final boolean isPortrait = DisplayUtils.isPortrait(getContext());

        if (isPortrait) {
            //竖屏
            mControllerBack.setVisibility(VISIBLE);
            mVideoFullScreen.setVisibility(View.VISIBLE);
            mScreenLock.setVisibility(GONE);

            //竖屏时 title 消失
            mVideoTitle.setVisibility(GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mControllerBack.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ez_video_player_oval_white_stroke));
            } else {
                mControllerBack.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ez_video_player_oval_white_stroke));
            }
        } else {
            //横屏
            mVideoFullScreen.setVisibility(View.GONE);
            if (mShowing) {
                mScreenLock.setVisibility(VISIBLE);
            }
            //标题显示
            mVideoTitle.setVisibility(VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mControllerBack.setBackground(null);
            } else {
                mControllerBack.setBackgroundDrawable(null);
            }
            mControllerBack.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
        }
    }

}
