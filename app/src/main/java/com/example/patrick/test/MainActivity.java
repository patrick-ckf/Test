package com.example.patrick.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.patrick.test.main";

    private EditText mEdit;
    private ProgressBar progressBar;
    private RecyclerView mRecyclerView;

    private List<BloggerItem> bloggerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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
                            String blogger_name = mEdit.getText().toString();
                            if (blogger_name.length() > 0)
                                loadBloggerListing(blogger_name);
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
                                          String blogger_name = mEdit.getText().toString();
                                          if (blogger_name.length() > 0)
                                              loadBloggerListing(blogger_name);
                                      }
                                  }
        );

        progressBar.setVisibility(View.INVISIBLE);
    }

    private void changeBloggerListing() {
        List<BloggerDB> list = new ArrayList<>();

        try {
            list = new Select().from(BloggerDB.class).execute();
        }  catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }

        bloggerList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            BloggerItem item = new BloggerItem();
            long _time = list.get(i).time;
            Calendar c = Calendar.getInstance();
            long cur_time = c.getTimeInMillis();
            if (cur_time - _time >= 3600000) {
                new Delete().from(BloggerDB.class).where("Name = ?", list.get(i).name).execute();
            } else {
                item.setName(list.get(i).name);
                bloggerList.add(item);
            }
        }

        BloggerListingViewAdaptor adapter = new BloggerListingViewAdaptor(MainActivity.this, bloggerList);
        adapter.setOnItemClickListener(new BloggerListingOnItemClickListener() {
            @Override
            public void onItemClick(BloggerItem item) {
                loadBloggerListing(item.getName());
            }
        });
        mRecyclerView.setAdapter(adapter);
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

    private void loadBloggerListing(String blog) {
        Intent intent = new Intent(this, VideoListingActivity.class);
        intent.putExtra(EXTRA_MESSAGE, blog);
        startActivity(intent);
    }
}