package com.example.jingj.photogallery;

import com.google.gson.annotations.SerializedName;

public class GalleryItem {

    //标题
    @SerializedName("title")
    private String mCaption;

    @SerializedName("id")
    private String mID;

    @SerializedName("url_s")
    private String mUrl;

    public String getmCaption() {
        return mCaption;
    }

    public void setmCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getmID() {
        return mID;
    }

    public void setmID(String mID) {
        this.mID = mID;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    @Override
    public String toString() {
        return mCaption;
    }
}
