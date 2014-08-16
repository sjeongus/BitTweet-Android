package org.bittweet.android.internal;

import android.content.Context;

import java.util.List;
import java.util.Map;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;

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

    @Override
    public boolean wantsStatus(Status status) {
        return true;
    }

    @Override
    public void onStatus(Status status) {
        addItem(new StatusItem(status, getUser().getId()), true);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onStatusDelete(StatusDeletionNotice statusDeletionNotice) {
        String statusId = String.valueOf(statusDeletionNotice.getStatusId());
        StatusItem status = getGlobalStatusMap().get(statusId);

        getStatuses().remove(status);

        getStatusMap().remove(statusId);
        getGlobalStatusMap().remove(statusId);

        getAdapter().notifyDataSetChanged();
    }
}
