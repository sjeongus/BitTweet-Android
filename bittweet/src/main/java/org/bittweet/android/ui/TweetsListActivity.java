package org.bittweet.android.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.crashlytics.android.Crashlytics;
import org.bittweet.android.R;
import org.bittweet.android.internal.MyTwitterFactory;
import org.bittweet.android.ui.fragments.HomeTweetsListFragment;
import org.bittweet.android.ui.fragments.MentionsTweetsListFragment;
import org.bittweet.android.ui.fragments.TweetsDetailFragment;
import org.bittweet.android.ui.fragments.TweetsListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.Twitter;

public class TweetsListActivity extends FragmentActivity implements TweetsListFragment.Callbacks {
    private boolean isTwoPane;

    private Twitter mTwitter;
    private SharedPreferences sharedPreferences;
    private FragmentManager fragmentManager;

    private static final String TAG = "TweetsListActivity";
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    public boolean isTwitterLoggedInAlready() {
        return sharedPreferences.getBoolean(TwitterLoginActivity.PREF_KEY_TWITTER_LOGIN, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);


        sharedPreferences = getSharedPreferences("MyTwitter", MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();

        if (!isTwitterLoggedInAlready()) {
            startActivity(new Intent(getApplicationContext(), TwitterLoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_tweets_list);
        initializeDrawer();

        if (findViewById(R.id.tweets_detail_container) != null) {
            isTwoPane = true;
        }

        loadHomeTimeline();
    }

    private void initializeDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.bittweet_ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        String[] items = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray icons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        List<Map<String, String>> navItems = new ArrayList<Map<String, String>>();
        Map<String, String> navItemTemp;

        for(int i = 0; i < items.length; i++) {
            navItemTemp = new HashMap<String, String>();
            navItemTemp.put("icon", String.valueOf(icons.getResourceId(i, -1)));
            navItemTemp.put("text", items[i]);
            navItems.add(navItemTemp);
        }

        drawerList.setAdapter(new SimpleAdapter(this, navItems, R.layout.navigation_item, new String[] { "icon", "text" }, new int[] { R.id.nav_icon, R.id.nav_text }));

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        loadHomeTimeline();
                        break;
                    case 1:
                        loadMentionsTimeline();
                        break;
                }

                drawerLayout.closeDrawer(drawerList);
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        icons.recycle();
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
                Intent newTweet = new Intent(this, NewTweetActivity.class);
                startActivity(newTweet);
                return true;
            case R.id.action_logout:
                logoutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.logout_title)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.logout_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sharedPreferences.edit().clear().commit();
                        mTwitter = MyTwitterFactory.getInstance(getApplicationContext()).getTwitter();
                        mTwitter.setOAuthAccessToken(null);
                        finish();
                        startActivity(getIntent());
                    }
                })
                .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object
        builder.show();
    }

    private void loadMentionsTimeline() {
        MentionsTweetsListFragment fragment = null;

        try {
            fragment = MentionsTweetsListFragment.class.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.tweets_list_container, fragment)
                .commit();


        fragment.setActivateOnItemClick(isTwoPane);
        getActionBar().setSubtitle(R.string.mentions_timeline);
    }

    private void loadHomeTimeline() {
        HomeTweetsListFragment fragment = null;

        try {
            fragment = HomeTweetsListFragment.class.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.tweets_list_container, fragment)
                .commit();

        fragment.setActivateOnItemClick(isTwoPane);
        getActionBar().setSubtitle(R.string.home_timeline);
    }
}
