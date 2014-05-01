package com.golden.owaranai.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.MyTwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetService extends Service {
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
        if(intent == null || intent.getStringExtra(Intent.EXTRA_TEXT) == null) {
            return START_STICKY;
        }

        String tweetText = intent.getStringExtra(Intent.EXTRA_TEXT);
        new ProcessTweetTask().execute(tweetText);

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

    private void processTweet(String text) {
        try {
            twitter.verifyCredentials();
            twitter.updateStatus(text);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private class ProcessTweetTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            showNotification();
        }

        @Override
        protected Void doInBackground(String... strings) {
            processTweet(strings[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideNotification();
        }
    }
}
