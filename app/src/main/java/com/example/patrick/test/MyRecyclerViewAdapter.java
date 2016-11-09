package com.example.patrick.test;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.List;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.CustomViewHolder> {
    private List<VideoItem> videoItemList;
    private Context mContext;

    private OnItemClickListener onItemClickListener;

    MyRecyclerViewAdapter(Context context, List<VideoItem> videoItemList) {
        this.videoItemList = videoItemList;
        this.mContext = context;
        this.onItemClickListener = null;
    }

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        VideoItem feedItem = videoItemList.get(i);

        //Render image using Picasso library
        if (!TextUtils.isEmpty(feedItem.getThumbnail())) {
            Picasso.with(mContext).load(feedItem.getThumbnail())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.imageView);
        }

        //customViewHolder.textView.setText(feedItem.getTitle());

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomViewHolder holder = (CustomViewHolder) v.getTag();
                if (holder != null) {
                    int pos = holder.getAdapterPosition();
                    if (pos >= 0) {
                        VideoItem feedItem = videoItemList.get(pos);
                        if (feedItem != null) {
                            onItemClickListener.onItemClick(feedItem);
                        }
                    }
                }
            }
        };

        customViewHolder.imageView.setOnClickListener(listener);
        customViewHolder.textView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != videoItemList ? videoItemList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        CustomViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.title);
            this.textView.setTag(this);
            this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            this.imageView.setTag(this);
        }
    }
}
