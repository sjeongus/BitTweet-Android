package org.bittweet.android.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;

import org.bittweet.android.ApplicationController;
import org.bittweet.android.internal.MyTwitterFactory;
import org.bittweet.android.internal.StatusItem;

import java.io.File;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UploadedMedia;

import static org.bittweet.android.ui.util.ImageUtils.getBitmapDimensions;
import static org.bittweet.android.ui.util.ImageUtils.getResizedBitmapFile;

public class TweetService extends Service {
    public static final String ACTION_FAV = "org.bittweet.android.actions.favourite";
    public static final String ACTION_RT = "org.bittweet.android.actions.retweet";
    public static final String ARG_TWEET_ID = "tweet_id";

    private SharedPreferences twitPref;

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

        twitPref = getSharedPreferences("MyTwitter", MODE_PRIVATE);
        twitter = MyTwitterFactory.getInstance(TweetService.this).getTwitter();
        controller = (ApplicationController) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) {
            return START_STICKY;
        }

        if(Intent.ACTION_SEND.equals(intent.getAction())) {
            String tweetText = intent.getStringExtra(Intent.EXTRA_TEXT);
            String imageString[] = new String[4];
            // Get the string extra to check if an image has been attached. Set empty if none
            if (intent.getStringArrayExtra("IMAGE_ARRAY") != null) {
                imageString = intent.getStringArrayExtra("IMAGE_ARRAY");
            }
            new ProcessTweetTask(imageString).execute(tweetText, intent.getStringExtra(ARG_TWEET_ID));
        } else if(ACTION_FAV.equals(intent.getAction())) {
            String tweetId = intent.getStringExtra(ARG_TWEET_ID);
            new FavTweetTask().execute(tweetId);
        } else if(ACTION_RT.equals(intent.getAction())) {
            String tweetId = intent.getStringExtra(ARG_TWEET_ID);
            new RtTweetTask(tweetId).execute();
        }

        return START_STICKY;
    }

    // Function that sends a broadcast to TweetsListActivity of results of a tweet
    private void notifier(boolean start, boolean success) {
        Intent intent = new Intent();
        intent.setAction("org.bittweet.android.services.TweetService");
        if (start) {
            twitPref.edit().putString("TWEET_SEND", "sending").apply();
        } else if (success) {
            twitPref.edit().putString("TWEET_SEND", "sent").apply();
        } else {
            twitPref.edit().putString("TWEET_SEND", "error").apply();
        }
        System.err.println("Tweet sent broadcast sent!");
        sendBroadcast(intent);
    }

    // Asynchronously send a tweet
    private class ProcessTweetTask extends AsyncTask<String, Void, Boolean> {

        private String[] uriPath;
        private int count;

        public ProcessTweetTask(String[] uriString) {
            uriPath = new String[4];
            count = 0;
            for (int i = 0; i < uriPath.length; i++) {
                if (uriString[i] != null) {
                    count++;
                    uriPath[i] = uriString[i];
                }
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            // Notify in beginning of status updating
            notifier(true, false);
            StatusUpdate update;
            try {
                twitter.verifyCredentials();
                update = new StatusUpdate(strings[0]);
                // Check if tweet is a mention, and set to mention
                if (strings[1] != null) {
                    update.setInReplyToStatusId(Long.parseLong(strings[1]));
                }
                if (count > 0) {
                    // Check if an image is attached, and attach to the tweet
                    long[] mediaIds = new long[count];
                    int i = 0;
                    for (String uri : uriPath) {
                        if (uri != null) {
                            System.err.println("Set " + uri);
                            //File f = new File(uri);
                            int[] dimens = getBitmapDimensions(uri);
                            File f;
                            if (dimens[0] > 2048) {
                                System.err.println("Resizing width");
                                f = getResizedBitmapFile(getApplicationContext(), uri,
                                        "image" + i, 2048, dimens[1] * 2048/dimens[0]);
                            } else if (dimens[1] > 2048) {
                                System.err.println("Resizing height");
                                f = getResizedBitmapFile(getApplicationContext(), uri,
                                        "image" + i, dimens[0] * 2048/dimens[1], 2048);
                            } else {
                                f = new File(uri);
                            }
                            UploadedMedia media = twitter.uploadMedia(f);
                            mediaIds[i] = media.getMediaId();
                            i++;
                        }
                    }
                    update.setMediaIds(mediaIds);
                }
                twitter.updateStatus(update);
            } catch (TwitterException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // notify at end of result
            notifier(false, result);
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
                // Error, Display a Crouton informing user favorite failed
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
                // There was an error! Inform user with Crouton that retweet failed
                return;
            }

            // WARNING! The following line damages the property of all the hash maps that use an Id to point to the status with that Id
            // Because it, based on need, replaces a status with a different status with a different Id
            statusContainer.setStatus(status);
            controller.notifyStatusChanged();
        }
    }
}
