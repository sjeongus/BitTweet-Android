package org.bittweet.android.internal;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;

public interface TwitterStreamConsumer {
    public boolean wantsStatus(Status status);
    public void onStatus(Status status);
    public void onStatusDelete(StatusDeletionNotice statusDeletionNotice);
}
