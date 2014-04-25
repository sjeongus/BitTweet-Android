package com.golden.owaranai.internal;

import android.content.Context;
import twitter4j.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Retrieve the home timeline of the user
public class HomeTimelineContent implements TimelineContent {
    private List<StatusItem> statuses;
    private Map<String, StatusItem> statusMap;

    private Context context;
    private Twitter twitter;
    private User user;
    private int morePage = 1;

    private static final int PER_PAGE = 40;
    private static final String TAG = "HomeTimelineContent";

    public HomeTimelineContent(Context context) {
        this.context = context;
        this.statuses = new ArrayList<StatusItem>();
        this.statusMap = new HashMap<String, StatusItem>();
        this.user = null;
    }

    // Function to add an item to the List and the Map
    private void addItem(StatusItem status, boolean prepend) {
        if(statusMap.containsKey(status.getId())) {
            // Don't add more than once
            return;
        }

        // Prepend to list
        if(prepend) {
            statuses.add(0, status);
        } else {
            statuses.add(status);
        }

        // Store reference in hash map
        statusMap.put(status.getId(), status);
    }

    private void addItem(StatusItem status) {
        // Prepend by default
        addItem(status, true);
    }

    @Override
    public StatusItem getStatusItem(String id) {
        return statusMap.get(id);
    }

    @Override
    public StatusItem getStatusItemAt(int position) {
        return statuses.get(position);
    }

    @Override
    public List<StatusItem> getStatusItems() {
        return statuses;
    }

    // Function that retrieves the user's timeline
    public void update() {
        try {
            twitter = MyTwitterFactory.getInstance(context).getTwitter();
            user = twitter.verifyCredentials();
            Paging paging = new Paging(1, PER_PAGE);

            if(statuses.size() > 0) {
                // Fetch items newer than the latest status that we have
                paging.sinceId(Long.parseLong(statuses.get(0).getId()));
            }

            // Desc-ordered list of new items
            List<Status> temp = twitter.getHomeTimeline(paging);

            // Should prepend this list to the existing list...
            // We will iterate this from end to start, prepending each item
            // That way we will preserve descending order even on further calls
            for (int i = temp.size() - 1; i > -1; i--) {
                addItem(new StatusItem(temp.get(i), user.getId()));
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void loadMore() {
        try {
            twitter = MyTwitterFactory.getInstance(context).getTwitter();
            user = twitter.verifyCredentials();
            Paging paging = new Paging(++morePage, PER_PAGE);

            if(statuses.size() > 0) {
                // Fetch items older than the oldest status that we have
                paging.maxId(Long.parseLong(statuses.get(statuses.size() - 1).getId()));
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
