package com.golden.owaranai.internal;

import android.content.Context;
import twitter4j.*;

import java.util.List;
import java.util.Map;

// Retrieve the home timeline of the user
public class HomeTimelineContent extends GeneralTimelineContent {
    private Twitter twitter;
    private User user;
    private int morePage = 1;

    private static final String TAG = "HomeTimelineContent";

    public HomeTimelineContent(Context context, Map<String, StatusItem> globalStatusMap) {
        super(context, globalStatusMap);
        this.user = null;
    }

    @Override
    protected boolean canAddItem(StatusItem statusItem) {
        return true;
    }

    // Function that retrieves the user's timeline
    public void update() {
        try {
            twitter = MyTwitterFactory.getInstance(getContext()).getTwitter();
            user = twitter.verifyCredentials();
            Paging paging = new Paging(1, PER_PAGE);

            if(getStatuses().size() > 0) {
                // Fetch items newer than the latest status that we have
                paging.sinceId(Long.parseLong(getStatuses().get(0).getId()));
            }

            // Desc-ordered list of new items
            List<Status> temp = twitter.getHomeTimeline(paging);

            // Should prepend this list to the existing list...
            // We will iterate this from end to start, prepending each item
            // That way we will preserve descending order even on further calls
            for (int i = temp.size() - 1; i > -1; i--) {
                addItem(new StatusItem(temp.get(i), user.getId()), true);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void loadMore() {
        try {
            twitter = MyTwitterFactory.getInstance(getContext()).getTwitter();
            user = twitter.verifyCredentials();
            Paging paging = new Paging(++morePage, PER_PAGE);

            if(getStatuses().size() > 0) {
                // Fetch items older than the oldest status that we have
                paging.maxId(Long.parseLong(getStatuses().get(getStatuses().size() - 1).getId()));
            }

            // Desc-ordered list of older items
            List<Status> temp = twitter.getHomeTimeline(paging);

            // We need to append it to the end of our statuses array
            for(Status aTemp : temp) {
                addItem(new StatusItem(aTemp, user.getId()), false);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
}
