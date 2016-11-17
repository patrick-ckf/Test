package com.example.patrick.tumblrloader.Activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.example.patrick.tumblrloader.Adaptor.VideoItem;
import com.example.patrick.tumblrloader.Adaptor.VideoListingOnItemClickListener;
import com.example.patrick.tumblrloader.Adaptor.VideoListingViewAdaptor;
import com.example.patrick.tumblrloader.DB.BloggerDB;
import com.example.patrick.tumblrloader.R;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class VideoListingActivity extends Activity {
    public final static String AES_PASSWORD = "tumblr_loader";
    public final static String EXTRA_MESSAGE = "com.example.patrick.tumblrloader.main";
    public final static String oauth_key = "AdTG7mb7yTD1ccUZPWug2kejxQqyGGwd2lXhWWrxNndKcS0sBK";

    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;

    private String mEncryptedJsonStr;

    private List<VideoItem> videoItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_listing);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        TextView BloggerNameTextView = (TextView) findViewById(R.id.blogger_name);

        Intent intent = getIntent();
        String blogger = intent.getStringExtra(EXTRA_MESSAGE);

        String str = blogger + " >";

        BloggerNameTextView.setText(str);

        if (find_blogger(blogger)) {
            BloggerDB blog = new Select().from(BloggerDB.class).where("name = ?", blogger).executeSingle();
            if (blog.json != null) {
                try {
                    String jsonStr = AESCrypt.decrypt(AES_PASSWORD, blog.json);
                    postProcessJson(jsonStr, false);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            } else {
                new FetchTumblrData(blog.name).execute();
            }
        } else {
            new FetchTumblrData(blogger).execute();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void show_alert_message(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoListingActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private class FetchTumblrData extends AsyncTask<Void, Void, String> {
        String blog_name;

        FetchTumblrData(String blog_name) {
            if (blog_name != null) {
                if (blog_name.length() > 0) {
                    this.blog_name = blog_name;
                }
            }
        }

        @Override
        protected void onPreExecute() { progressBar.setVisibility(View.VISIBLE); }

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String JsonStr;

            try {
                if (blog_name != null) {
                    if (blog_name.length() > 0) {
                        String url_str = "https://api.tumblr.com/v2/blog/" + blog_name + "/posts/video?api_key=" + oauth_key;
                        URL url = new URL(url_str);

                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        InputStream inputStream;
                        try {
                            inputStream = urlConnection.getInputStream();
                        } catch(java.io.FileNotFoundException e) {
                            e.printStackTrace();
                            return null;
                        }

                        StringBuilder buffer = new StringBuilder();
                        if (inputStream == null) {
                            // Nothing to do.
                            return null;
                        }

                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line).append("\n");
                        }

                        if (buffer.length() == 0) {
                            return null;
                        }
                        JsonStr = buffer.toString();
                        return JsonStr;
                    }
                }
                return null;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            try {
                if (s != null) {
                    if (!s.isEmpty()) {
                        postProcessJson(s, true);
                        if (videoItemList != null) {
                            if (videoItemList.size() > 0) {
                                saveBloggertoDB(blog_name);
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
    }

    private boolean find_blogger(String blogger) {
        BloggerDB item = new Select().from(BloggerDB.class).where("name = ?", blogger).executeSingle();
        return item != null;
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
                VideoListingViewAdaptor adapter = new VideoListingViewAdaptor(VideoListingActivity.this, videoItemList);
                adapter.setVideoListingOnItemClickListener(new VideoListingOnItemClickListener() {
                    @Override
                    public void onItemClick(VideoItem item) {
                        on_list_item_clicked(item.getVideourl());
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

    private void on_list_item_clicked(String url) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(EXTRA_MESSAGE, url);
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

    private void saveBloggertoDB(String blog_name) {
        BloggerDB item = new Select().from(BloggerDB.class).where("name = ?", blog_name).executeSingle();
        if (item == null) {
            ActiveAndroid.beginTransaction();
            try {
                BloggerDB blogger = new BloggerDB();
                blogger.name = blog_name;
                blogger.json = mEncryptedJsonStr;
                blogger.save();
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }
}
