package com.example.android.myweather.Weather;

public class LiveIndex {
    private String title;
    private String comment;
    private int imgResourceId;
    private String tips;

    public String getTitle() {
        return title;
    }

    public String getComment() {
        return comment;
    }

    public int getImgResourceId() {
        return imgResourceId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setImgResourceId(int imgResourceId) {
        this.imgResourceId = imgResourceId;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
