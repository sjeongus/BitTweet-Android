package org.bittweet.android.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import org.bittweet.android.R;
import org.bittweet.android.internal.MyTwitterFactory;
import org.bittweet.android.ui.util.RoundedTransformation;

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
        verified = (TextView) findViewById(R.id.displayname);
        protect = (TextView) findViewById(R.id.displayname);
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

    public class LoadTask extends AsyncTask<Void, Void, Void> {
        private String username;
        private String displayname;
        private boolean isVerified;
        private boolean isProtected;
        private int tweets;
        private int followers;
        private int following;
        private int favorites;
        private String about;
        private String url;
        private String location;

        private long myUserId;
        private long userId;

        @Override
        protected Void doInBackground(Void... args) {
            twitter = MyTwitterFactory.getInstance(ProfileActivity.this).getTwitter();
            myUserId = MyTwitterFactory.getInstance(ProfileActivity.this).getUserId();
            //userId = getIntent().getLongExtra("USERID", 0);
            username = getIntent().getStringExtra("USERNAME");
            try {
                //twitter.verifyCredentials();
                //myUser = twitter.showUser(myUserId);
                user = twitter.showUser(username);
                userId = user.getId();
                username = user.getScreenName();
                displayname = user.getName();
                isVerified = user.isVerified();
                isProtected = user.isProtected();
                relationShip = twitter.showFriendship(myUserId, userId);
                tweets = user.getStatusesCount();
                followers = user.getFollowersCount();
                following = user.getFriendsCount();
                favorites = user.getFavouritesCount();
                about = user.getDescription();
                url = user.getURLEntity().getDisplayURL();
                location = user.getLocation();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            countainer.setVisibility(View.VISIBLE);

            userName.setText("@" + username);
            displayName.setText(displayname);
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
                    .transform(new RoundedTransformation(250, 0))
                    .load(user.getOriginalProfileImageURLHttps());

            Ion.with(header).load(user.getProfileBannerURL());

            String myTweets = Integer.toString(tweets);
            String myFollowers = Integer.toString(followers);
            String myFollowing = Integer.toString(following);
            String myFavorites = Integer.toString(favorites);

            tweetCount.setText(myTweets);
            followerCount.setText(myFollowers);
            followingCount.setText(myFollowing);
            favoriteCount.setText(myFavorites);

            userInfo.setText(about);
            userUrl.setText(url);
            userLocation.setText(location);
        }
    }
}
