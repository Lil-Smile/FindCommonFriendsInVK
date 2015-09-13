package com.igoryakovlev.findcommonfriends.app;

/**
 * Created by Smile on 13.09.15.
 */
public class StringUrlWrapper {
    private String url;
    private int position;

    public StringUrlWrapper(String url, int position) {
        this.url = url;
        this.position = position;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
