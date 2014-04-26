package com.golden.owaranai.internal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.golden.owaranai.ApplicationController;
import com.golden.owaranai.GoHome;

import twitter4j.Twitter;
import twitter4j.User;

/**
 * Created by soomin on 4/25/2014.
 */
// Class for posting a new tweet
public class PostStatus {

    private Context mContext;
    private String tweet_text = "";

    public PostStatus(Context context) {
        mContext = context;
        // Build a dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Compose Tweet");

        final EditText tweet = new EditText(context);
        tweet.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(tweet);

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

        builder.show();
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
