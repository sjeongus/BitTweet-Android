package com.golden.owaranai.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.golden.owaranai.GoHome;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.SecretKeys;
import com.golden.owaranai.ui.fragments.TweetsDetailFragment;
import com.golden.owaranai.ui.fragments.TweetsListFragment;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TweetsListActivity extends FragmentActivity implements TweetsListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private SharedPreferences mSharedPreferences;

    private static final String TAG = "TweetsListActivity";

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    public boolean isTwitterLoggedInAlready() {
        return mSharedPreferences.getBoolean(TwitterLoginActivity.PREF_KEY_TWITTER_LOGIN, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getSharedPreferences("MyTwitter", MODE_PRIVATE);

        if (!isTwitterLoggedInAlready()) {
            startActivity(new Intent(getApplicationContext(), TwitterLoginActivity.class));
            return;
        }

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

    // AsyncTask that tweets a random tweet on user's behalf?
    public class TweetTask extends AsyncTask<Void, Void, Void> {
        private User user;
        private String status;
        private Twitter twitter;

        @Override
        protected void onPreExecute() {
            String oauthKey = mSharedPreferences.getString(TwitterLoginActivity.PREF_KEY_OAUTH_TOKEN, "No user");
            String oauthSecret = mSharedPreferences.getString(TwitterLoginActivity.PREF_KEY_OAUTH_SECRET, "No secret");
            ConfigurationBuilder confBuild = new ConfigurationBuilder();

            confBuild.setOAuthAccessToken(oauthKey)
                    .setOAuthAccessTokenSecret(oauthSecret)
                    .setOAuthConsumerKey(SecretKeys.CONSUMER_KEY)
                    .setOAuthConsumerSecret(SecretKeys.CONSUMER_SECRET);

            twitter = new TwitterFactory(confBuild.build()).getInstance();
            user = null;
        }

        @Override
        protected Void doInBackground(Void... args) {
            try {
                user = twitter.verifyCredentials();
                Log.e(TAG, "Hello my name is " + user.getScreenName());
                status = GoHome.getStatus(user.getScreenName());
                twitter.updateStatus(status);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
