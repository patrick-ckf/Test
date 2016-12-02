package com.example.patrick.tumblrloader.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.patrick.tumblrloader.Activity.VideoListingActivity;
import com.example.patrick.tumblrloader.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoListingViewAdapter extends RecyclerView.Adapter<VideoListingViewAdapter.ImageTextViewHolder> {
    private List<VideoItem> videoItemList;
    private Context mContext;

    private VideoListingOnItemClickListener videoListingOnItemClickListener;

    public VideoListingViewAdapter(Context context, List<VideoItem> videoItemList) {
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

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageTextViewHolder holder = (ImageTextViewHolder) v.getTag();
                if (holder != null) {
                    int pos = holder.getAdapterPosition();
                    VideoListingActivity parent = (VideoListingActivity) mContext;
                    boolean autoPlay = parent.isAutoPlay();
                    if (pos >= 0) {
                        if (autoPlay) {
                            videoListingOnItemClickListener.onItemClick(pos);
                        } else {
                            VideoItem feedItem = videoItemList.get(pos);
                            if (feedItem != null) {
                                videoListingOnItemClickListener.onItemClick(feedItem);
                            }
                        }
                    }

                }
            }
        };

        customViewHolder.imageView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != videoItemList ? videoItemList.size() : 0);
    }

    class ImageTextViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageTextViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            this.imageView.setTag(this);
        }
    }
}
