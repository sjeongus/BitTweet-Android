package com.golden.owaranai.internal;

import android.content.Context;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.util.List;
import java.util.Map;

public class MentionsTimelineContent extends GeneralTimelineContent {
    private static final String TAG = "MentionsTimelineContent";

    public MentionsTimelineContent(Context context, Map<String, StatusItem> globalStatusMap) {
        super(context, globalStatusMap);
    }

    @Override
    protected boolean canAddItem(StatusItem statusItem) {
        return statusItem.getStatus().getUser().getId() != getUser().getId();
    }

    @Override
    protected List<Status> getUpdate(Paging paging) throws TwitterException {
        return getTwitter().getMentionsTimeline(paging);
    }

    @Override
    protected List<Status> getMore(Paging paging) throws TwitterException {
        return getTwitter().getMentionsTimeline(paging);
    }
}
