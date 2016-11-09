package com.example.patrick.test;

import org.json.JSONException;
import org.json.JSONObject;

class VideoItem {
    private String title;
    private String videourl;
    private String thumbnail;

    String getTitle() { return this.title; }
    String getVideourl () { return this.videourl; }
    String getThumbnail () { return this.thumbnail; }

    // Constructor to convert JSON object into a Java class instance
    VideoItem(JSONObject object){
        try {
            this.title = object.getString("source_title");
            this.videourl = object.getString("video_url");
            this.thumbnail = object.getString("thumbnail_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}