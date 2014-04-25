package com.golden.owaranai.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    private ActionBarDrawerToggle drawerToggle;

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
        initializeDrawer();
        getActionBar().setSubtitle(R.string.home_timeline);
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

    private void initializeDrawer() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.setDrawerListener(drawerToggle);
        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.navigation_item, new String[] { "Home", "Mentions" }));

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        loadHomeTimeline();
                        getActionBar().setSubtitle(R.string.home_timeline);
                        break;
                    case 1:
                        loadMentionsTimeline();
                        getActionBar().setSubtitle(R.string.mentions_timeline);
                        break;
                }
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
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
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_tweet:
                new TweetTask().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMentionsTimeline() {
        MentionsTweetsListFragment mentionsTweetsListFragment = new MentionsTweetsListFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.tweets_list_container, mentionsTweetsListFragment)
                .addToBackStack("ToMentions")
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
