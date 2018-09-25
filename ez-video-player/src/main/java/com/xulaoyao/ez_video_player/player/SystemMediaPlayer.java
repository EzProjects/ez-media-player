package com.xulaoyao.ez_video_player.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceView;

import com.xulaoyao.ez_video_player.EzVideoPlayer;
import com.xulaoyao.ez_video_player.model.EzVideoInfo;

import java.io.IOException;

/**
 * SystemMediaPlayer
 * MediaPlayer 系统视频播放器实现
 * Created by renwoxing on 2018/9/25.
 */
public class SystemMediaPlayer extends EzVideoPlayer {


    private static final String TAG = "MediaPlayer";
    private MediaPlayer player;
    private MediaPlayer.OnErrorListener mErrorListener;

    public SystemMediaPlayer() {
        mErrorListener = new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                setCurrentState(STATE_ERROR);
                if (callback != null) {
                    callback.onError(framework_err, impl_err);
                }
                return true;
            }
        };
    }

    @Override
    public void openVideo() {
        if (getVideoPath() == null || surfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        reset();

        try {
            player = new MediaPlayer();
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    currentBufferPercentage = percent;
                    if (callback != null) callback.onBufferingUpdate(percent);
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    setCurrentState(STATE_PLAYBACK_COMPLETED);
                    if (callback != null) {
                        callback.onCompletion();
                    }
                }
            });
            player.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (callback != null) {
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            callback.onLoadingChanged(true);
                        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                            callback.onLoadingChanged(false);
                        }
                    }
                    return false;
                }
            });
            player.setOnErrorListener(mErrorListener);
            player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    if (callback != null) callback.onVideoSizeChanged(width, height);
                }
            });
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setCurrentState(STATE_PREPARED);
                    if (callback != null) {
                        callback.onPrepared();
                    }
                }
            });
            currentBufferPercentage = 0;
            player.setDataSource(path);
            player.setDisplay(surfaceHolder);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setScreenOnWhilePlaying(true);
            player.prepareAsync();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            setCurrentState(STATE_PREPARING);
        } catch (IOException | IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + path, ex);
            setCurrentState(STATE_ERROR);
            mErrorListener.onError(player, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }


    @Override
    public void setSurfaceView(SurfaceView surfaceView) {
        surfaceHolder = surfaceView.getHolder();
    }

//    @Override
//    public void setSurface(Surface surface) {
//
//    }

    @Override
    public void setDataSource(EzVideoInfo info) {
        if (info != null)
            super.path = info.getVideoPath();
    }

    @Override
    public void start() {
        Log.i("DDD", "start");
        if (isInPlaybackState()) {
            player.start();
            setCurrentState(STATE_PLAYING);
        }
    }

    @Override
    public void start(int msc) {

    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (player.isPlaying()) {
                player.pause();
                setCurrentState(STATE_PAUSED);
            }
        }
    }

    @Override
    public void resume() {
        Log.i("DDD", "restart");
        openVideo();
    }

    @Override
    public void seekTo(int msc) {
        if (isInPlaybackState()) {
            player.seekTo(msc);
        }
    }

    @Override
    public void stop() {
        if (player != null) {
            player.stop();
            player.release();
            // TODO: 2017/6/19 = null ?
            player = null;
            surfaceHolder = null;
            setCurrentState(STATE_IDLE);
        }
    }

    @Override
    public void reset() {
        if (player != null) {
            player.reset();
            player.release();
            setCurrentState(STATE_IDLE);
        }
    }

    @Override
    public void destroy() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
            surfaceHolder = null;
            setCurrentState(STATE_IDLE);
        }
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return player.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && player.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (player != null) {
            return currentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean isInPlaybackState() {
        return (player != null &&
                curState != STATE_ERROR &&
                curState != STATE_IDLE &&
                curState != STATE_PREPARING);
    }

}
