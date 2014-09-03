package org.bittweet.android.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import org.bittweet.android.ApplicationController;
import org.bittweet.android.R;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.services.TweetService;
import org.bittweet.android.ui.adapters.SimpleTweetAdapter;
import org.bittweet.android.ui.adapters.TweetAdapter;
import org.bittweet.android.ui.adapters.TweetViewHolder;
import org.bittweet.android.ui.util.RoundedTransformation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import twitter4j.UserMentionEntity;

public class NewTweetActivity extends FragmentActivity {
    public static final String ARG_REPLY_TO_ID = "reply_to";
    public static final String INTENT_REPLY = "org.bittweet.android.actions.REPLY";
    public static final String INTENT_FEEDBACK = "org.bittweet.android.actions.FEEDBACK";

    private ApplicationController controller;
    private StatusItem inReplyToStatus;
    private ActionBar actionBar;

    private EditText viewTweetEdit;
    private TextView viewCharCounter;
    private ImageButton attachImage;
    private ImageView uploadImage;
    private ImageView avatar;
    private Bitmap myBitmap;
    private Uri imageUri;
    private String myAvatar;
    private String myUser;
    private SharedPreferences twitPref;

    private final int IMAGE_PICKER_SELECT = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet);
        twitPref = getSharedPreferences("MyTwitter", MODE_PRIVATE);
        myUser = twitPref.getString("USERNAME", "null");
        myAvatar = twitPref.getString("AVATAR", "null");
        initializeResources();

        myBitmap = null;

        avatar = (ImageView) findViewById(R.id.profilephoto);
        Ion.with(avatar).resize(250, 250).transform(new RoundedTransformation(250, 0)).animateGif(true).load(myAvatar);
        uploadImage = (ImageView) findViewById(R.id.image);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBitmap != null) {
                    myBitmap = null;
                    imageUri = null;
                    uploadImage.setImageBitmap(null);
                    attachImage.setEnabled(true);
                    uploadImage.setVisibility(View.GONE);
                }
            }
        });
        attachImage = (ImageButton) findViewById(R.id.attachImage);
        attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, IMAGE_PICKER_SELECT);
            }
        });
    }

    /* Photo Selection result */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_PICKER_SELECT && resultCode == Activity.RESULT_OK) {
            Context activity = getApplicationContext();
            //myBitmap = getBitmapFromCameraData(data, activity);
            //uploadImage.setImageBitmap(myBitmap);
            //uploadImage.setVisibility(View.VISIBLE);
            //attachImage.setEnabled(false);
            BitmapWorkerTask task = new BitmapWorkerTask(uploadImage, data);
            task.execute();
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            // Do something
        }
    }

    class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private Intent mData;
        private int data = 0;

        public BitmapWorkerTask(ImageView imageView, Intent data) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            mData = data;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params) {
            Context activity = getApplicationContext();
            Bitmap bitmap = getBitmapFromCameraData(mData, activity);
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    myBitmap = bitmap;
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                    attachImage.setEnabled(false);
                }
            }
        }
    }

    public Bitmap getBitmapFromCameraData(Intent data, Context context) {
        imageUri = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(imageUri,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return BitmapFactory.decodeFile(picturePath);
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

        if (getIntent() != null && INTENT_FEEDBACK.equals(getIntent().getAction())) {
            viewTweetEdit.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
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
            if(originalMention.getScreenName().equals(inReplyToStatus.getStatus().getUser().getScreenName())
                    || originalMention.getScreenName().equals(myUser)) {
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
