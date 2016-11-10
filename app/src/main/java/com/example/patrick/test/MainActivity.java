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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
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

public class MainActivity extends Activity {
    public final static String AES_PASSWORD = "tumblr_loader";
    public final static String EXTRA_MESSAGE = "com.example.patrick.test.main";
    public final static String oauth_key = "AdTG7mb7yTD1ccUZPWug2kejxQqyGGwd2lXhWWrxNndKcS0sBK";

    private EditText mEdit;
    private ProgressBar progressBar;
    private RecyclerView mRecyclerView;

    private String mEncryptedJsonStr;

    private List<VideoItem> videoItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //new Delete().from(Blogger.class).where("name = ?", "xxxsexxx").execute();
        //new Delete().from(Blogger.class).where("name = ?", "av-jokbari").execute();
        //List<Blogger> list = new Select().from(Blogger.class).execute();
        mEdit = (EditText) findViewById(R.id.blogger_name);
        Button Button = (Button) findViewById(R.id.load_btn);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (i)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            loadBlogger();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        Button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          loadBlogger();
                                      }
                                  }
        );

        progressBar.setVisibility(View.INVISIBLE);

        Blogger temp = getBlogger();
        if (temp != null) {
            Log.d(getText(R.string.app_name).toString(), temp.name);
            mEdit.setText(temp.name);
            try {

                if (temp.json != null && temp.json.length() > 0) {
                    String jsonStr = AESCrypt.decrypt(AES_PASSWORD, temp.json);
                    postProcessJson(jsonStr, false);
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
            clear_keyboard();
        }
    }

    private void clear_keyboard() {
        mEdit.clearFocus();
        hideSoftKeyboard(MainActivity.this);
    }

    private void loadBlogger() {
        String blogger_name = mEdit.getText().toString();
        if (blogger_name.length() > 0) {
            clear_keyboard();
            new FetchTumblrData(blogger_name).execute();
        } else {
            // prompt message to handle
            show_alert_message(getText(R.string.app_name).toString(), "Blogger name should not be blank!");
        }
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        try {
            assert activity.getCurrentFocus() != null;
            assert activity.getCurrentFocus().getWindowToken() != null;
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }
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

                        InputStream inputStream = urlConnection.getInputStream();
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
                        saveBloggertoDB();
                    }
                } else {
                    show_alert_message(getText(R.string.app_name).toString(), "You may entered a wrong blogger name, please try again!");
                }
            } catch (java.lang.NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void postProcessJson(String str, boolean encrypt) {
        videoItemList = null;
        if (encrypt) {
            try {
                mEncryptedJsonStr = AESCrypt.encrypt(AES_PASSWORD, str);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
        parseResult(str);
        MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(MainActivity.this, videoItemList);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(VideoItem item) {
                on_list_item_clicked(item.getVideourl());
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    private void saveBloggertoDB() {
        String blog_name = mEdit.getText().toString();
        Blogger item = new Select().from(Blogger.class).where("name = ?", blog_name).executeSingle();
        if (item == null) {
            ActiveAndroid.beginTransaction();
            try {
                Blogger blogger = new Blogger();
                blogger.name = blog_name;
                blogger.json = mEncryptedJsonStr;
                blogger.save();
                ActiveAndroid.setTransactionSuccessful();
                //List<Blogger> list = new Select().from(Blogger.class).execute();
                //Log.d("Blogger name", blog_name);
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private static Blogger getBlogger() {
        return new Select().from(Blogger.class).orderBy("Id DESC").limit(1).executeSingle();
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
                VideoItem item = new VideoItem(post);
                videoItemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}