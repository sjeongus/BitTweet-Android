package org.bittweet.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import org.bittweet.android.R;
import org.bittweet.android.internal.MyTwitterFactory;
import org.bittweet.android.internal.SecretKeys;
import org.bittweet.android.ui.blur.BlurTask;
import org.bittweet.android.ui.blur.Utils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterLoginActivity extends FragmentActivity {
    // Constants
    static String TWITTER_CONSUMER_KEY = SecretKeys.CONSUMER_KEY;
    static String TWITTER_CONSUMER_SECRET = SecretKeys.CONSUMER_SECRET;

    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

    // Logging
    private static final String TAG = "TwitterLoginActivity";

    // Views
    private Button loginButton;
    private WebView webView;

    // Twitter
    private Twitter twitter;
    private RequestToken requestToken;

    // Misc
    private SharedPreferences sharedPreferences;
    private ImageView blurredOverlay;
    private Bitmap scaled;
    private View content;
    private BlurTask blurTask;
    private int width;
    private int height;
    private Drawable windowBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getActionBar().hide();

        int[] attrs = { android.R.attr.windowBackground };

        blurredOverlay = (ImageView) findViewById(R.id.blur_image);
        content = findViewById(R.id.backg);
        TypedValue outValue = new TypedValue();
        getApplicationContext().getTheme().resolveAttribute(android.R.attr.windowBackground, outValue, true);

        TypedArray style = getApplicationContext().getTheme().obtainStyledAttributes(outValue.resourceId, attrs);
        windowBackground = style.getDrawable(0);
        style.recycle();

        ViewTreeObserver vto = content.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                height = content.getMeasuredHeight();
                width = content.getMeasuredWidth();
                scaled = Utils.drawViewToBitmap(scaled, content, width, height, 5, windowBackground);
                blurTask = new BlurTask(getApplicationContext(), scaled, 7);
                return true;
            }
        });

        twitter = MyTwitterFactory.getInstance(this).getTwitter();
        loginButton = (Button) findViewById(R.id.button_login);
        webView = (WebView) findViewById(R.id.webview);
        sharedPreferences = getSharedPreferences("MyTwitter", MODE_PRIVATE);

        // When user clicks login button, should open up a web view that allows them to log in
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActionBar().show();
                new RequestAuthTask().execute();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();

        if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
            new HandleAuthTask(uri).execute();
        }
    }

    // AsyncTask that allows user to sign in to obtain API token
    private class RequestAuthTask extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... args) {
            String authUrl = "";

            try {
                // Get the request token using the callback URL
                requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                authUrl = requestToken.getAuthenticationURL();
            } catch (TwitterException e) {
                e.printStackTrace();
                Log.e(TAG, "Could not get authURL");
            }

            return authUrl;
        }

        @Override
        protected void onPostExecute(String result) {
            // If successful and the authURL was obtained, then start a web view that allows user to sign in.
            if (!result.equals("")) {
                displayLoginPage(result);
            } else {
                Toast.makeText(TwitterLoginActivity.this, "Could not OAuth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayLoginPage(String url) {
        loginButton.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }

    // AsyncTask that obtains the API access key and secret, and stores into SharedPreferences
    private class HandleAuthTask extends AsyncTask<Void, Void, Void> {
        private AccessToken accessToken = null;
        private Uri mUri;

        public HandleAuthTask(Uri uri) {
            accessToken = null;
            mUri = uri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mUri != null && mUri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                // oAuth verifier
                String verifier = mUri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

                try {
                    // Get the access token
                    accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                } catch (TwitterException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Could not get access token");
                }
            } else if (mUri == null) {
                Log.e(TAG, "Uri is null");
            } else {
                Log.e(TAG, mUri.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (accessToken == null) {
                // TODO: Display error
                Log.e(TAG, "Token is null");
                return;
            }

            twitter.setOAuthAccessToken(accessToken);

            /* Save credentials to preferences for future use */
            String token = accessToken.getToken();
            String secret = accessToken.getTokenSecret();

            // Shared Preferences
            SharedPreferences.Editor e = sharedPreferences.edit();

            // After getting access token, access token secret
            // store them in application preferences
            e.putString(PREF_KEY_OAUTH_TOKEN, token);
            e.putString(PREF_KEY_OAUTH_SECRET, secret);

            // Store login status - true
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.commit(); // save changes

            Log.e("Twitter OAuth Token", "> " + token);

            Toast.makeText(TwitterLoginActivity.this, "Logged In", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(TwitterLoginActivity.this, TweetsListActivity.class));
            finish();
        }
    }
}
