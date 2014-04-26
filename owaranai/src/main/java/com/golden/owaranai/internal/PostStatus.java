package com.golden.owaranai.internal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.golden.owaranai.ApplicationController;
import com.golden.owaranai.GoHome;
import com.golden.owaranai.R;

import twitter4j.Twitter;
import twitter4j.User;

/**
 * Created by soomin on 4/25/2014.
 */
// Class for posting a new tweet
public class PostStatus extends DialogFragment {

    private Context mContext;
    private String tweet_text = "";

    public static PostStatus newInstance() {
        PostStatus stat = new PostStatus();
        Bundle args = new Bundle();
        return stat;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        // Build a dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialog = inflater.inflate(R.layout.dialog_poststatus, null);

        builder.setView(dialog);
        final EditText tweet = (EditText) dialog.findViewById(R.id.text);
        final TextView counter = (TextView) dialog.findViewById(R.id.count);

        TextWatcher textWatch = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int len = s.length();
                int remain = 140 - s.length();
                counter.setText(String.valueOf(remain));
            }

            public void afterTextChanged(Editable s) {
            }
        };

        tweet.addTextChangedListener(textWatch);

        builder.setPositiveButton("Tweet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tweet_text = tweet.getText().toString();
                new TweetTask().execute(tweet_text);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    // AsyncTask that tweets a random tweet on user's behalf?
    public class TweetTask extends AsyncTask<String, Void, Void> {
        private User user;
        private Twitter twitter;

        @Override
        protected void onPreExecute() {
            twitter = MyTwitterFactory.getInstance(mContext).getTwitter();
            user = null;
        }

        @Override
        protected Void doInBackground(String... args) {
            String status = args[0];
            try {
                user = twitter.verifyCredentials();
                //Log.e(TAG, "Hello my name is " + user.getScreenName());
                //status = GoHome.getStatus(user.getScreenName());
                twitter.updateStatus(status);
            } catch (Exception e) {
                System.out.println("Could not get the twitter instance.");
                e.printStackTrace();
            }
            return null;
        }
    }
}
