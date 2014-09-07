package org.bittweet.android.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import org.bittweet.android.ApplicationController;
import org.bittweet.android.R;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.services.TweetService;
import org.bittweet.android.ui.adapters.SimpleTweetAdapter;
import org.bittweet.android.ui.adapters.TweetAdapter;
import org.bittweet.android.ui.adapters.TweetViewHolder;
import org.bittweet.android.ui.util.LinkTouchMovementMethod;
import org.bittweet.android.ui.util.RoundedTransformation;
import org.bittweet.android.ui.util.TweetFormatter;

import java.lang.ref.WeakReference;
import java.util.Dictionary;
import java.util.Map;

import twitter4j.UserMentionEntity;

import static org.bittweet.android.ui.util.ImageUtils.convertPixelsToDp;
import static org.bittweet.android.ui.util.ImageUtils.decodeSampledBitmapFromResource;
import static org.bittweet.android.ui.util.ImageUtils.rotateBitmap;
import static org.bittweet.android.ui.util.ImageUtils.scaleCenterCrop;

public class NewTweetActivity extends FragmentActivity {
    public static final String ARG_REPLY_TO_ID = "reply_to";
    public static final String INTENT_REPLY = "org.bittweet.android.actions.REPLY";
    public static final String INTENT_FEEDBACK = "org.bittweet.android.actions.FEEDBACK";

    private StatusItem inReplyToStatus;

    private EditText viewTweetEdit;
    private TextView viewCharCounter;
    private ImageButton attachImage;
    private String myUser;

    private Uri[] imageUri;

    private ImageView uploadImage1;
    private ImageView uploadImage2;
    private ImageView uploadImage3;
    private ImageView uploadImage4;

    private final int IMAGE_PICKER_SELECT = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet);
        SharedPreferences twitPref = getSharedPreferences("MyTwitter", MODE_PRIVATE);
        myUser = twitPref.getString("USERNAME", "null");
        String myAvatar = twitPref.getString("AVATAR", "null");
        initializeResources();

        ImageView avatar = (ImageView) findViewById(R.id.profilephoto);
        Ion.with(avatar).resize(250, 250).transform(new RoundedTransformation(250, 0)).animateGif(true).load(myAvatar);

        uploadImage1 = (ImageView) findViewById(R.id.image1);
        uploadImage2 = (ImageView) findViewById(R.id.image2);
        uploadImage3 = (ImageView) findViewById(R.id.image3);
        uploadImage4 = (ImageView) findViewById(R.id.image4);
        imageUri = new Uri[4];

        attachImage = (ImageButton) findViewById(R.id.attachImage);
        attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, IMAGE_PICKER_SELECT);
            }
        });

        uploadImage1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopup(uploadImage1, 0);
                return true;
            }
        });
        uploadImage2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopup(uploadImage2, 1);
                return true;
            }
        });
        uploadImage3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopup(uploadImage3, 2);
                return true;
            }
        });
        uploadImage4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopup(uploadImage4, 3);
                return true;
            }
        });
    }

    public void showPopup(ImageView view, int pos) {
        final ImageView v = view;
        final int i = pos;
        PopupMenu popupMenu = new PopupMenu(NewTweetActivity.this, v);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.remove_item:
                        removeImage(v, i);
                        return true;
                    case R.id.cancel_item:
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.inflate(R.menu.compose_remove_image);
        popupMenu.show();
    }

    public void removeImage(ImageView view, int pos) {
        if (imageUri[pos] != null) {
            imageUri[pos] = null;
            view.setImageBitmap(null);
            view.setVisibility(View.GONE);
            attachImage.setEnabled(true);
            attachImage.setBackgroundResource(R.drawable.bittweet_activated_background_holo_light);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // Only will trigger it if no physical keyboard is open
        inputMethodManager.showSoftInput(viewTweetEdit, InputMethodManager.SHOW_IMPLICIT);
        return true;
    }

    // Photo selection result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_PICKER_SELECT && resultCode == Activity.RESULT_OK) {
            BitmapWorkerTask task = new BitmapWorkerTask(data);
            task.execute();
        }
    }

    // Asynchronously load bitmap into imageview
    class BitmapWorkerTask extends AsyncTask<Void, Void, String> {
        private final WeakReference<ImageView> imageViewReference;
        private int pos;
        private ImageView imageView;
        private Intent mData;

        public BitmapWorkerTask(Intent data) {
            for (int i = 0; i < imageUri.length; i++) {
                if (imageUri[i] == null) {
                    pos = i;
                    switch(pos) {
                        case 0:
                            imageView = uploadImage1;
                            break;
                        case 1:
                            imageView = uploadImage2;
                            break;
                        case 2:
                            imageView = uploadImage3;
                            break;
                        case 3:
                            imageView = uploadImage4;
                            break;
                    }
                    break;
                }
            }
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            mData = data;
        }

        // Decode image in background.
        @Override
        protected String doInBackground(Void... params) {
            Context activity = getApplicationContext();
            return getBitmapFromCameraData(pos, mData, activity);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(String path) {
            if (path != null) {
                final ImageView myView = imageViewReference.get();
                if (myView != null) {
                    int size = (int) convertPixelsToDp(150, getApplicationContext());
                    Bitmap downmap = decodeSampledBitmapFromResource(path, size, size);
                    downmap = rotateBitmap(path, downmap);
                    downmap = scaleCenterCrop(downmap, size, size);
                    myView.setImageBitmap(downmap);
                    myView.setVisibility(View.VISIBLE);
                    if (imageUri[0] != null && imageUri[1] != null && imageUri[2] != null && imageUri[3] != null) {
                        attachImage.setEnabled(false);
                        attachImage.setBackgroundColor(Color.LTGRAY);
                    }
                }
            }
        }
    }

    // Function to get bitmap from gallery
    public String getBitmapFromCameraData(int pos, Intent data, Context context) {
        imageUri[pos] = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(data.getData(),filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    private void initializeResources() {
        ApplicationController controller = (ApplicationController) getApplication();
        ActionBar actionBar = getActionBar();

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
            String feedback = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            viewTweetEdit.setText(feedback);
            viewTweetEdit.setSelection(feedback.length());
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void initializeReplyToStatus() {
        LinearLayout replyContainer = (LinearLayout) findViewById(R.id.reply_container);
        TextView replyText = (TextView) findViewById(R.id.reply_text);
        replyContainer.setVisibility(View.VISIBLE);
        replyText.setMovementMethod(new LinkTouchMovementMethod(true));
        replyText.setText(TweetFormatter.formatReplyText(NewTweetActivity.this, inReplyToStatus.getStatus()));

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
            if (imageUri[0] != null || imageUri[1] != null || imageUri[2] != null || imageUri[3] != null) {
                String[] uriString = new String[4];
                for (int i = 0; i < imageUri.length; i++) {
                    System.err.println(imageUri.toString());
                    if (imageUri[i] != null) {
                        uriString[i] = imageUri[i].toString();
                    }
                    imageUri[i] = null;
                }
                serviceIntent.putExtra("IMAGE_ARRAY", uriString);
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
