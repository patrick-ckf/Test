package com.example.patrick.test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by patrick on 7/11/2016.
 */

public class VideoItem {
    private String title;
    private String video_url;
    private String thumbnail;

    public VideoItem() {}

    // Constructor to convert JSON object into a Java class instance
    public VideoItem(JSONObject object){
        try {
            this.title = object.getString("slug");
            this.video_url = object.getString("video_url");
            this.thumbnail = object.getString("thumbnail_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void setTitle (String title) { this.title = title; }
    void setVideo_url (String url) { this.video_url = url; }
    void setThumbnail (String thumbnail) { this.thumbnail = thumbnail; }

    String getTitle () { return this.title; }
    String getVideo_url () { return this.video_url; }
    String getThumbnail () { return this.thumbnail; }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
    public static ArrayList<VideoItem> fromJson(JSONArray jsonObjects) {
        ArrayList<VideoItem> items = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                JSONObject obj = jsonObjects.getJSONObject(i);
                VideoItem item = new VideoItem(obj);
                items.add(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return items;
    }
}