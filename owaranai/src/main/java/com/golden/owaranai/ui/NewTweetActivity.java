package com.golden.owaranai.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import com.golden.owaranai.ApplicationController;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.StatusItem;
import com.golden.owaranai.services.TweetService;
import com.golden.owaranai.ui.adapters.SimpleTweetAdapter;
import com.golden.owaranai.ui.adapters.TimelineAdapter;
import com.golden.owaranai.ui.adapters.TweetAdapter;
import twitter4j.UserMentionEntity;

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

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        viewTweetEdit = (EditText) findViewById(R.id.text);
        viewCharCounter = (TextView) findViewById(R.id.count);

        viewTweetEdit.addTextChangedListener(new TweetTextWatcher());
        viewTweetEdit.requestFocus();

        if(getIntent() != null && INTENT_REPLY.equals(getIntent().getAction())) {
            inReplyToStatus = controller.getStatus(getIntent().getStringExtra(ARG_REPLY_TO_ID));
            initializeReplyToStatus();
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void initializeReplyToStatus() {
        TimelineAdapter.ViewHolder holder = new TimelineAdapter.ViewHolder();
        TweetAdapter adapter = new SimpleTweetAdapter(this);
        ViewStub viewReplyStub = (ViewStub) findViewById(R.id.reply_container);
        View viewReply = viewReplyStub.inflate();

        holder.initialize(viewReply);
        adapter.recreateView(inReplyToStatus, holder);
        viewReply.setVisibility(View.VISIBLE);

        StringBuilder newMentionPrefix = new StringBuilder();

        newMentionPrefix.append("@").append(inReplyToStatus.getStatus().getUser().getScreenName());

        for(UserMentionEntity originalMention : inReplyToStatus.getStatus().getUserMentionEntities()) {
            if(originalMention.getScreenName().equals(inReplyToStatus.getStatus().getUser().getScreenName())) {
                // No duplicates pls!
                continue;
            }

            newMentionPrefix.append(" @").append(originalMention.getScreenName());
        }

        newMentionPrefix.append(" ");

        viewTweetEdit.setText(newMentionPrefix);
        viewTweetEdit.setSelection(newMentionPrefix.length());
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

        if(checkCharactersLeft(text) > -1) {
            Intent serviceIntent = new Intent(this, TweetService.class);
            serviceIntent.setAction(Intent.ACTION_SEND);
            serviceIntent.putExtra(Intent.EXTRA_TEXT, text);

            if(inReplyToStatus != null) {
                serviceIntent.putExtra(TweetService.ARG_TWEET_ID, inReplyToStatus.getId());
            }

            startService(serviceIntent);
            NavUtils.navigateUpFromSameTask(NewTweetActivity.this);
        }
    }

    private int checkCharactersLeft(CharSequence str) {
        return 140 - str.length();
    }

    private class TweetTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            viewCharCounter.setText(String.valueOf(checkCharactersLeft(charSequence)));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
