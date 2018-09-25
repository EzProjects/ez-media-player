package com.xulaoyao.ezmediaplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.xulaoyao.ez_video_player.EzVideoPlayerView;
import com.xulaoyao.ez_video_player.listener.SimpleOnVideoControlListener;
import com.xulaoyao.ez_video_player.model.EzVideoInfo;
import com.xulaoyao.ez_video_player.util.DisplayUtils;

//import tv.danmaku.ijk.media.player.IMediaPlayer;
//import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity {

    //private static final String VIDEO_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    //private static final String VIDEO_URL = "http://developv.xy.jxcstatic.com/0548EFB26CEEA9D1FB2E8BAC35A82F3E";
    //private static final String VIDEO_URL = "http://jxcvideo.xy.jxcstatic.com/56B75A564A415FBE28AD2231A3E94CDF";
    private static final String VIDEO_URL = "http://jxcvideo.xy.jxcstatic.com/A8386451B572D93CACC24BCC6B3F2339";
    //

    //private AndroidMediaController mMediaController;

    // Used to load the 'native-lib' library on application startup.
    static {
        //System.loadLibrary("native-lib");
    }

    //private IjkVideoView mVideoView;
    //private TableLayout mHudView;
    private EzVideoPlayerView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setFullScreen(this);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);

        //tv.setText(stringFromJNI());
        //mHudView = (TableLayout) findViewById(R.id.hud_view);

        //initPalyer();

        mVideoView = (EzVideoPlayerView) findViewById(R.id.ez_vpv);
        mVideoView.setVideoControlListener(new SimpleOnVideoControlListener() {

            @Override
            public void onRetry(int errorStatus) {
                // TODO: 2017/6/20 调用业务接口重新获取数据
                // get info and call method "videoView.startPlayVideo(info);"
            }

            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onFullScreen() {
                DisplayUtils.toggleScreenOrientation(MainActivity.this);
            }
        });

        EzVideoInfo info = new EzVideoInfo("----标题", VIDEO_URL);
        mVideoView.startPlayVideo(info);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mVideoView != null)
            mVideoView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVideoView != null)
            mVideoView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null)
            mVideoView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!DisplayUtils.isPortrait(this) && mVideoView != null) {
            if (!mVideoView.isLock()) {
                DisplayUtils.toggleScreenOrientation(this);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    //    private void initPalyer() {
//        // int ui
//        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        //setSupportActionBar(toolbar);
//
//        ActionBar actionBar = getSupportActionBar();
//        mMediaController = new AndroidMediaController(this, false);
//        mMediaController.setSupportActionBar(actionBar);
//
//        // init player
//        IjkMediaPlayer.loadLibrariesOnce(null);
//        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
//        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
//        mVideoView.setMediaController(mMediaController);
//        //mVideoView.setHudView(mHudView);
//
//        mVideoView.setVideoURI(Uri.parse("http://106.36.45.36/live.aishang.ctlcdn.com/00000110240001_1/encoder/1/playlist.m3u8"));
//
//        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(IMediaPlayer iMediaPlayer) {
//                mVideoView.start();
//            }
//        });
//    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

}
