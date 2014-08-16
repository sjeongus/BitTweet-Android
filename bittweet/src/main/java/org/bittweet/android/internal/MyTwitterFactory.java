package org.bittweet.android.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.bittweet.android.ui.TwitterLoginActivity;

import java.io.File;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by soomin on 3/1/14.
 */

public class MyTwitterFactory {
    private static final String TAG = "MyTwitterFactory";
    private static MyTwitterFactory singletonInstance = null;

    private Context context;
    private Twitter twitter;
    private TwitterStream twitterStream;
    private Configuration conf;

    private MyTwitterFactory(Context context) {
        this.context = context;

        installHttpResponseCache();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        SharedPreferences prefs = context.getSharedPreferences("MyTwitter", 0);
        String oauthKey = prefs.getString(TwitterLoginActivity.PREF_KEY_OAUTH_TOKEN, null);
        String oauthSecret = prefs.getString(TwitterLoginActivity.PREF_KEY_OAUTH_SECRET, null);

        if(oauthKey != null) {
            configurationBuilder.setOAuthAccessToken(oauthKey);
        }

        if(oauthSecret != null) {
            configurationBuilder.setOAuthAccessTokenSecret(oauthSecret);
        }

        configurationBuilder.setOAuthConsumerKey(SecretKeys.CONSUMER_KEY);
        configurationBuilder.setOAuthConsumerSecret(SecretKeys.CONSUMER_SECRET);

        conf = configurationBuilder.build();

        twitter = new TwitterFactory(conf).getInstance();
        twitterStream = new TwitterStreamFactory(conf).getInstance();
    }

    public static MyTwitterFactory getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new MyTwitterFactory(context);
        }

        return singletonInstance;
    }

    /**
     * Setup the Android 4.0 HttpResponseCache
     */
    private void installHttpResponseCache() {
        final long httpCacheSize = 5 * 1024 * 1024; // 5 MiB
        final File httpCacheDir = new File(context.getCacheDir(), "http");

        try {
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
            Log.d(TAG, "android.net.http.HttpResponseCache not available, ");
        }
    }

    public Twitter getTwitter() {
        return twitter;
    }

    public TwitterStream getTwitterStream() {
        return twitterStream;
    }

    public Configuration getConfiguration() {
        return conf;
    }
}
