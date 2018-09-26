package com.xulaoyao.ez_video_player.player;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.xulaoyao.ez_video_player.EzVideoPlayer;
import com.xulaoyao.ez_video_player.model.EzVideoInfo;

import static android.media.AudioRecord.STATE_INITIALIZED;
import static android.media.session.PlaybackState.STATE_STOPPED;

/**
 * ExoMediaPlayer
 * Created by renwoxing on 2018/9/25.
 */
public class ExoMediaPlayer extends EzVideoPlayer {

    private final String TAG = "-ExoMediaPlayer-";
    private final Context mAppContext;
    private final DefaultBandwidthMeter mBandwidthMeter;
    private SimpleExoPlayer mInternalPlayer;
    private boolean isPreparing = true;   //Loading
    private boolean isBuffering = false;  // 缓冲
    private boolean isPendingSeek = false; //
    private int mStartPos = -1;

    public ExoMediaPlayer(Context context) {
        mAppContext = context.getApplicationContext();

        RenderersFactory renderersFactory = new DefaultRenderersFactory(mAppContext);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();


        mInternalPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

        // Measures bandwidth during playback. Can be null if not required.
        mBandwidthMeter = new DefaultBandwidthMeter();

    }


//    /**
//     * 初始化player
//     */
//    private void initPlayer() {
//        //1. 创建一个默认的 TrackSelector
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTackSelectionFactory =
//                new AdaptiveTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector =
//                new DefaultTrackSelector(videoTackSelectionFactory);
//        LoadControl loadControl = new DefaultLoadControl();
//        //2.创建ExoPlayer
//        mInternalPlayer = ExoPlayerFactory.newSimpleInstance(mAppContext,trackSelector,loadControl);
//
//    }


    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        surfaceHolder = surfaceView.getHolder();
    }

    @Override
    public void setDataSource(EzVideoInfo info) {
        if (info != null) {
            path = info.getVideoPath();
            Log.d(TAG, "setDataSource: 设置视频路径 -------- path:" + path);
            openVideo();
        }
    }

    @Override
    public void openVideo() {
        //mInternalPlayer.setVideoListener(mVideoListener);
        String data = getVideoPath();
        Uri videoUri = null;

        if (!TextUtils.isEmpty(data)) {
            videoUri = Uri.parse(data);
        }

        if (videoUri == null) {
            return;
        }

        // 添加事件监听
        mInternalPlayer.addListener(new PlayerEventListener());

        mInternalPlayer.setVideoSurfaceHolder(surfaceHolder);
        // Prepare the player with the source.
        isPreparing = true;
        mInternalPlayer.prepare(getMediaSource(videoUri));
        mInternalPlayer.setPlayWhenReady(false);

        //Bundle sourceBundle = BundlePool.obtain();
        //sourceBundle.putSerializable(EventKey.SERIALIZABLE_DATA, dataSource);
        //submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET,sourceBundle);
    }

    @Override
    public int getCurrentPosition() {
        return (int) mInternalPlayer.getCurrentPosition();
    }

    @Override
    public int getBufferPercentage() {
        //return (int) mInternalPlayer.getCurrentPosition();
        return mInternalPlayer.getBufferedPercentage();
    }

    @Override
    public int getDuration() {
        return (int) mInternalPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        if (mInternalPlayer == null)
            return false;
        int state = mInternalPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                return mInternalPlayer.getPlayWhenReady();
            case Player.STATE_IDLE:
            case Player.STATE_ENDED:
            default:
                return false;
        }
    }

    @Override
    public boolean isInPlaybackState() {
        return mInternalPlayer != null
                //&& curState!= STATE_END
                && curState != STATE_ERROR
                && curState != STATE_INITIALIZED
                && curState != STATE_STOPPED;

    }

    @Override
    public void start() {
        mInternalPlayer.setPlayWhenReady(true);
    }

    @Override
    public void start(int msc) {
        mStartPos = msc;
        start();
    }

    @Override
    public void pause() {
        if (isInPlaybackState())
            mInternalPlayer.setPlayWhenReady(false);
    }

    /**
     * restart
     */
    @Override
    public void resume() {
        if (isInPlaybackState())
            mInternalPlayer.setPlayWhenReady(true);
    }

    @Override
    public void seekTo(int msc) {
        if (isInPlaybackState()) {
            isPendingSeek = true;
        }
        mInternalPlayer.seekTo(msc);
        //Bundle bundle = BundlePool.obtain();
        //bundle.putInt(EventKey.INT_DATA, msc);
        //submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_TO, bundle);
    }

    @Override
    public void stop() {
        isPreparing = true;
        isBuffering = false;
        //updateStatus(IPlayer.STATE_STOPPED);
        mInternalPlayer.stop();
    }

    @Override
    public void reset() {
        stop();
    }

    @Override
    public void destroy() {
        isPreparing = true;
        isBuffering = false;
//        updateStatus(IPlayer.STATE_END);
//        mInternalPlayer.removeListener(mEventListener);
//        mInternalPlayer.clearVideoListener(mVideoListener);
        mInternalPlayer.release();
    }


    @SuppressWarnings("unchecked")
    private MediaSource getMediaSource(Uri uri) {
        int contentType = Util.inferContentType(uri);
        DefaultDataSourceFactory dataSourceFactory =
                new DefaultDataSourceFactory(mAppContext,
                        Util.getUserAgent(mAppContext, mAppContext.getPackageName()), mBandwidthMeter);
        switch (contentType) {
            case C.TYPE_DASH:
                DefaultDashChunkSource.Factory factory = new DefaultDashChunkSource.Factory(dataSourceFactory);
                return new DashMediaSource(uri, dataSourceFactory, factory, null, null);
            case C.TYPE_SS:
                DefaultSsChunkSource.Factory ssFactory = new DefaultSsChunkSource.Factory(dataSourceFactory);
                return new SsMediaSource(uri, dataSourceFactory, ssFactory, null, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, dataSourceFactory, null, null);
            case C.TYPE_OTHER:
            default:
                // This is the MediaSource representing the media to be played.
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                return new ExtractorMediaSource(uri,
                        dataSourceFactory, extractorsFactory, null, null);
        }
    }


    /*************** explayer event **************/

    private class PlayerEventListener extends Player.DefaultEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//            if (playbackState == Player.STATE_ENDED) {
