package com.xulaoyao.ez_video_player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xulaoyao.ez_video_player.R;
import com.xulaoyao.ez_video_player.listener.EzVideoPlayerControlListener;

/**
 * 视频错误View
 */
public class EzVideoPlayerErrorView extends FrameLayout {

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_VIDEO_DETAIL_ERROR = 1;
    public static final int STATUS_VIDEO_SRC_ERROR = 2;
    public static final int STATUS_UN_WIFI_ERROR = 3;
    public static final int STATUS_NO_NETWORK_ERROR = 4;
    public static final int STATUS_VIDEO_SRC_NULL = 5;

    private int curStatus;
    private TextView video_error_info;
    private Button video_error_retry;
    private EzVideoPlayerControlListener ezVideoPlayerControlListener;

    public EzVideoPlayerErrorView(Context context) {
        super(context);
        init();
    }

    public EzVideoPlayerErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EzVideoPlayerErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getCurStatus() {
        return curStatus;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.ez_video_player_controller_error, this);

        video_error_info = (TextView) findViewById(R.id.video_error_info);
        video_error_retry = (Button) findViewById(R.id.video_error_retry);

        video_error_retry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ezVideoPlayerControlListener != null) {
                    ezVideoPlayerControlListener.onRetry(curStatus);
                }
            }
        });

        hideError();
    }

    public void showError(int status) {
        setVisibility(View.VISIBLE);

        if (curStatus == status) {
            return;
        }

        curStatus = status;

        switch (status) {
            case STATUS_VIDEO_DETAIL_ERROR:
                Log.i("DDD", "showVideoDetailError");
                video_error_info.setText("视频加载失败");
                video_error_retry.setText("点此重试");
                break;
            case STATUS_VIDEO_SRC_ERROR:
                Log.i("DDD", "showVideoSrcError");
                video_error_info.setText("视频加载失败");
                video_error_retry.setText("点此重试");
                break;
            case STATUS_NO_NETWORK_ERROR:
                Log.i("DDD", "showNoNetWorkError");
                video_error_info.setText("网络连接异常，请检查网络设置后重试");
                video_error_retry.setText("重试");
                break;
            case STATUS_VIDEO_SRC_NULL:
                Log.i("DDD", "showNoNetWorkError");
                video_error_info.setText("视频为空");
                video_error_retry.setText("请重新加载视频");
            case STATUS_UN_WIFI_ERROR:
                Log.i("DDD", "showUnWifiError");
                video_error_info.setText("温馨提示：您正在使用非WiFi网络，播放将产生流量费用");
                video_error_retry.setText("继续播放");
                break;
        }
    }

    public void hideError() {
        Log.i("DDD", "hideError");
        setVisibility(GONE);
        curStatus = STATUS_NORMAL;
    }

    public void setEzVideoPlayerControlListener(EzVideoPlayerControlListener ezVideoPlayerControlListener) {
        this.ezVideoPlayerControlListener = ezVideoPlayerControlListener;
    }
}
