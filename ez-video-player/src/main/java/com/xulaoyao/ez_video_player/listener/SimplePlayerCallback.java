package com.xulaoyao.ez_video_player.listener;

public class SimplePlayerCallback implements EzVideoPlayerCallback {

    @Override
    public void onPrepared() {

    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height) {

    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onError(int what, int extra) {

    }

    /**
     * 视频加载状态变化
     *
     * @param isShow 是否显示loading
     */
    @Override
    public void onLoadingChanged(boolean isShow) {

    }

    /**
     * 视频状态变化
     * <p><img src="../../../../../../images/mediaplayer_state_diagram.gif"
     * alt="MediaPlayer State diagram" border="0" /></p>
     *
     * @param curState 当前视频状态
     *                 <ul>
     *                 <li>{@link com.xulaoyao.ez_video_player.EzVideoPlayer#STATE_ERROR}
     *                 <li>{@link com.xulaoyao.ez_video_player.EzVideoPlayer#STATE_IDLE}
     *                 <li>{@link com.xulaoyao.ez_video_player.EzVideoPlayer#STATE_PREPARING}
     *                 <li>{@link com.xulaoyao.ez_video_player.EzVideoPlayer#STATE_PREPARED}
     *                 <li>{@link com.xulaoyao.ez_video_player.EzVideoPlayer#STATE_PLAYING}
     *                 <li>{@link com.xulaoyao.ez_video_player.EzVideoPlayer#STATE_PAUSED}
     *                 <li>{@link com.xulaoyao.ez_video_player.EzVideoPlayer#STATE_PLAYBACK_COMPLETED}
     *                 </ul>
     */
    @Override
    public void onStateChanged(int curState) {

    }
}