//                showControls();
//            }
//            updateButtonVisibilities();


            Log.d(TAG, "-0-0000 ---------  onPlayerStateChanged : playWhenReady = " + playWhenReady
                    + ", playbackState = " + playbackState);

            if (!isPreparing) {
                if (playWhenReady) {
                    //updateStatus(IPlayer.STATE_STARTED);
                    //callback.onStateChanged();
                    //submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_RESUME, null);
                    mInternalPlayer.setPlayWhenReady(true);
                } else {
                    mInternalPlayer.setPlayWhenReady(false);
                    //updateStatus(IPlayer.STATE_PAUSED);
                    //submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PAUSE, null);
                }
            }

            if (isPreparing) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        isPreparing = false;
//                        Format format = mInternalPlayer.getVideoFormat();
//                        Bundle bundle = BundlePool.obtain();
//                        if(format!=null){
//                            bundle.putInt(EventKey.INT_ARG1, format.width);
//                            bundle.putInt(EventKey.INT_ARG2, format.height);
//                        }
//                        updateStatus(IPlayer.STATE_PREPARED);
//                        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PREPARED, bundle);
//                        if(mStartPos > 0){
//                            mInternalPlayer.seekTo(mStartPos);
//                            mStartPos = -1;
//                        }
                        callback.onPrepared();
                        mInternalPlayer.setPlayWhenReady(true);
                        break;
                }
                callback.onLoadingChanged(isPreparing);
            }

            if (isBuffering) {
                switch (playbackState) {
                    case Player.STATE_READY:
                    case Player.STATE_ENDED:
                        long bitrateEstimate = mBandwidthMeter.getBitrateEstimate();
                        Log.d(TAG, "buffer_end, BandWidth : " + bitrateEstimate);
                        isBuffering = false;
//                        Bundle bundle = BundlePool.obtain();
//                        bundle.putLong(EventKey.LONG_DATA, bitrateEstimate);
//                        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_END, bundle);
                        callback.onBufferingUpdate((int) bitrateEstimate);
                        break;
                }
                callback.onLoadingChanged(isBuffering);
            }

            if (isPendingSeek) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        isPendingSeek = false;
                        //submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE, null);
                        break;
                }
            }

            if (!isPreparing) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        long bitrateEstimate = mBandwidthMeter.getBitrateEstimate();
                        Log.d(TAG, "buffer_start, BandWidth : " + bitrateEstimate);
                        isBuffering = true;
                        callback.onBufferingUpdate((int) bitrateEstimate);
//                        Bundle bundle = BundlePool.obtain();
//                        bundle.putLong(EventKey.LONG_DATA, bitrateEstimate);
//                        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_START, bundle);
                        break;
                    case Player.STATE_ENDED:
                        setCurrentState(STATE_PLAYBACK_COMPLETED);
                        callback.onCompletion();
//                        updateStatus(IPlayer.STATE_PLAYBACK_COMPLETE);
//                        submitPlayerEvent(OnPlayerEventListener.PLAYER_EVENT_ON_PLAY_COMPLETE, null);
                        break;
                }
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (mInternalPlayer.getPlaybackError() != null) {
                // The user has performed a seek whilst in the error state. Update the resume position so
                // that if the user then retries, playback resumes from the position to which they seeked.
                //updateStartPosition();
            }
            Log.d(TAG, "onPositionDiscontinuity: --------- DiscontinuityReason:" + reason);
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            super.onLoadingChanged(isLoading);
            int bufferPercentage = mInternalPlayer.getBufferedPercentage();
            if (!isLoading) {
                //submitBufferingUpdate(bufferPercentage, null);
                callback.onBufferingUpdate(bufferPercentage);
            }
            Log.d(TAG, "onLoadingChanged : " + isLoading + ", bufferPercentage = " + bufferPercentage);
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.e(TAG, error.getMessage() == null ? "" : error.getMessage());
            setCurrentState(STATE_ERROR);
            if (callback != null) {
                // 类型 代码
                callback.onError(error.type, error.rendererIndex);
            }
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        }
    }

}
