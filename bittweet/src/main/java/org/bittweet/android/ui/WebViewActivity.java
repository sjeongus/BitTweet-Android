package org.bittweet.android.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.bittweet.android.R;
import org.bittweet.android.internal.MyTwitterFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by soomin on 8/31/2014.
 */
public class WebViewActivity extends Activity {

    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    static final String TWITTER_CALLBACK_URL = "http://www.chromatiqa.org/bittweet/";

    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

    // Logging
    private static final String TAG = "TwitterLoginActivity";

    // Twitter
    private Twitter twitter;
    private RequestToken requestToken;

    // Misc
    private SharedPreferences sharedPreferences;
    private WebView webView;
    private CookieManager cookieManager;
    private boolean login = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // request the progress-bar feature for the activity
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        // Show the Up button in the action bar.
        ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);

        CookieSyncManager.createInstance(getApplicationContext());
        cookieManager = CookieManager.getInstance();

        twitter = MyTwitterFactory.getInstance(this).getTwitter();
        webView = (WebView) findViewById(R.id.webview);
        sharedPreferences = getSharedPreferences("MyTwitter", MODE_PRIVATE);

        if (getIntent().getBooleanExtra("LOGIN", false)) {
            System.err.println("LOGIN received. Starting oauth");
            login = true;
            setTitle(R.string.login_title);
            new RequestAuthTask().execute();
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.getSettings().setJavaScriptEnabled(true);
            //webView.loadUrl(getIntent().getDataString());
            if (getIntent().getBooleanExtra("URL", false)) {
                webView.loadUrl(getIntent().getDataString());
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        WebViewActivity.this.setTitle(view.getTitle());
                    }

                    public void onProgressChanged(WebView view, int progress) {
                        // update the progressBar
                        WebViewActivity.this.setProgress(progress * 100);
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (login) {
            webView.clearHistory();
            webView.clearFormData();
            webView.clearCache(true);

            cookieManager.removeAllCookie();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (login) {
            System.err.println("Back pressed! Going back to login screen.");
            finish();
            Intent intent = new Intent(WebViewActivity.this, TwitterLoginActivity.class);
            startActivity(intent);
            return;
        }
        // Check if the key event was the Back button and if there's history
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
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
                System.err.println("Could not get authURL");
            }

            return authUrl;
        }

        @Override
        protected void onPostExecute(String result) {
            // If successful and the authURL was obtained, then start a web view that allows user to sign in.
            if (!result.equals("")) {
                displayLoginPage(result);
            } else {
                Toast.makeText(WebViewActivity.this, "Could not OAuth. Restart the app completely.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayLoginPage(String url) {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(url);
                System.err.println("Uri is " + uri);

                if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL) && login) {
                    new HandleAuthTask(uri).execute();
                }
                //intent.setData(uri);
                //onNewIntent(intent);

                return super.shouldOverrideUrlLoading(view, url);
            }
        }); webView.loadUrl(url);
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
                    System.err.println("Could not get access token");
                    return null;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (mUri == null) {
                System.err.println("Uri is null");
            } else {
                System.err.println(mUri.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (accessToken == null) {
                // TODO: Display error
                System.err.println("Token is null");
                return;
            }

            twitter.setOAuthAccessToken(accessToken);

            // Save credentials to preferences for future use
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
            System.err.println("Oauth token is " + token);

            Toast.makeText(WebViewActivity.this, "Logged In", Toast.LENGTH_SHORT).show();

            finish();
            Intent intent = new Intent(WebViewActivity.this, TwitterLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        }
    }
}
