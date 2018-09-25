package com.xulaoyao.ez_video_player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.xulaoyao.ez_video_player.listener.EzVideoPlayerControlListener;
import com.xulaoyao.ez_video_player.listener.SimplePlayerCallback;
import com.xulaoyao.ez_video_player.model.EzVideoInfo;
import com.xulaoyao.ez_video_player.player.SystemMediaPlayer;
import com.xulaoyao.ez_video_player.util.NetworkUtils;
import com.xulaoyao.ez_video_player.view.EzVideoPlayerBehaviorView;
import com.xulaoyao.ez_video_player.view.EzVideoPlayerControllerView;
import com.xulaoyao.ez_video_player.view.EzVideoPlayerProgressOverlay;
import com.xulaoyao.ez_video_player.view.EzVideoPlayerSystemOverlay;

/**
 * EzVideoView
 * Created by renwoxing on 2018/1/22.
 */
public class EzVideoPlayerView extends EzVideoPlayerBehaviorView {

    private SurfaceView mSurfaceView;
    private View mLoading;
    private EzVideoPlayerControllerView mediaController;
    private EzVideoPlayerSystemOverlay videoSystemOverlay;
    private EzVideoPlayerProgressOverlay videoProgressOverlay;
    private EzVideoPlayer mMediaPlayer;

    private int initWidth;
    private int initHeight;
    private boolean isBackgroundPause;
    private NetChangedReceiver netChangedReceiver;

    public EzVideoPlayerView(Context context) {
        super(context);
        init();
    }

    public EzVideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EzVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public boolean isLock() {
        return mediaController.isLock();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.ez_video_player_view, this);

        mSurfaceView = (SurfaceView) findViewById(R.id.ez_video_player_surface);
        mLoading = findViewById(R.id.ez_video_player_loading);
        mediaController = (EzVideoPlayerControllerView) findViewById(R.id.ez_video_player_controller);
        videoSystemOverlay = (EzVideoPlayerSystemOverlay) findViewById(R.id.ez_video_player_system_overlay);
        videoProgressOverlay = (EzVideoPlayerProgressOverlay) findViewById(R.id.ez_video_player_progress_overlay);

        initPlayer();

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initWidth = getWidth();
                initHeight = getHeight();

                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(holder);
                    mMediaPlayer.openVideo();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        registerNetChangedReceiver();
    }

    private void initPlayer() {
        mMediaPlayer = new SystemMediaPlayer();
        mMediaPlayer.setCallback(new SimplePlayerCallback() {

            @Override
            public void onStateChanged(int curState) {
                switch (curState) {
                    case EzVideoPlayer.STATE_IDLE:
                        am.abandonAudioFocus(null);
                        break;
                    case EzVideoPlayer.STATE_PREPARING:
                        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        break;
                }
            }

            @Override
            public void onCompletion() {
                mediaController.updatePausePlay();
            }

            @Override
            public void onError(int what, int extra) {
                mediaController.checkShowError(false);
            }

            @Override
            public void onLoadingChanged(boolean isShow) {
                if (isShow) showLoading();
                else hideLoading();
            }

            @Override
            public void onPrepared() {
                mMediaPlayer.start();
                mediaController.show();
                mediaController.hideErrorView();
            }
        });
        mediaController.setMediaPlayer(mMediaPlayer);
    }

    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mLoading.setVisibility(View.GONE);
    }

    public void onStop() {
        if (mMediaPlayer.isPlaying()) {
            // 如果已经开始且在播放，则暂停同时记录状态
            isBackgroundPause = true;
            mMediaPlayer.pause();
        }
    }

    public void onStart() {
        if (isBackgroundPause) {
            // 如果切换到后台暂停，后又切回来，则继续播放
            isBackgroundPause = false;
            mMediaPlayer.start();
        }
    }

    public void onDestroy() {
        mMediaPlayer.stop();
        mediaController.release();
        unRegisterNetChangedReceiver();
    }

    /**
     * 开始播放
     */
    public void startPlayVideo(final EzVideoInfo video) {
        if (video == null) {
            return;
        }

        mMediaPlayer.reset();
//        String videoPath = video.getVideoPath();
//        mediaController.setVideoInfo(video);
//        mMediaPlayer.setVideoPath(videoPath);

        mediaController.setVideoInfo(video);
        mMediaPlayer.setDataSource(video);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mediaController.toggleDisplay();
        return super.onSingleTapUp(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (isLock()) {
            return false;
        }
        return super.onDown(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isLock()) {
            return false;
        }
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    protected void endGesture(int behaviorType) {
        switch (behaviorType) {
            case EzVideoPlayerBehaviorView.FINGER_BEHAVIOR_BRIGHTNESS:
            case EzVideoPlayerBehaviorView.FINGER_BEHAVIOR_VOLUME:
                Log.i("DDD", "endGesture: left right");
                videoSystemOverlay.hide();
                break;
            case EzVideoPlayerBehaviorView.FINGER_BEHAVIOR_PROGRESS:
                Log.i("DDD", "endGesture: bottom");
                mMediaPlayer.seekTo(videoProgressOverlay.getTargetProgress());
                videoProgressOverlay.hide();
                break;
        }
    }

    @Override
    protected void updateSeekUI(int delProgress) {
        videoProgressOverlay.show(delProgress, mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
    }

    @Override
    protected void updateVolumeUI(int max, int progress) {
        videoSystemOverlay.show(EzVideoPlayerSystemOverlay.SystemType.VOLUME, max, progress);
    }

    @Override
    protected void updateLightUI(int max, int progress) {
        videoSystemOverlay.show(EzVideoPlayerSystemOverlay.SystemType.BRIGHTNESS, max, progress);
    }

    public void setVideoControlListener(EzVideoPlayerControlListener onVideoControlListener) {
        mediaController.setEzVideoPlayerControlListener(onVideoControlListener);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getLayoutParams().width = initWidth;
            getLayoutParams().height = initHeight;
        } else {
            getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
            getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
        }

    }

    public void registerNetChangedReceiver() {
        if (netChangedReceiver == null) {
            netChangedReceiver = new NetChangedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            activity.registerReceiver(netChangedReceiver, filter);
        }
    }

    public void unRegisterNetChangedReceiver() {
        if (netChangedReceiver != null) {
            activity.unregisterReceiver(netChangedReceiver);
        }
    }

    private class NetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Parcelable extra = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (extra != null && extra instanceof NetworkInfo) {
                NetworkInfo netInfo = (NetworkInfo) extra;

                if (NetworkUtils.isNetworkConnected(context) && netInfo.getState() != NetworkInfo.State.CONNECTED) {
                    // 网络连接的情况下只处理连接完成状态
                    return;
                }

                mediaController.checkShowError(true);
            }
        }
    }
}
