package com.golden.owaranai.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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

    private EditText viewTweetEdit;
    private TextView viewCharCounter;
    private TextView viewReplyText;
    private Button viewBtnPost;
    private Button viewBtnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet);
        getActionBar().hide();
        initializeResources();
    }

    private void initializeResources() {
        controller = (ApplicationController) getApplication();

        if(getIntent() != null && INTENT_REPLY.equals(getIntent().getAction())) {
            inReplyToStatus = controller.getStatus(getIntent().getStringExtra(ARG_REPLY_TO_ID));
        }

        viewTweetEdit = (EditText) findViewById(R.id.text);
        viewCharCounter = (TextView) findViewById(R.id.count);
        viewReplyText = (TextView) findViewById(R.id.reply_text);
        viewBtnPost = (Button) findViewById(R.id.btn_post_tweet);
        viewBtnCancel = (Button) findViewById(R.id.btn_cancel_tweet);

        viewTweetEdit.addTextChangedListener(new TweetTextWatcher());

        viewBtnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTweet();
            }
        });

        viewBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if(inReplyToStatus != null) {
            viewReplyText.setText("Reply to: " + inReplyToStatus.getStatus().getText());
            viewReplyText.setVisibility(View.VISIBLE);
        } else {
            viewReplyText.setVisibility(View.GONE);
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
