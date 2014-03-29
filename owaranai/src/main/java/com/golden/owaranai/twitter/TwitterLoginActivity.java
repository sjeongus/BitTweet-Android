package com.golden.owaranai.twitter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.golden.owaranai.R;
import com.golden.owaranai.TweetsListActivity;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

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
        setContentView(R.layout.login_fragment);

        context = getApplicationContext();
        getActionBar().hide();
        twitter = MyTwitterFactory.getInstance(context).getTwitter();

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
        mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        //getActionBar().hide();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(view);
            }
        });
    }

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null &&
                uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
            new HandleAuthTask(uri).execute();
        }
    }

    public void loginUser(View view) {
        new RequestAuthTask().execute();
    }

    private class RequestAuthTask extends AsyncTask<Void, String, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... args) {
            String authUrl = "";
            try {
                requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                authUrl = requestToken.getAuthenticationURL();
            } catch (TwitterException e) {
                e.printStackTrace();
                System.out.print("Could not get authURL");
            }
            return authUrl;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(""))
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result)));
            else
                Toast.makeText(context, "Could not OAuth", Toast.LENGTH_SHORT).show();
        }
    }

    private class HandleAuthTask extends AsyncTask<Void, Void, Void> {

        private AccessToken accessToken = null;
        private Uri mUri;

        public HandleAuthTask(Uri uri) {
            accessToken = null;
            mUri = uri;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Uri uri = getIntent().getData();
            if (mUri != null && mUri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                // oAuth verifier
                String verifier = mUri
                        .getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

                try {
                    // Get the access token
                    accessToken = twitter.getOAuthAccessToken(
                            requestToken, verifier);
                } catch (TwitterException e) {
                    e.printStackTrace();
                    System.out.print("Could not get access token");
                }
                return null;
            }
            if (mUri == null)
                System.out.println("Uri is null");
            else
                System.out.println(mUri.toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (accessToken == null) {
                System.out.println("Token is null");
                return;
            }

            twitter.setOAuthAccessToken(accessToken);

            /* Save creds to preferences for future use */
            String token = accessToken.getToken();
            String secret = accessToken.getTokenSecret();

            // Shared Preferences
            SharedPreferences.Editor e = mSharedPreferences.edit();

            // After getting access token, access token secret
            // store them in application preferences
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            // Store login status - true
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.commit(); // save changes

            Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
            System.out.println("Twitter OAuth Token" + "> " + accessToken.getToken());

            Toast.makeText(context, "Logged In", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(context, TweetsListActivity.class));
        }
    }
}
