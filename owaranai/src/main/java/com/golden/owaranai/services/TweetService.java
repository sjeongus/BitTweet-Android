package com.golden.owaranai.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.golden.owaranai.ApplicationController;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.MyTwitterFactory;
import com.golden.owaranai.internal.StatusItem;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetService extends Service {
    public static final String ACTION_FAV = "com.golden.owaranai.actions.favourite";
    public static final String ACTION_RT = "com.golden.owaranai.actions.retweet";
    public static final String ARG_TWEET_ID = "tweet_id";

    private Twitter twitter;
    private ApplicationController controller;

    public TweetService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        twitter = MyTwitterFactory.getInstance(this).getTwitter();
        controller = (ApplicationController) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) {
            return START_STICKY;
        }

        if(Intent.ACTION_SEND.equals(intent.getAction())) {
            String tweetText = intent.getStringExtra(Intent.EXTRA_TEXT);
            new ProcessTweetTask().execute(tweetText, intent.getStringExtra(ARG_TWEET_ID));
        } else if(ACTION_FAV.equals(intent.getAction())) {
            String tweetId = intent.getStringExtra(ARG_TWEET_ID);
            new FavTweetTask().execute(tweetId);
        } else if(ACTION_RT.equals(intent.getAction())) {
            String tweetId = intent.getStringExtra(ARG_TWEET_ID);
            new RtTweetTask(tweetId).execute();
        }

        return START_STICKY;
    }

    private void hideNotification() {
        stopForeground(true);
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOngoing(true);
        builder.setTicker("Updating status...");
        builder.setContentText("...");
        builder.setContentTitle("Updating status...");
        builder.setSmallIcon(R.drawable.ic_nav_timeline);
        startForeground(R.string.tweet_service_ongoing, builder.build());
    }

    private class ProcessTweetTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            showNotification();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                twitter.verifyCredentials();
                StatusUpdate update = new StatusUpdate(strings[0]);

                if(strings[1] != null) {
                    update.setInReplyToStatusId(Long.parseLong(strings[1]));
                }

                twitter.updateStatus(update);
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideNotification();
        }
    }

    private class FavTweetTask extends AsyncTask<String, Void, Status> {
        @Override
        protected twitter4j.Status doInBackground(String... strings) {
            twitter4j.Status favorited = null;

            try {
                twitter.verifyCredentials();


                if(controller.getStatus(strings[0]).getStatus().isFavorited()) {
                    favorited = twitter.destroyFavorite(Long.parseLong(strings[0]));
                } else {
                    favorited = twitter.createFavorite(Long.parseLong(strings[0]));
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return favorited;
        }

        @Override
        protected void onPostExecute(twitter4j.Status status) {
            if(status == null) {
                // Error, uguu
                return;
            }

            controller.getStatus(String.valueOf(status.getId())).setStatus(status);
            controller.notifyStatusChanged();
        }
    }

    private class RtTweetTask extends AsyncTask<Void, Void, Status> {
        private StatusItem statusContainer;

        public RtTweetTask(String id) {
            this.statusContainer = controller.getStatus(id);
        }

        @Override
        protected twitter4j.Status doInBackground(Void... voids) {
            twitter4j.Status retweeted = null;

            try {
                twitter.verifyCredentials();

                if(statusContainer.getStatus().isRetweeted()) {
                    twitter.destroyStatus(statusContainer.getStatus().getId());
                    retweeted = statusContainer.getStatus().getRetweetedStatus();
                } else {
                    retweeted = twitter.retweetStatus(statusContainer.getStatus().getId());
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return retweeted;
        }

        @Override
        protected void onPostExecute(twitter4j.Status status) {
            if(status == null) {
                // There was an error!
                return;
            }

            // WARNING! The following line damages the property of all the hash maps that use an Id to point to the status with that Id
            // Because it, based on need, replaces a status with a different status with a different Id
            statusContainer.setStatus(status);
            controller.notifyStatusChanged();
        }
    }
}
