package com.golden.owaranai.twitter;

import android.content.SharedPreferences;
import android.os.AsyncTask;

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

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class HomeTimelineContent implements TimelineContent {

    /**
     * Usage: java twitter4j.examples.timeline.GetHomeTimeline
     *
     */
    public static List<StatusItem> statuses = new ArrayList<StatusItem>();
    public static Map<String, StatusItem> status_map = new HashMap<String, StatusItem>();

    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";

    public void addItem(StatusItem status) {
        statuses.add(status);
        status_map.put(status.id, status);
    }

    public void getTimeline(SharedPreferences mSharedPreferences) {
        try {
            String oauthkey = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "No user");
            String oauthsecret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "No secret");
            ConfigurationBuilder confbuild = new ConfigurationBuilder();
            confbuild.setOAuthAccessToken(oauthkey).setOAuthAccessTokenSecret(oauthsecret)
                    .setOAuthConsumerKey(SecretKeys.getCONSUMER_KEY()).setOAuthConsumerSecret(SecretKeys.getCONSUMER_SECRET());
            Twitter twitter = new TwitterFactory(confbuild.build()).getInstance();
            User user = twitter.verifyCredentials();
            List<Status> temp = twitter.getHomeTimeline();
            for (int i = 0; i < temp.size(); i++) {
                System.out.println(temp.get(i).getText());
                addItem(new StatusItem(temp.get(i)));
            }
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit(-1);
        }
    }

    public final class getTimelineTask extends AsyncTask<SharedPreferences, Void, Void> {
        public getTimelineTask() {}

        @Override
        protected void onPreExecute() {
            // before the network request begins, show a progress indicator
            //showProgressDialog("Fetching timeline...");
        }

        @Override
        protected Void doInBackground(SharedPreferences... mPreferences) {
            try {
                getTimeline(mPreferences[0]);
            } catch (Exception e) {
                System.out.println("Error in fetching home timeline.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // after the network request completes, hide the progress indicator
            //dismissProgressDialog();
            //showResult(tweets);
            System.out.println("Retrieved timeline! Printing...");
        }

    }
}
