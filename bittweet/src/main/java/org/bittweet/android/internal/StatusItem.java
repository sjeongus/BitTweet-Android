package org.bittweet.android.internal;

import twitter4j.Status;
import twitter4j.UserMentionEntity;

/**
 * Created by soomin on 3/26/2014.
 */
public class StatusItem {
    private String id;
    private Status status;
    private long myUserId;

    public StatusItem(Status status, long myUserId) {
        this.myUserId = myUserId;
        this.id = String.valueOf(status.getId());
        this.setStatus(status);
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isMention() {
        for (UserMentionEntity mId : getStatus().getUserMentionEntities()) {
            if (mId.getId() == myUserId) {
                return true;
            }
        }
        return false;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
