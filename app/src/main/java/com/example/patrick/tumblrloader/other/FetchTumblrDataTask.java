package com.example.patrick.tumblrloader.other;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchTumblrDataTask extends AsyncTask<Void, Void, String> {
    public final static String oauth_key = "AdTG7mb7yTD1ccUZPWug2kejxQqyGGwd2lXhWWrxNndKcS0sBK";
    private String bloggerName;
    private FetchTumblrDataTaskListener mListener;

    public FetchTumblrDataTask(String str) {
        this.bloggerName = str;
    }

    final public void setListener(FetchTumblrDataTaskListener listener) {
        mListener = listener;
    }

    @Override
    final protected String doInBackground(Void... progress) {
        // do stuff, common to both activities in here
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String JsonStr;

        try {
            if (bloggerName != null) {
                if (bloggerName.length() > 0) {
                    String url_str = "https://api.tumblr.com/v2/blog/" + bloggerName + "/posts/video?api_key=" + oauth_key;
                    URL url = new URL(url_str);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream;
                    try {
                        inputStream = urlConnection.getInputStream();
                    } catch (java.io.FileNotFoundException e) {
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
    final protected void onPreExecute() {
        // common stuff

        if (mListener != null)
            mListener.onPreExecuteConcluded();
    }

    @Override
    final protected void onPostExecute(String result) {
        // common stuff
        super.onPostExecute(result);

        if (mListener != null)
            mListener.onPostExecuteConcluded(result);
    }

    public interface FetchTumblrDataTaskListener {
        void onPreExecuteConcluded();

        void onPostExecuteConcluded(String result);
    }
}