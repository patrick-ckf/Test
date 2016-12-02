package com.example.patrick.tumblrloader.Adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.patrick.tumblrloader.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder> {

    private Context mContext;
    private List<VideoItem> videoItemList;
    private VideoListingOnItemClickListener videoListingOnItemClickListener;

    public AlbumsAdapter(Context mContext, List<VideoItem> videoItemList) {
        this.mContext = mContext;
        this.videoItemList = videoItemList;
        this.videoListingOnItemClickListener = null;
    }

    public void setVideoListingOnItemClickListener(VideoListingOnItemClickListener videoListingOnItemClickListener) {
        this.videoListingOnItemClickListener = videoListingOnItemClickListener;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);

        return new AlbumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AlbumViewHolder holder, int position) {
        VideoItem feedItem = videoItemList.get(position);

        //Render image using Picasso library
        if (!TextUtils.isEmpty(feedItem.getThumbnail())) {
            Picasso.with(mContext).load(feedItem.getThumbnail())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.thumbnail);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumsAdapter.AlbumViewHolder holder = (AlbumsAdapter.AlbumViewHolder) v.getTag();
                if (holder != null) {
                    int pos = holder.getAdapterPosition();
                    VideoItem feedItem = videoItemList.get(pos);
                    if (feedItem != null) {
                        videoListingOnItemClickListener.onItemClick(feedItem);
                    }
                }
            }
        };

        holder.thumbnail.setOnClickListener(listener);

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow);
            }
        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    @Override
    public int getItemCount() {
        return videoItemList.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail, overflow;

        public AlbumViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);

            this.thumbnail.setTag(this);
        }
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_play_next:
                    Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }
}
