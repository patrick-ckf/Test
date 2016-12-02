package com.example.patrick.tumblrloader.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.example.patrick.tumblrloader.Adapter.BloggerItem;
import com.example.patrick.tumblrloader.Adapter.BloggerListingOnItemClickListener;
import com.example.patrick.tumblrloader.Adapter.BloggerListingViewAdapter;
import com.example.patrick.tumblrloader.DB.BloggerDB;
import com.example.patrick.tumblrloader.DB.DataBaseInerator;
import com.example.patrick.tumblrloader.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.patrick.tumblrloader.main";

    public final static int time_interval = 60 * 60 * 1000;

    private EditText mEdit;
    private RecyclerView mRecyclerView;

    private List<BloggerItem> bloggerList;

    private DataBaseInerator db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DataBaseInerator();

        setContentView(R.layout.activity_main);

        mEdit = (EditText) findViewById(R.id.blogger_name);
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
                            String blogger_name = mEdit.getText().toString();
                            if (blogger_name.length() > 0) {
                                Random random = new Random();
                                int rand = random.nextInt(2);
                                if (rand == 0) {
                                    loadBloggerGridListing(blogger_name);
                                } else {
                                    loadBloggerListing(blogger_name);
                                }
                            }
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        setUpItemTouchHelper();
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeBloggerListing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bloggerList = null;
    }

    private void changeBloggerListing() {
        List<BloggerDB> list = new ArrayList<>();

        try {
            list = db.findBloggers();
        }  catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }

        bloggerList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            BloggerItem item = new BloggerItem();
            long _time = list.get(i).time;
            Calendar c = Calendar.getInstance();
            long cur_time = c.getTimeInMillis();
            if (cur_time - _time >= time_interval) {
                BloggerDB b = db.findBloggerByName(list.get(i).name);
                b.json = null;
                b.save();
            }
            item.setName(list.get(i).name);
            bloggerList.add(item);
        }

        BloggerListingViewAdapter adapter = new BloggerListingViewAdapter(bloggerList);
        adapter.setOnItemClickListener(new BloggerListingOnItemClickListener() {
            @Override
            public void onItemClick(BloggerItem item) {
                Random random = new Random();
                int i = random.nextInt(2);
                if (i == 0) {
                    loadBloggerGridListing(item.getName());
                } else {
                    loadBloggerListing(item.getName());
                }
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    private void loadBloggerListing(String blog) {
        Intent intent = new Intent(this, VideoListingActivity.class);
        intent.putExtra(EXTRA_MESSAGE, blog);
        startActivity(intent);
    }

    private void loadBloggerGridListing(String blog) {
        Intent intent = new Intent(this, VideoListingCardViewActivity.class);
        intent.putExtra(EXTRA_MESSAGE, blog);
        startActivity(intent);
    }

    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                BloggerListingViewAdapter adapter = (BloggerListingViewAdapter) mRecyclerView.getAdapter();
                adapter.remove(swipedPosition);
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }
}