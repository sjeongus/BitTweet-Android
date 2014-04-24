package com.golden.owaranai.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.golden.owaranai.GoHome;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.MyTwitterFactory;
import com.golden.owaranai.ui.fragments.HomeTweetsListFragment;
import com.golden.owaranai.ui.fragments.MentionsTweetsListFragment;
import com.golden.owaranai.ui.fragments.TweetsDetailFragment;
import com.golden.owaranai.ui.fragments.TweetsListFragment;
import twitter4j.Twitter;
import twitter4j.User;

public class TweetsListActivity extends FragmentActivity implements TweetsListFragment.Callbacks {
    private boolean isTwoPane;

    private SharedPreferences sharedPreferences;
    private FragmentManager fragmentManager;

    private static final String TAG = "TweetsListActivity";

    public boolean isTwitterLoggedInAlready() {
        return sharedPreferences.getBoolean(TwitterLoginActivity.PREF_KEY_TWITTER_LOGIN, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("MyTwitter", MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();

        if (!isTwitterLoggedInAlready()) {
            startActivity(new Intent(getApplicationContext(), TwitterLoginActivity.class));
            return;
        }

        setContentView(R.layout.activity_tweets_list);
        loadHomeTimeline();

        if (findViewById(R.id.tweets_detail_container) != null) {
            isTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((TweetsListFragment) fragmentManager
                    .findFragmentById(R.id.tweets_list_container))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (isTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(TweetsDetailFragment.ARG_ITEM_ID, id);

            TweetsDetailFragment fragment = new TweetsDetailFragment();
            fragment.setArguments(arguments);

            fragmentManager.beginTransaction()
                    .replace(R.id.tweets_detail_container, fragment)
                    .commit();
        } else {
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
            case R.id.action_home:
                loadHomeTimeline();
                return true;
            case R.id.action_mentions:
                loadMentionsTimeline();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMentionsTimeline() {
        MentionsTweetsListFragment mentionsTweetsListFragment = new MentionsTweetsListFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.tweets_list_container, mentionsTweetsListFragment)
                .commit();
    }

    private void loadHomeTimeline() {
        HomeTweetsListFragment homeTweetsListFragment = new HomeTweetsListFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.tweets_list_container, homeTweetsListFragment)
                .commit();
    }

    // AsyncTask that tweets a random tweet on user's behalf?
    public class TweetTask extends AsyncTask<Void, Void, Void> {
        private User user;
        private String status;
        private Twitter twitter;

        @Override
        protected void onPreExecute() {
            twitter = MyTwitterFactory.getInstance(TweetsListActivity.this).getTwitter();
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
