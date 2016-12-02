package com.example.patrick.tumblrloader.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.patrick.tumblrloader.Adapter.VideoItem;
import com.example.patrick.tumblrloader.Adapter.VideoListingOnItemClickListener;
import com.example.patrick.tumblrloader.Adapter.VideoListingViewAdapter;
import com.example.patrick.tumblrloader.DB.BloggerDB;
import com.example.patrick.tumblrloader.DB.DataBaseInerator;
import com.example.patrick.tumblrloader.R;
import com.example.patrick.tumblrloader.other.FetchTumblrDataTask;
import com.google.gson.Gson;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class VideoListingActivity extends Activity implements FetchTumblrDataTask.FetchTumblrDataTaskListener {
    public final static String AES_PASSWORD = "tumblr_loader";
    public final static String EXTRA_MESSAGE = "com.example.patrick.tumblrloader.main";
    public final static String EXTRA_MESSAGE_SINGLE_URL = "com.example.patrick.tumblrloader.url";
    public final static String EXTRA_MESSAGE_PLAYLIST = "com.example.patrick.tumblrloader.playlist";
    public final static String EXTRA_MESSAGE_LIST_OF_URL = "com.example.patrick.tumblrloader.url";
    public final static String EXTRA_MESSAGE_POS = "com.example.patrick.tumblrloader.pos";

    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;

    private String mEncryptedJsonStr;
    private List<VideoItem> videoItemList;
    private boolean mIsAutoPlay;
    private String blogger;
    private DataBaseInerator db;

    public boolean isAutoPlay() {
        return mIsAutoPlay;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsAutoPlay = false;

        db = new DataBaseInerator();

        setContentView(R.layout.activity_video_listing);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ToggleButton mAutoPlayBtn = (ToggleButton) findViewById(R.id.toggleButton);

        mAutoPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsAutoPlay = !mIsAutoPlay;
            }
        });

        TextView BloggerNameTextView = (TextView) findViewById(R.id.blogger_name);

        Intent intent = getIntent();
        blogger = intent.getStringExtra(EXTRA_MESSAGE);
        String str = blogger + " >";

        BloggerNameTextView.setText(str);

        FetchTumblrDataTask task = new FetchTumblrDataTask(blogger);
        task.setListener(this);

        if (db.isBloggerExist(blogger)) {
            BloggerDB blog = db.findBloggerByName(blogger);
            if (blog.json != null) {
                try {
                    String jsonStr = AESCrypt.decrypt(AES_PASSWORD, blog.json);
                    postProcessJson(jsonStr, false);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            } else {
                task.execute();
            }
        } else {
            task.execute();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onPreExecuteConcluded() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPostExecuteConcluded(String s) {
        progressBar.setVisibility(View.GONE);
        try {
            if (s != null) {
                if (!s.isEmpty()) {
                    postProcessJson(s, true);
                    if (videoItemList != null) {
                        if (videoItemList.size() > 0) {
                            db.saveBloggertoDB(blogger, mEncryptedJsonStr);
                        }
                    }
                }
            } else {
                show_alert_message(getText(R.string.app_name).toString(), "You may entered a wrong blogger name, please try again!");
            }
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void postProcessJson(String str, boolean encrypt) {
        videoItemList = null;

        parseResult(str);
        if (videoItemList != null) {
            if (videoItemList.size() > 0) {
                if (encrypt) {
                    try {
                        mEncryptedJsonStr = AESCrypt.encrypt(AES_PASSWORD, str);
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }
                VideoListingViewAdapter adapter = new VideoListingViewAdapter(VideoListingActivity.this, videoItemList);
                adapter.setVideoListingOnItemClickListener(new VideoListingOnItemClickListener() {
                    @Override
                    public void onItemClick(VideoItem item) {
                        on_list_item_clicked(item.getVideourl());
                    }

                    @Override
                    public void onItemClick(int pos) {
                        on_list_item_clicked(pos);
                    }
                });
                mRecyclerView.setAdapter(adapter);
            } else {
                show_alert_message(getText(R.string.app_name).toString(), "No video listing of this blogger|");
            }
        } else {
            show_alert_message(getText(R.string.app_name).toString(), "No video listing of this blogger|");
        }
    }

    private void on_list_item_clicked(int pos) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        Gson gson = new Gson();
        String jsonCars = gson.toJson(videoItemList);
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_MESSAGE_POS, pos);
        extras.putBoolean(EXTRA_MESSAGE_PLAYLIST, true);
        extras.putString(EXTRA_MESSAGE_LIST_OF_URL, jsonCars);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void on_list_item_clicked(String url) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        Bundle extras = new Bundle();
        extras.putBoolean(EXTRA_MESSAGE_PLAYLIST, false);
        extras.putString(EXTRA_MESSAGE_SINGLE_URL, url);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void parseResult(String result) {
        try {
            JSONObject mainObject = new JSONObject(result);
            JSONObject responseObj = null;
            JSONArray jsonArray = null;
            if (mainObject.has("response")) {
                responseObj = mainObject.optJSONObject("response");
            }
            if (responseObj != null) {
                if (responseObj.has("posts")) jsonArray = responseObj.optJSONArray("posts");
            }

            if (jsonArray != null) {
                if (jsonArray.length() > 0) {
                    videoItemList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject post = jsonArray.optJSONObject(i);
                        if (post.has("video_url") && post.has("thumbnail_url")) {
                            VideoItem item = new VideoItem(post);
                            videoItemList.add(item);
                        }
                    }
                } else {
                    show_alert_message(getText(R.string.app_name).toString(), "No video listing of this blogger|");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void show_alert_message(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoListingActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
