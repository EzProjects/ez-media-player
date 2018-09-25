package com.xulaoyao.ez_video_player.listener;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.xulaoyao.ez_video_player.model.EzVideoInfo;

/**
 * IPlayer
 * Created by renwoxing on 2018/9/25.
 */
public interface IPlayer {

    void setSurfaceView(SurfaceView surfaceView);

    //void setSurface(Surface surface);
    void setDisplay(SurfaceHolder surfaceHolder);


    // 设置 相关信息
    void setDataSource(EzVideoInfo info);


    void openVideo();

    int getCurrentPosition();

    int getBufferPercentage();

    int getDuration();

    boolean isPlaying();

    boolean isInPlaybackState();


    void start();

    void start(int msc);

    void pause();

    void resume();

    void seekTo(int msc);

    void stop();

    void reset();

    void destroy();
}
