package com.example.patrick.tumblrloader.Adaptor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.example.patrick.tumblrloader.DB.BloggerDB;
import com.example.patrick.tumblrloader.R;

import java.util.List;

public class BloggerListingViewAdaptor extends RecyclerView.Adapter<BloggerListingViewAdaptor.BlogListViewHolder> {
    private List<BloggerItem> bloggerList;

    private BloggerListingOnItemClickListener onItemClickListener;

    public BloggerListingViewAdaptor(List<BloggerItem> bloggerList) {
        this.bloggerList = bloggerList;
        this.onItemClickListener = null;
    }

    public void setOnItemClickListener(BloggerListingOnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public BlogListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_listing_item, null);
        return new BlogListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BlogListViewHolder customViewHolder, int i) {
        BloggerItem item = bloggerList.get(i);

        String str = "Blogger: "+ item.getName();
        customViewHolder.textView.setText(str);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BlogListViewHolder holder = (BlogListViewHolder) v.getTag();
                if (holder != null) {
                    int pos = holder.getAdapterPosition();
                    if (pos >= 0) {
                        BloggerItem feedItem = bloggerList.get(pos);
                        if (feedItem != null) {
                            onItemClickListener.onItemClick(feedItem);
                        }
                    }
                }
            }
        };

        customViewHolder.textView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != bloggerList ? bloggerList.size() : 0);
    }

    class BlogListViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        BlogListViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.blogger_name);
            this.textView.setTag(this);
        }
    }

    public void remove(int position) {
        if (position < 0 || position >= bloggerList.size()) {
            return;
        }

        List<BloggerDB> list = new Select().from(BloggerDB.class).execute();
        list.get(position);

        bloggerList.remove(position);
        notifyItemRemoved(position);
    }
}