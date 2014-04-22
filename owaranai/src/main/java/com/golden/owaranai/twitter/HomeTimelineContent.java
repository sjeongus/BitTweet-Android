package com.golden.owaranai.twitter;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Retrieve the home timeline of the user
public class HomeTimelineContent implements TimelineContent {
    private List<StatusItem> statuses;
    private Map<String, StatusItem> statusMap;

    private SharedPreferences mSharedPreferences;
    private Twitter twitter;
    private User user;

    private static final String TAG = "HomeTimelineContent";

    public HomeTimelineContent(SharedPreferences preferences) {
        mSharedPreferences = preferences;
        statuses = new ArrayList<StatusItem>();
        statusMap = new HashMap<String, StatusItem>();
    }

    // Function to add an item to the List and the Map
    private void addItem(StatusItem status) {
        // Prepend to list
        statuses.add(0, status);

        // Store reference in hash map
        statusMap.put(status.id, status);
    }

    @Override
    public StatusItem getStatusItem(String id) {
        return statusMap.get(id);
    }

    @Override
    public StatusItem getStatusItemAt(int position) {
        return statuses.get(position);
    }

    @Override
    public List<StatusItem> getStatusItems() {
        return statuses;
    }

    // Function that retrieves the user's timeline
    public void update() {
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

            // Desc-ordered list of new items
            List<Status> temp = twitter.getHomeTimeline();

            // Should prepend this list to the existing list...
            // We will iterate this from end to start, prepending each item
            // That way we will preserve descending order even on further calls
            for (int i = temp.size() - 1; i > -1; i--) {
                StatusItem nItem = new StatusItem(temp.get(i));

                // Set the profile image in the StatusItem. If already in cache, load that instead.
                // Previously profile images loaded each time because we tried to load every time list item was created.
                String profileUrl = temp.get(i).getUser().getBiggerProfileImageURL();
                InputStream in = new java.net.URL(profileUrl).openStream();
                nItem.profilePic = BitmapFactory.decodeStream(in);
                addItem(nItem);
                //addItem(new StatusItem(temp.get(i)));
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
