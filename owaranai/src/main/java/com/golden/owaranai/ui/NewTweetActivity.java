package com.golden.owaranai.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.golden.owaranai.ApplicationController;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.StatusItem;

public class NewTweetActivity extends Activity {
    public static final String ARG_REPLY_TO_ID = "reply_to";
    public static final String INTENT_REPLY = "com.golden.owaranai.actions.REPLY";

    private ApplicationController controller;
    private StatusItem inReplyToStatus;

    private EditText viewTweetEdit;
    private TextView viewCharCounter;
    private View viewOriginalStatus;

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
        viewTweetEdit.addTextChangedListener(new TweetTextWatcher());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_tweet_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tweet:
                // TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
}
