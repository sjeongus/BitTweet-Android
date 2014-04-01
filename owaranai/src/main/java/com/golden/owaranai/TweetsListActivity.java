package com.golden.owaranai;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.golden.owaranai.twitter.HomeTimelineContent;
import com.golden.owaranai.twitter.SecretKeys;
import com.golden.owaranai.twitter.TwitterLoginActivity;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import static com.golden.owaranai.twitter.HomeTimelineContent.statuses;


/**
 * An activity representing a list of Tweets. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TweetsDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TweetsListFragment} and the item details
 * (if present) is a {@link TweetsDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link TweetsListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class TweetsListActivity extends FragmentActivity
        implements TweetsListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static SharedPreferences mSharedPreferences;

    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    public boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getApplicationContext().getSharedPreferences("MyTwitter", MODE_PRIVATE);
        if (!isTwitterLoggedInAlready())
            startActivity(new Intent(getApplicationContext(), TwitterLoginActivity.class));
        setContentView(R.layout.activity_tweets_list);

        if (findViewById(R.id.tweets_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((TweetsListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.tweets_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link TweetsListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(TweetsDetailFragment.ARG_ITEM_ID, id);
            TweetsDetailFragment fragment = new TweetsDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tweets_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, TweetsDetailActivity.class);
            detailIntent.putExtra(TweetsDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tweet_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_tweet:
                new TweetTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // AsyncTask that executes getTimeline
    public class TweetTask extends AsyncTask<Void, Void, Void> {

        private User user;
        private String status;
        private Twitter twitter;

        public TweetTask() {}

        @Override
        protected void onPreExecute() {
            // before the network request begins, show a progress indicator
            //showProgressDialog("Fetching timeline...");
            String oauthkey = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "No user");
            String oauthsecret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "No secret");
            ConfigurationBuilder confbuild = new ConfigurationBuilder();
            confbuild.setOAuthAccessToken(oauthkey).setOAuthAccessTokenSecret(oauthsecret).setOAuthConsumerKey(SecretKeys.getCONSUMER_KEY()).setOAuthConsumerSecret(SecretKeys.getCONSUMER_SECRET());
            twitter = new TwitterFactory(confbuild.build()).getInstance();
            User user = null;
        }

        @Override
        protected Void doInBackground(Void... args) {
            try {
                user = twitter.verifyCredentials();
                GoHome ngh = new GoHome();
                status = ngh.getStatus(user.getScreenName());
                twitter.updateStatus(status);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

    }
}
