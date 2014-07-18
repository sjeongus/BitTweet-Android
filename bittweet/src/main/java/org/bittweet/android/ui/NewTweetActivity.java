package org.bittweet.android.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.bittweet.android.ApplicationController;
import org.bittweet.android.R;
import org.bittweet.android.internal.MyTwitterFactory;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.services.TweetService;
import org.bittweet.android.ui.adapters.SimpleTweetAdapter;
import org.bittweet.android.ui.adapters.TweetAdapter;
import org.bittweet.android.ui.adapters.TweetViewHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;

public class NewTweetActivity extends Activity {
    public static final String ARG_REPLY_TO_ID = "reply_to";
    public static final String INTENT_REPLY = "org.bittweet.android.actions.REPLY";

    private ApplicationController controller;
    private StatusItem inReplyToStatus;
    private ActionBar actionBar;

    private EditText viewTweetEdit;
    private TextView viewCharCounter;
    private Button attachImage;
    private ImageView uploadImage;
    private Bitmap myBitmap;
    private Uri imageUri;

    private final int SELECT_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet);
        initializeResources();

        myBitmap = null;

        uploadImage = (ImageView) findViewById(R.id.image);
        attachImage = (Button) findViewById(R.id.attachImage);
        attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
    }

    /* Photo Selection result */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            Context activity = getApplicationContext();
            myBitmap = getBitmapFromCameraData(data, activity);
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            // Do something
        }
    }

    private Bitmap getBitmapFromCameraData(Intent data, Context context) {
        imageUri = data.getData();
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            if (bitmap != null) {
                uploadImage.setImageBitmap(bitmap);
                uploadImage.setVisibility(View.VISIBLE);
            }
            return bitmap;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
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
        TweetAdapter adapter = new SimpleTweetAdapter(this);
        ViewStub viewReplyStub = (ViewStub) findViewById(R.id.reply_container);
        View viewReply = viewReplyStub.inflate();
        TweetViewHolder holder = new TweetViewHolder(viewReply);

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
            if (imageUri != null) {
                serviceIntent.putExtra(Intent.EXTRA_STREAM, imageUri.toString());
                imageUri = null;
            }
            else {
                serviceIntent.putExtra(Intent.EXTRA_STREAM, "empty");
            }

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
