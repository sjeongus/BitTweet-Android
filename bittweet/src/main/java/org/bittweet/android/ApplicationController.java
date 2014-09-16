package org.bittweet.android;

import android.app.Application;

import org.bittweet.android.internal.HomeTimelineContent;
import org.bittweet.android.internal.MentionsTimelineContent;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.internal.TimelineContent;
import org.bittweet.android.internal.TwitterStreamRouter;

import java.util.HashMap;

public class ApplicationController extends Application {
    private TimelineContent homeTimelineContent;
    private TimelineContent mentionsTimelineContent;
    private TwitterStreamRouter twitterStreamRouter;
    private HashMap<String, StatusItem> statusMap;

    @Override
    public void onCreate() {
        super.onCreate();

        statusMap = new HashMap<String, StatusItem>();
        homeTimelineContent = new HomeTimelineContent(ApplicationController.this, statusMap);
        mentionsTimelineContent = new MentionsTimelineContent(ApplicationController.this, statusMap);
        twitterStreamRouter = new TwitterStreamRouter(ApplicationController.this);
    }

    public TimelineContent getHomeTimelineContent() {
        return homeTimelineContent;
    }

    public TimelineContent getMentionsTimelineContent() {
        return mentionsTimelineContent;
    }

    public StatusItem getStatus(String id) {
        return statusMap.get(id);
    }

    public TwitterStreamRouter getTwitterStreamRouter() {
        return twitterStreamRouter;
    }

    public void notifyStatusChanged() {
        homeTimelineContent.notifyDataSetChanged();
        mentionsTimelineContent.notifyDataSetChanged();
    }
}
