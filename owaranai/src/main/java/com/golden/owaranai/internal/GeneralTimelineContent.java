package com.golden.owaranai.internal;

import android.content.Context;
import android.widget.BaseAdapter;
import com.golden.owaranai.ApplicationController;
import com.golden.owaranai.ui.adapters.TimelineAdapter;
import twitter4j.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GeneralTimelineContent implements TimelineContent, TwitterStreamConsumer {
    private Context context;
    private Map<String, StatusItem> globalStatusMap;
    private Map<String, StatusItem> statusMap;
    private List<StatusItem> statuses;
    private BaseAdapter adapter;

    private Twitter twitter;
    private User user;

    protected static final int PER_PAGE = 40;
    private int morePage;

    public GeneralTimelineContent(Context context, Map<String, StatusItem> globalStatusMap) {
        this.context = context;
        this.globalStatusMap = globalStatusMap;
        this.statuses = new ArrayList<StatusItem>();
        this.statusMap = new HashMap<String, StatusItem>();
        this.morePage = 1;
    }

    protected Context getContext() {
        return context;
    }

    protected Map<String, StatusItem> getGlobalStatusMap() {
        return globalStatusMap;
    }

    protected Map<String, StatusItem> getStatusMap() {
        return statusMap;
    }

    protected List<StatusItem> getStatuses() {
        return statuses;
    }

    protected Twitter getTwitter() {
        return twitter;
    }

    protected User getUser() {
        return user;
    }

    protected abstract boolean canAddItem(StatusItem statusItem);

    protected void addItem(StatusItem statusItem, boolean prepend) {
        if(statusMap.containsKey(statusItem.getId()) || !canAddItem(statusItem)) {
            return;
        }

        if(prepend) {
            statuses.add(0, statusItem);
        } else {
            statuses.add(statusItem);
        }

        statusMap.put(statusItem.getId(), statusItem);
        globalStatusMap.put(statusItem.getId(), statusItem);
    }


    @Override
    public StatusItem getStatusItem(String id) {
        return getStatusMap().get(id);
    }

    @Override
    public StatusItem getStatusItemAt(int position) {
        return getStatuses().get(position);
    }

    @Override
    public List<StatusItem> getStatusItems() {
        return getStatuses();
    }

    protected abstract List<Status> getUpdate(Paging paging) throws TwitterException;

    protected abstract List<Status> getMore(Paging paging) throws TwitterException;

    @Override
    public void update() {
        try {
            twitter = MyTwitterFactory.getInstance(getContext()).getTwitter();
            user = twitter.verifyCredentials();
            Paging paging = new Paging(1, PER_PAGE);

            if(getStatuses().size() > 0) {
                // Fetch items newer than the latest status that we have
                paging.sinceId(Long.parseLong(getStatuses().get(0).getId()));
            }

            List<Status> temp = getUpdate(paging);

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

    @Override
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
            List<Status> temp = getMore(paging);

            // We need to append it to the end of our statuses array
            for(Status aTemp : temp) {
                addItem(new StatusItem(aTemp, user.getId()), false);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void attachStreamToAdapter(final TimelineAdapter adapter) {
        this.adapter = adapter;
        TwitterStreamRouter router = ((ApplicationController) context).getTwitterStreamRouter();
        router.registerConsumer(this);
    }

    public void detachStream() {
        TwitterStreamRouter router = ((ApplicationController) context).getTwitterStreamRouter();
        router.unregisterConsumer(this);
    }

    protected BaseAdapter getAdapter() {
        return adapter;
    }
}
