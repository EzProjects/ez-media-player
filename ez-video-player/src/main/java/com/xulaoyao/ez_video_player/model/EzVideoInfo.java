package com.xulaoyao.ez_video_player.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * EzVideoInfo
 * Created by renwoxing on 2018/1/22.
 */
public class EzVideoInfo implements Parcelable {

    public static final Creator<EzVideoInfo> CREATOR = new Creator<EzVideoInfo>() {
        @Override
        public EzVideoInfo createFromParcel(Parcel in) {
            return new EzVideoInfo(in);
        }

        @Override
        public EzVideoInfo[] newArray(int size) {
            return new EzVideoInfo[size];
        }
    };
    private String title;
    private String videoPath;

    public EzVideoInfo() {
    }

    public EzVideoInfo(String videoPath) {
        this.videoPath = videoPath;
    }

    public EzVideoInfo(String title, String videoPath) {
        this.title = title;
        this.videoPath = videoPath;
    }

    protected EzVideoInfo(Parcel in) {
        title = in.readString();
        videoPath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(videoPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public String toString() {
        return "EzVideoInfo{" +
                "title='" + title + '\'' +
                ", videoPath='" + videoPath + '\'' +
                '}';
    }
}
