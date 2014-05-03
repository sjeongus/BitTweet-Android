package com.golden.owaranai.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.golden.owaranai.ApplicationController;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.MyTwitterFactory;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetService extends Service {
    public static final String ACTION_FAV = "com.golden.owaranai.actions.favourite";
    public static final String ACTION_RT = "com.golden.owaranai.actions.retweet";
    public static final String ARG_TWEET_ID = "tweet_id";

    private Twitter twitter;

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
            new RtTweetTask().execute(tweetId);
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

    private class FavTweetTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                twitter.verifyCredentials();
                twitter.createFavorite(Long.parseLong(strings[0]));
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return strings[0];
        }

        @Override
        protected void onPostExecute(String id) {
            ((ApplicationController) getApplication()).getStatus(id).setFavorited(true);
        }
    }

    private class RtTweetTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                twitter.verifyCredentials();
                twitter.retweetStatus(Long.parseLong(strings[0]));
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return strings[0];
        }

        @Override
        protected void onPostExecute(String id) {
            ((ApplicationController) getApplication()).getStatus(id).setRetweeted(true);
        }
    }
}
