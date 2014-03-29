package com.golden.owaranai.twitter;

import android.content.Context;
import android.util.Log;

import java.io.File;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by soomin on 3/1/14.
 */

// Class may not be necessary
public class MyTwitterFactory {
    private static final String TAG = "RoidRage/ResourceLoader";

    private static MyTwitterFactory singletonInstance = null;

    private Context mContext;

    private Twitter mTwitter;

    private MyTwitterFactory(Context context) {
        mContext = context;

        installHttpResponseCache();

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(SecretKeys.getCONSUMER_KEY());
        configurationBuilder.setOAuthConsumerSecret(SecretKeys.getCONSUMER_SECRET());
        configurationBuilder.setUseSSL(true);
        Configuration configuration = configurationBuilder.build();
        mTwitter = new TwitterFactory(configuration).getInstance();
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
        final File httpCacheDir = new File("/sdcard", "http");

        try {
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception httpResponseCacheNotAvailable) {
            Log.d(TAG, "android.net.http.HttpResponseCache not available, ");
        }
    }

    public Twitter getTwitter() {
        return mTwitter;
    }

    public void setTwitter(Twitter twitter) {
        mTwitter = twitter;
    }

}
