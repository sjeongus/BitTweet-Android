package com.golden.owaranai.twitter;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

// Retrieve the home timeline of the user
public class HomeTimelineContent implements TimelineContent {

    public List<StatusItem> statuses = new ArrayList<StatusItem>();
    public Map<String, StatusItem> status_map = new HashMap<String, StatusItem>();

    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";

    private SharedPreferences mSharedPreferences;
    private Twitter twitter;
    private User user;

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
            String oauthkey = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "No user");
            String oauthsecret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "No secret");
            ConfigurationBuilder confbuild = new ConfigurationBuilder();
            confbuild.setOAuthAccessToken(oauthkey).setOAuthAccessTokenSecret(oauthsecret)
                    .setOAuthConsumerKey(SecretKeys.getCONSUMER_KEY()).setOAuthConsumerSecret(SecretKeys.getCONSUMER_SECRET());
            Twitter twitter = new TwitterFactory(confbuild.build()).getInstance();
            User user = twitter.verifyCredentials();
            List<Status> temp = twitter.getHomeTimeline();
            System.out.println("Got up to here!");
            for (int i = 0; i < temp.size(); i++) {
                //System.out.println(temp.get(i).getText());
                StatusItem nItem = new StatusItem(temp.get(i));
                String profileUrl = temp.get(i).getUser().getBiggerProfileImageURL();
                InputStream in = new java.net.URL(profileUrl).openStream();
                nItem.profilePic = BitmapFactory.decodeStream(in);
                addItem(nItem);
            }
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit(-1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
