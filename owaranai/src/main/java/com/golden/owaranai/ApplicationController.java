package com.golden.owaranai;

import android.app.Application;
import com.golden.owaranai.internal.*;

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
        homeTimelineContent = new HomeTimelineContent(this, statusMap);
        mentionsTimelineContent = new MentionsTimelineContent(this, statusMap);
        twitterStreamRouter = new TwitterStreamRouter(this);
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
}
