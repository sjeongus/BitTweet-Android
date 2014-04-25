package com.golden.owaranai.internal;

import android.content.Context;
import android.os.Handler;
import com.golden.owaranai.ui.adapters.TimelineAdapter;
import twitter4j.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GeneralTimelineContent implements TimelineContent {
    private Context context;
    private Map<String, StatusItem> globalStatusMap;
    private Map<String, StatusItem> statusMap;
    private List<StatusItem> statuses;

    private Twitter twitter;
    private User user;
    private TwitterStream stream;

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
        stream = MyTwitterFactory.getInstance(context).getTwitterStream();
        final Handler handler = new Handler();
        adapter.setStatuses(statuses);

        final UserStreamListener listener = new UserStreamListener() {
            @Override
            public void onDeletionNotice(long l, long l2) {

            }

            @Override
            public void onFriendList(long[] longs) {

            }

            @Override
            public void onFavorite(User user, User user2, Status status) {

            }

            @Override
            public void onUnfavorite(User user, User user2, Status status) {

            }

            @Override
            public void onFollow(User user, User user2) {

            }

            @Override
            public void onUnfollow(User user, User user2) {

            }

            @Override
            public void onDirectMessage(DirectMessage directMessage) {

            }

            @Override
            public void onUserListMemberAddition(User user, User user2, UserList userList) {

            }

            @Override
            public void onUserListMemberDeletion(User user, User user2, UserList userList) {

            }

            @Override
            public void onUserListSubscription(User user, User user2, UserList userList) {

            }

            @Override
            public void onUserListUnsubscription(User user, User user2, UserList userList) {

            }

            @Override
            public void onUserListCreation(User user, UserList userList) {

            }

            @Override
            public void onUserListUpdate(User user, UserList userList) {

            }

            @Override
            public void onUserListDeletion(User user, UserList userList) {

            }

            @Override
            public void onUserProfileUpdate(User user) {

            }

            @Override
            public void onBlock(User user, User user2) {

            }

            @Override
            public void onUnblock(User user, User user2) {

            }

            @Override
            public void onStatus(Status status) {
                addItem(new StatusItem(status, user.getId()), true);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                String statusId = String.valueOf(statusDeletionNotice.getStatusId());
                StatusItem statusToDelete = statusMap.get(statusId);

                statuses.remove(statusToDelete);
                statusMap.remove(statusId);
                globalStatusMap.remove(statusId);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l2) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }
        };

        stream.addListener(listener);
        stream.user();
    }

    public void detachStream() {
        if(stream == null) {
            return;
        }

        stream.shutdown();
        stream.cleanUp();
        stream = null;
    }
}
