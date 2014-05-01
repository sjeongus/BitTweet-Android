package com.golden.owaranai.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import com.golden.owaranai.ApplicationController;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.MyTwitterFactory;
import com.golden.owaranai.internal.StatusItem;
import twitter4j.Twitter;
import twitter4j.User;

public class NewTweetActivity extends Activity {
    public static final String ARG_REPLY_TO_ID = "reply_to";
    public static final String INTENT_REPLY = "com.golden.owaranai.actions.REPLY";

    private ApplicationController controller;
    private StatusItem inReplyToStatus;
    private ActionBar actionBar;

    private EditText viewTweetEdit;
    private TextView viewCharCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet);
        initializeResources();
    }

    private void initializeResources() {
        controller = (ApplicationController) getApplication();
        actionBar = getActionBar();

        if(getIntent() != null && INTENT_REPLY.equals(getIntent().getAction())) {
            inReplyToStatus = controller.getStatus(getIntent().getStringExtra(ARG_REPLY_TO_ID));
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);

        viewTweetEdit = (EditText) findViewById(R.id.text);
        viewCharCounter = (TextView) findViewById(R.id.count);

        viewTweetEdit.addTextChangedListener(new TweetTextWatcher());
        viewTweetEdit.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_tweet_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_tweet:
                sendTweet();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendTweet() {
        String text = viewTweetEdit.getText().toString();
        new TweetTask().execute(text);
    }

    private class TweetTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            viewCharCounter.setText(String.valueOf(140 - charSequence.length()));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private class TweetTask extends AsyncTask<String, Void, Void> {
        private User user;
        private Twitter twitter;

        @Override
        protected void onPreExecute() {
            twitter = MyTwitterFactory.getInstance(NewTweetActivity.this).getTwitter();
            user = null;
        }

        @Override
        protected Void doInBackground(String... args) {
            String status = args[0];
            try {
                user = twitter.verifyCredentials();
                twitter.updateStatus(status);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            finish();
        }
    }
}
