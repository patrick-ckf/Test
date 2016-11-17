package com.example.patrick.tumblrloader.Adaptor;

import org.json.JSONObject;

public class VideoItem {
    private String videourl;
    private String thumbnail;

    public String getVideourl () { return this.videourl; }
    String getThumbnail() { return this.thumbnail; }

    // Constructor to convert JSON object into a Java class instance
    public VideoItem(JSONObject object){
        String videourl = null;
        String thumbnail = null;
        try {
            if (object.has("video_url")) videourl = object.getString("video_url");
            if (object.has("thumbnail_url")) thumbnail = object.getString("thumbnail_url");
        } catch(org.json.JSONException e) {
            e.printStackTrace();
        } finally {
            if (videourl != null) this.videourl = videourl;
            if (thumbnail != null) this.thumbnail = thumbnail;
        }
    }
}