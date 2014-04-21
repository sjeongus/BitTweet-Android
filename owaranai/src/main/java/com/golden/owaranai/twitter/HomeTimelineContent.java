package com.golden.owaranai.twitter;

import android.content.SharedPreferences;
import android.util.Log;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Retrieve the home timeline of the user
public class HomeTimelineContent implements TimelineContent {
    public List<StatusItem> statuses = new ArrayList<StatusItem>();
    public Map<String, StatusItem> status_map = new HashMap<String, StatusItem>();

    private SharedPreferences mSharedPreferences;
    private Twitter twitter;
    private User user;

    private static final String TAG = "HomeTimelineContent";

    public HomeTimelineContent(SharedPreferences preferences) {
        mSharedPreferences = preferences;
    }

    // Function to add an item to the List and the Map
    public void addItem(StatusItem status) {
        statuses.add(status);
        status_map.put(status.id, status);
    }

    // Function that retrieves the user's timeline
    public void getTimeline() {
        try {
            String oauthKey = mSharedPreferences.getString(TwitterLoginActivity.PREF_KEY_OAUTH_TOKEN, "No user");
            String oauthSecret = mSharedPreferences.getString(TwitterLoginActivity.PREF_KEY_OAUTH_SECRET, "No secret");
            ConfigurationBuilder confBuild = new ConfigurationBuilder();

            confBuild.setOAuthAccessToken(oauthKey)
                    .setOAuthAccessTokenSecret(oauthSecret)
                    .setOAuthConsumerKey(SecretKeys.CONSUMER_KEY)
                    .setOAuthConsumerSecret(SecretKeys.CONSUMER_SECRET);

            twitter = new TwitterFactory(confBuild.build()).getInstance();
            user = twitter.verifyCredentials();

            List<Status> temp = twitter.getHomeTimeline();
            Log.e(TAG, "Got up to here!");

            for (int i = 0; i < temp.size(); i++) {
                addItem(new StatusItem(temp.get(i)));
            }
        } catch (TwitterException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to get timeline");
        }
    }
}
