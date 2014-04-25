package com.golden.owaranai.internal;

import android.content.Context;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.util.List;
import java.util.Map;

// Retrieve the home timeline of the user
public class HomeTimelineContent extends GeneralTimelineContent {
    private static final String TAG = "HomeTimelineContent";

    public HomeTimelineContent(Context context, Map<String, StatusItem> globalStatusMap) {
        super(context, globalStatusMap);
    }

    @Override
    protected boolean canAddItem(StatusItem statusItem) {
        return true;
    }

    @Override
    protected List<Status> getUpdate(Paging paging) throws TwitterException {
        return getTwitter().getHomeTimeline(paging);
    }

    @Override
    protected List<Status> getMore(Paging paging) throws TwitterException {
        return getTwitter().getHomeTimeline(paging);
    }
}
