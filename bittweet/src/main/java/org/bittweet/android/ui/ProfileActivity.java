package org.bittweet.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import org.bittweet.android.R;
import org.bittweet.android.internal.MyTwitterFactory;
import org.bittweet.android.ui.util.LinkTouchMovementMethod;
import org.bittweet.android.ui.util.RoundedTransformation;
import org.bittweet.android.ui.util.TweetFormatter;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by soomin on 8/30/2014.
 */
public class ProfileActivity extends FragmentActivity {

    // TextViews
    private TextView whoText;
    private TextView userName;
    private TextView displayName;
    private TextView verified;
    private TextView protect;
    private TextView tweetCount;
    private TextView followerCount;
    private TextView followingCount;
    private TextView favoriteCount;
    private TextView userInfo;
    private TextView userUrl;
    private TextView userLocation;

    // ImageViews
    private ImageView avatar;
    private ImageView header;

    private LinearLayout countainer;

    private Context context;
    private Twitter twitter;
    private User myUser;
    private User user;
    private Relationship relationShip;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_profile);

        context = getApplicationContext();

        whoText = (TextView) findViewById(R.id.whotext);
        userName = (TextView) findViewById(R.id.username);
        displayName = (TextView) findViewById(R.id.displayname);
        verified = (TextView) findViewById(R.id.verifiedtext);
        protect = (TextView) findViewById(R.id.protectedtext);
        tweetCount = (TextView) findViewById(R.id.tweetnumber);
        followerCount = (TextView) findViewById(R.id.followernumber);
        followingCount = (TextView) findViewById(R.id.followingnumber);
        favoriteCount = (TextView) findViewById(R.id.favoritesnumber);
        userInfo = (TextView) findViewById(R.id.infotext);
        userUrl = (TextView) findViewById(R.id.urltext);
        userLocation = (TextView) findViewById(R.id.locationtext);

        avatar = (ImageView) findViewById(R.id.profilephoto);
        header = (ImageView) findViewById(R.id.headerphoto);

        countainer = (LinearLayout) findViewById(R.id.countainer);

        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class LoadTask extends AsyncTask<Void, Void, Void> {
        private String username;
        private boolean isVerified;
        private boolean isProtected;

        private long myUserId;
        private long userId;

        @Override
        protected Void doInBackground(Void... args) {
            twitter = MyTwitterFactory.getInstance(ProfileActivity.this).getTwitter();
            myUserId = MyTwitterFactory.getInstance(ProfileActivity.this).getUserId();
            username = getIntent().getStringExtra("USERNAME");
            try {
                user = twitter.showUser(username);
                userId = user.getId();
                relationShip = twitter.showFriendship(myUserId, userId);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            isVerified = user.isVerified();
            isProtected = user.isProtected();
            countainer.setVisibility(View.VISIBLE);

            userName.setText("@" + username);
            displayName.setText(user.getName());
            if (isVerified) {
                verified.setVisibility(View.VISIBLE);
            }
            if (isProtected) {
                protect.setVisibility(View.VISIBLE);
            }

            if (userId == myUserId) {
                whoText.setText(R.string.profile_status_me);
            } else {
                setTitle("@" + username);
                if (relationShip.isSourceFollowingTarget() && relationShip.isSourceFollowedByTarget()) {
                    whoText.setText(R.string.profile_status_mutual);
                } else if (relationShip.isSourceFollowedByTarget()) {
                    whoText.setText(R.string.profile_status_followed);
                } else {
                    whoText.setText(R.string.profile_status_notfollowed);
                }
            }

            Ion.with(avatar)
                    .resize(250, 250)
                    .transform(new RoundedTransformation(250, 0,
                            true, true, true, true))
                    .load(user.getOriginalProfileImageURLHttps());

            Ion.with(header).load(user.getProfileBannerURL());

            String myTweets = Integer.toString(user.getStatusesCount());
            String myFollowers = Integer.toString(user.getFollowersCount());
            String myFollowing = Integer.toString(user.getFriendsCount());
            String myFavorites = Integer.toString(user.getFavouritesCount());

            tweetCount.setText(myTweets);
            followerCount.setText(myFollowers);
            followingCount.setText(myFollowing);
            favoriteCount.setText(myFavorites);

            userInfo.setMovementMethod(new LinkTouchMovementMethod(false));
            userInfo.setText(TweetFormatter.formatDescriptionText(ProfileActivity.this, user));
            userUrl.setMovementMethod(new LinkTouchMovementMethod(false));
            userUrl.setText(TweetFormatter.formatUrlText(ProfileActivity.this, user.getURLEntity()));
            userLocation.setText(user.getLocation());
        }
    }
}
