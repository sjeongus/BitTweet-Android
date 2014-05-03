package com.golden.owaranai.internal;

import twitter4j.Status;

/**
 * Created by soomin on 3/26/2014.
 */
public class StatusItem {
    private String id;
    private Status status;
    private long myUserId;
    private boolean retweetOverride;
    private boolean favoriteOverride;

    public StatusItem(Status status, long myUserId) {
        this.myUserId = myUserId;
        this.id = String.valueOf(status.getId());
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isMention() {
        return status.getInReplyToUserId() == myUserId;
    }

    public boolean isRetweetedByMe() {
        return retweetOverride || status.isRetweetedByMe();
    }

    public void setRetweeted(boolean retweeted) {
        this.retweetOverride = retweeted;
    }

    public boolean isFavorited() {
        return favoriteOverride || status.isFavorited();
    }

    public void setFavorited(boolean favorited) {
        this.favoriteOverride = favorited;
    }
}
