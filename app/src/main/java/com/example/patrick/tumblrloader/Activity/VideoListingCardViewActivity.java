package com.example.patrick.tumblrloader.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.patrick.tumblrloader.Adapter.AlbumsAdapter;
import com.example.patrick.tumblrloader.Adapter.VideoItem;
import com.example.patrick.tumblrloader.Adapter.VideoListingOnItemClickListener;
import com.example.patrick.tumblrloader.DB.BloggerDB;
import com.example.patrick.tumblrloader.DB.DataBaseInerator;
import com.example.patrick.tumblrloader.R;
import com.example.patrick.tumblrloader.other.FetchTumblrDataTask;
import com.scottyab.aescrypt.AESCrypt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class VideoListingCardViewActivity extends Activity implements FetchTumblrDataTask.FetchTumblrDataTaskListener {
    public final static String AES_PASSWORD = "tumblr_loader";
    public final static String EXTRA_MESSAGE = "com.example.patrick.tumblrloader.main";
    public final static String EXTRA_MESSAGE_SINGLE_URL = "com.example.patrick.tumblrloader.url";
    public final static String EXTRA_MESSAGE_PLAYLIST = "com.example.patrick.tumblrloader.playlist";

    private RecyclerView recyclerView;
    private AlbumsAdapter adapter;
    private List<VideoItem> videoItemList;
    private ProgressBar progressBar;

    private DataBaseInerator db;

    private String mEncryptedJsonStr;
    private String blogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_listing_card_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        TextView BloggerNameTextView = (TextView) findViewById(R.id.blogger_name);

        db = new DataBaseInerator();
        videoItemList = new ArrayList<>();

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

                adapter = new AlbumsAdapter(VideoListingCardViewActivity.this, videoItemList);

                adapter.setVideoListingOnItemClickListener(new VideoListingOnItemClickListener() {
                    @Override
                    public void onItemClick(VideoItem item) {
                        on_list_item_clicked(item.getVideourl());
                    }

                    @Override
                    public void onItemClick(int pos) {
                    }
                });

                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(adapter);

            } else {
                show_alert_message(getText(R.string.app_name).toString(), "No video listing of this blogger|");
            }
        } else {
            show_alert_message(getText(R.string.app_name).toString(), "No video listing of this blogger|");
        }
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

    private void show_alert_message(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoListingCardViewActivity.this);
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

    private void on_list_item_clicked(String url) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        Bundle extras = new Bundle();
        extras.putBoolean(EXTRA_MESSAGE_PLAYLIST, false);
        extras.putString(EXTRA_MESSAGE_SINGLE_URL, url);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}