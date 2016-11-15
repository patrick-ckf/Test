package com.example.patrick.tumblrloader.Adaptor;

import org.json.JSONException;
import org.json.JSONObject;

public class VideoItem {
    //private String title;
    private String videourl;
    private String thumbnail;

    //public String getTitle() { return this.title; }
    public String getVideourl () { return this.videourl; }
    String getThumbnail() { return this.thumbnail; }

    // Constructor to convert JSON object into a Java class instance
    public VideoItem(JSONObject object){
        try {
            //this.title = object.getString("source_title");
            this.videourl = object.getString("video_url");
            this.thumbnail = object.getString("thumbnail_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}