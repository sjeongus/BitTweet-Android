package org.bittweet.android.internal;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;

public class TwitterStreamRouter {
    private TwitterStream stream;
    private Context context;
    private MyStreamListener listener;
    private List<TwitterStreamConsumer> consumers;
    private Handler handler;

    public TwitterStreamRouter(Context context) {
        this.context = context;
        this.consumers = new ArrayList<TwitterStreamConsumer>();
        this.handler = new Handler();
    }

    private void initializeStream() {
        stream = MyTwitterFactory.getInstance(context).getTwitterStream();
        listener = new MyStreamListener();
        stream.addListener(listener);
        stream.user();
    }

    public void registerConsumer(TwitterStreamConsumer consumer) {
        if(stream == null) {
            initializeStream();
        }

        consumers.add(consumer);
    }

    public void unregisterConsumer(TwitterStreamConsumer consumer) {
        consumers.remove(consumer);
    }

    private class MyStreamListener implements UserStreamListener {
        @Override
        public void onDeletionNotice(long directMessageId, long userId) {

        }

        @Override
        public void onFriendList(long[] friendIds) {

        }

        @Override
        public void onFavorite(User source, User target, Status favoritedStatus) {

        }

        @Override
        public void onUnfavorite(User source, User target, Status unfavoritedStatus) {

        }

        @Override
        public void onFollow(User source, User followedUser) {

        }

        @Override
        public void onUnfollow(User source, User unfollowedUser) {

        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {

        }

        @Override
        public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {

        }

        @Override
        public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {

        }

        @Override
        public void onUserListSubscription(User subscriber, User listOwner, UserList list) {

        }

        @Override
        public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {

        }

        @Override
        public void onUserListCreation(User listOwner, UserList list) {

        }

        @Override
        public void onUserListUpdate(User listOwner, UserList list) {

        }

        @Override
        public void onUserListDeletion(User listOwner, UserList list) {

        }

        @Override
        public void onUserProfileUpdate(User updatedUser) {

        }

        @Override
        public void onBlock(User source, User blockedUser) {

        }

        @Override
        public void onUnblock(User source, User unblockedUser) {

        }

        @Override
        public void onStatus(final Status status) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(TwitterStreamConsumer consumer : consumers) {
                        if(consumer.wantsStatus(status)) {
                            consumer.onStatus(status);
                        }
                    }
                }
            });
        }

        @Override
        public void onDeletionNotice(final StatusDeletionNotice statusDeletionNotice) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(TwitterStreamConsumer consumer : consumers) {
                        consumer.onStatusDelete(statusDeletionNotice);
                    }
                }
            });
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {

        }

        @Override
        public void onStallWarning(StallWarning warning) {

        }

        @Override
        public void onException(Exception ex) {

        }
    }
}
