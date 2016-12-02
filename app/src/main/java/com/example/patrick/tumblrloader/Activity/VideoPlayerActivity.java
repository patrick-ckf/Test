package com.example.patrick.tumblrloader.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.example.patrick.tumblrloader.Adapter.VideoItem;
import com.example.patrick.tumblrloader.R;
import com.example.patrick.tumblrloader.TumblrLoaderApplication;
import com.example.patrick.tumblrloader.other.EventLogger;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelections;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;


public class VideoPlayerActivity extends Activity implements ExoPlayer.EventListener,
        TrackSelector.EventListener<MappingTrackSelector.MappedTrackInfo>{
    //public final static String EXTRA_MESSAGE = "com.example.patrick.tumblrloader.main";
    public final static String EXTRA_MESSAGE_SIGNLE_URL = "com.example.patrick.tumblrloader.url";
    public final static String EXTRA_MESSAGE_PLAYLIST = "com.example.patrick.tumblrloader.playlist";
    public final static String EXTRA_MESSAGE_LIST_OF_URL = "com.example.patrick.tumblrloader.url";
    public final static String EXTRA_MESSAGE_POS = "com.example.patrick.tumblrloader.pos";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    List<VideoItem> videoItemList;
    private Handler mainHandler;
    private EventLogger eventLogger;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private MappingTrackSelector trackSelector;
    private boolean playerNeedsSource;
    private DataSource.Factory mediaDataSourceFactory;
    private Timeline.Window window;
    private boolean shouldAutoPlay;
    private boolean isTimelineStatic;
    private int playerWindow;
    private long playerPosition;
    private boolean autoPlayNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shouldAutoPlay = true;
        autoPlayNext = false;

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

        mediaDataSourceFactory = buildDataSourceFactory(true);
        setContentView(R.layout.activity_video_player);

        mainHandler = new Handler();
        window = new Timeline.Window();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        simpleExoPlayerView.requestFocus();
    }

    private void initializePlayer() {
        Intent intent = getIntent();
        String url = null;
        int pos = 0;
        Boolean play_list = intent.getBooleanExtra(EXTRA_MESSAGE_PLAYLIST, false);
        if (play_list) {
            String json_list = intent.getStringExtra(EXTRA_MESSAGE_LIST_OF_URL);
            pos = intent.getIntExtra(EXTRA_MESSAGE_POS, 0);
            Gson gson = new Gson();
            Type type = new TypeToken<List<VideoItem>>() {
            }.getType();
            videoItemList = gson.fromJson(json_list, type);
            autoPlayNext = true;
        } else {
            autoPlayNext = false;
            url = intent.getStringExtra(EXTRA_MESSAGE_SIGNLE_URL);
        }
        if (player == null) {
            eventLogger = new EventLogger();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
            trackSelector.addListener(this);
            trackSelector.addListener(eventLogger);
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl());
            player.addListener(this);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setId3Output(eventLogger);
            simpleExoPlayerView.setPlayer(player);
            if (isTimelineStatic) {
                if (playerPosition == C.TIME_UNSET) {
                    player.seekToDefaultPosition(playerWindow);
                } else {
                    player.seekTo(playerWindow, playerPosition);
                }
            }
            player.setPlayWhenReady(shouldAutoPlay);
            playerNeedsSource = true;
        }
        if (playerNeedsSource) {
            playerNeedsSource = false;
            if (!play_list) {
                Uri uri = null;
                try {
                    uri = Uri.parse(url);
                } catch (java.lang.NullPointerException e) {
                    e.printStackTrace();
                } finally {
                    if (uri != null) {
                        MediaSource mediaSource = buildMediaSource(uri);
                        player.prepare(mediaSource, !isTimelineStatic, !isTimelineStatic);
                    } else {
                        onBackPressed();
                    }
                }
            } else {
                MediaSource[] mediaSources = new MediaSource[videoItemList.size() - pos];
                for (int i = pos; i < videoItemList.size(); i++) {
                    Uri uri = Uri.parse(videoItemList.get(i).getVideourl());
                    mediaSources[i - pos] = buildMediaSource(uri);
                }
                MediaSource mediaSource = new ConcatenatingMediaSource(mediaSources);
                player.prepare(mediaSource, !isTimelineStatic, !isTimelineStatic);
            }
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(), mainHandler, eventLogger);
    }

    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            playerWindow = player.getCurrentWindowIndex();
            playerPosition = C.TIME_UNSET;
            Timeline timeline = player.getCurrentTimeline();
            if (timeline != null && timeline.getWindow(playerWindow, window).isSeekable) {
                playerPosition = player.getCurrentPosition();
            }
            player.release();
            player = null;
            trackSelector = null;
            eventLogger = null;
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        TumblrLoaderApplication app = (TumblrLoaderApplication) getApplication();
        return app.buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer();
        } else {
            finish();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        isTimelineStatic = false;
        setIntent(intent);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object obj) {
        isTimelineStatic = timeline != null && timeline.getWindowCount() > 0
                && !timeline.getWindow(timeline.getWindowCount() - 1, window).isDynamic;
    }

    @Override
    public void	onPositionDiscontinuity() {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_ENDED:
                if (autoPlayNext) {
                    // do auto play next here
                    Log.d("test", "play next video");
                } else {
                    onBackPressed();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        playerNeedsSource = true;
        show_alert_message(getText(R.string.app_name).toString(), "Video error!");
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onTrackSelectionsChanged(TrackSelections<? extends MappingTrackSelector.MappedTrackInfo> trackSelections) {
    }

    private void show_alert_message(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);
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
}