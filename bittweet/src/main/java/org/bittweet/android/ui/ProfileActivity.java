package org.bittweet.android.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
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

        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    public class LoadTask extends AsyncTask<Void, Void, Void> {
        private int tweets;
        private int followers;
        private int following;
        private int favorites;
        private String about;
        private String url;
        private String location;

        @Override
        protected Void doInBackground(Void... args) {
            twitter = MyTwitterFactory.getInstance(ProfileActivity.this).getTwitter();
            long myUserId = MyTwitterFactory.getInstance(ProfileActivity.this).getUserId();
            long userId = getIntent().getLongExtra("USER", 0);
            try {
                twitter.verifyCredentials();
                myUser = twitter.showUser(myUserId);
                user = twitter.showUser(userId);
                relationShip = twitter.showFriendship(myUserId, userId);
                tweets = user.getStatusesCount();
                followers = user.getFollowersCount();
                following = user.getFriendsCount();
                favorites = user.getFavouritesCount();
                about = user.getDescription();
                url = user.getURL();
                location = user.getLocation();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (user == myUser) {
                whoText.setText(R.string.profile_status_me);
                whoText.setVisibility(View.VISIBLE);
            } else if (relationShip.isSourceFollowedByTarget() && !relationShip.isSourceFollowingTarget()) {
                whoText.setText(R.string.profile_status_followed);
                whoText.setVisibility(View.VISIBLE);
            } else if (!relationShip.isSourceFollowedByTarget() && relationShip.isSourceFollowingTarget()) {
                whoText.setText(R.string.profile_status_following);
                whoText.setVisibility(View.VISIBLE);
            } else if (relationShip.isSourceFollowingTarget() && relationShip.isSourceFollowingTarget()) {
                whoText.setText(R.string.profile_status_mutual);
                whoText.setVisibility(View.VISIBLE);
            } else {
                // Do nothing because we don't need to display this
            }

            Ion.with(avatar)
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
