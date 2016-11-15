package com.example.patrick.tumblrloader.Adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.patrick.tumblrloader.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoListingViewAdaptor extends RecyclerView.Adapter<VideoListingViewAdaptor.ImageTextViewHolder> {
    private List<VideoItem> videoItemList;
    private Context mContext;

    private VideoListingOnItemClickListener videoListingOnItemClickListener;

    public VideoListingViewAdaptor(Context context, List<VideoItem> videoItemList) {
        this.videoItemList = videoItemList;
        this.mContext = context;
        this.videoListingOnItemClickListener = null;
    }

    public void setVideoListingOnItemClickListener(VideoListingOnItemClickListener videoListingOnItemClickListener) {
        this.videoListingOnItemClickListener = videoListingOnItemClickListener;
    }

    @Override
    public ImageTextViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_listing_item, null);
        return new ImageTextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageTextViewHolder customViewHolder, int i) {
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
                ImageTextViewHolder holder = (ImageTextViewHolder) v.getTag();
                if (holder != null) {
                    int pos = holder.getAdapterPosition();
                    if (pos >= 0) {
                        VideoItem feedItem = videoItemList.get(pos);
                        if (feedItem != null) {
                            videoListingOnItemClickListener.onItemClick(feedItem);
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

    class ImageTextViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        ImageTextViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.title);
            this.textView.setTag(this);
            this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            this.imageView.setTag(this);
        }
    }
}
