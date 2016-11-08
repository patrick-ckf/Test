package com.example.patrick.test;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.patrick.test.main";
    public final static String oauth_key = "AdTG7mb7yTD1ccUZPWug2kejxQqyGGwd2lXhWWrxNndKcS0sBK";

    private EditText mEdit;
    private ProgressBar progressBar;
    private RecyclerView mRecyclerView;

    private List<VideoItem> videoItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button Button = (Button) findViewById(R.id.load_btn);
        mEdit = (EditText) findViewById(R.id.blogger_name);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String blogger_name = mEdit.getText().toString();
                    if (blogger_name.length() > 0) {
                        new FetchTumblrData(blogger_name).execute();
                    } else {
                        // prompt message to handle
                        show_alert_message(EXTRA_MESSAGE, "Blogger name should not be blank!");
                    }
                }
            }
        );
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void show_alert_message(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
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
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                if (blog_name != null) {
                    if (blog_name.length() > 0) {
                        String url_str = "https://api.tumblr.com/v2/blog/" + blog_name + "/posts/video?api_key=" + oauth_key;
                        URL url = new URL(url_str);

                        // Create the request to OpenWeatherMap, and open the connection
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        // Read the input stream into a String
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuilder buffer = new StringBuilder();
                        if (inputStream == null) {
                            // Nothing to do.
                            return null;
                        }

                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                            // But it does make debugging a *lot* easier if you print out the completed
                            // buffer for debugging.
                            buffer.append(line).append("\n");
                        }

                        if (buffer.length() == 0) {
                            // Stream was empty.  No point in parsing.
                            return null;
                        }
                        forecastJsonStr = buffer.toString();
                        return forecastJsonStr;
                    }
                }
                return null;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
                        parseResult(s);
                        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(MainActivity.this, videoItemList);
                        adapter.setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick(VideoItem item) {
                                on_list_item_clicked(item.getVideo_url());
                            }
                        });
                        mRecyclerView.setAdapter(adapter);
                    }
                } else {
                    show_alert_message(EXTRA_MESSAGE, "You may entered a wrong blogger name, please try again!");
                }
            } catch (java.lang.NullPointerException e) {
                e.printStackTrace();
            }
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
            JSONObject responseObj = mainObject.optJSONObject("response");
            JSONArray jsonArray = responseObj.optJSONArray("posts");
            videoItemList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject post = jsonArray.optJSONObject(i);
                VideoItem item = new VideoItem();
                item.setTitle(post.optString("slug"));
                item.setVideo_url(post.optString("video_url"));
                item.setThumbnail(post.optString("thumbnail_url"));
                videoItemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}