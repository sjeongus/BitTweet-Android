package com.golden.owaranai.twitter;

import com.golden.owaranai.R;
import com.golden.owaranai.twitter.SecretKeys;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.app.Activity;
import android.widget.Toast;

import twitter4j.*;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterLoginActivity extends FragmentActivity {
    // Constants
    /**
     * Register your here app https://dev.twitter.com/apps/new and get your
     * consumer key and secret
     * */
    static String TWITTER_CONSUMER_KEY = SecretKeys.getCONSUMER_KEY();
    static String TWITTER_CONSUMER_SECRET = SecretKeys.getCONSUMER_SECRET();

    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

    static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

    private Button loginButton;

    // Twitter
    private Twitter twitter;
    private RequestToken requestToken;

    private Context context;

    private static SharedPreferences mSharedPreferences;
    private ConnectionDetector cd;
    AlertDialogManager alert = new AlertDialogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.login_fragment);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(TwitterLoginActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Check if twitter keys are set
        if(TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0) {
            // Internet Connection is not present
            alert.showAlertDialog(TwitterLoginActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
            // stop executing code by return
            return;
        }
        loginButton = (Button) findViewById(R.id.loginbutton);
        mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
        //getActionBar().hide();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loginToTwitter();
            }
        });
    }

    private void loginToTwitter() {
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();

            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            // user already logged into twitter
            Toast.makeText(getApplicationContext(), "Already Logged into twitter", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }
}
