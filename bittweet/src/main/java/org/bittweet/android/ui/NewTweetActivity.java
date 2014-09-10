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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
import org.bittweet.android.ui.util.LinkTouchMovementMethod;
import org.bittweet.android.ui.util.RoundedTransformation;
import org.bittweet.android.ui.util.TweetFormatter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.validation.Validator;

import twitter4j.UserMentionEntity;

import static org.bittweet.android.ui.util.ImageUtils.convertPixelsToDp;
import static org.bittweet.android.ui.util.ImageUtils.decodeSampledBitmapFromResource;
import static org.bittweet.android.ui.util.ImageUtils.rotateBitmap;
import static org.bittweet.android.ui.util.ImageUtils.scaleCenterCrop;
import static org.bittweet.android.ui.util.ImageUtils.setPic;

public class NewTweetActivity extends FragmentActivity {
    public static final String ARG_REPLY_TO_ID = "reply_to";
    public static final String INTENT_REPLY = "org.bittweet.android.actions.REPLY";
    public static final String INTENT_FEEDBACK = "org.bittweet.android.actions.FEEDBACK";

    private StatusItem inReplyToStatus;

    private EditText viewTweetEdit;
    private TextView viewCharCounter;
    private int textCount;
    private boolean imageAttached;
    private ImageButton attachImage;
    private String myUser;

    private String[] imageUri;
    private String mCurrentPhotoPath;

    private ImageView uploadImage1;
    private ImageView uploadImage2;
    private ImageView uploadImage3;
    private ImageView uploadImage4;

    private static final int IMAGE_PICKER_SELECT = 999;
    private static final int REQUEST_IMAGE_CAPTURE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet);
        SharedPreferences twitPref = getSharedPreferences("MyTwitter", MODE_PRIVATE);
        myUser = twitPref.getString("USERNAME", "null");
        String myAvatar = twitPref.getString("AVATAR", "null");
        initializeResources();

        ImageView avatar = (ImageView) findViewById(R.id.profilephoto);
        Ion.with(avatar).resize(250, 250).transform(new RoundedTransformation(250, 0,
                true, true, true, true)).animateGif(true).load(myAvatar);

        uploadImage1 = (ImageView) findViewById(R.id.image1);
        uploadImage2 = (ImageView) findViewById(R.id.image2);
        uploadImage3 = (ImageView) findViewById(R.id.image3);
        uploadImage4 = (ImageView) findViewById(R.id.image4);
        imageUri = new String[4];
        textCount = 140;

        attachImage = (ImageButton) findViewById(R.id.attachImage);
        attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryPopup(view);
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

    // Popup menu that allows user to remove image
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
                    default:
                        return false;
                }
            }
        });
        popupMenu.inflate(R.menu.compose_remove_image);
        popupMenu.show();
    }

    // Popup menu that allows user to choose to attach from camera or gallery
    public void galleryPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(NewTweetActivity.this, view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pick_camera:
                        dispatchTakePictureIntent();
                        return true;
                    case R.id.pick_gallery:
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(photoPickerIntent, IMAGE_PICKER_SELECT);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.inflate(R.menu.compose_add_image);
        popupMenu.show();
    }

    public void removeImage(ImageView view, int pos) {
        if (imageUri[pos] != null) {
            imageUri[pos] = null;
            view.setImageBitmap(null);
            view.setVisibility(View.GONE);
            attachImage.setEnabled(true);
            attachImage.setBackgroundResource(R.drawable.bittweet_activated_background_holo_light);
            if (imageUri[0] == null && imageUri[1] == null && imageUri[2] == null && imageUri[3] == null) {
                textCount += 25;
                imageAttached = false;
                viewCharCounter.setText(String.valueOf(checkCharactersLeft(viewTweetEdit.getText())));
            }
        }
    }

    // Starts the intent to take picture from camera
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.err.println("Could not create file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // Adds image taken from camera to the gallery
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // Only will trigger it if no physical keyboard is open
        inputMethodManager.showSoftInput(viewTweetEdit, InputMethodManager.SHOW_IMPLICIT);
        return true;
    }

    // Creates the image file to save to gallery
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        File direc = new File(storageDir.getAbsolutePath() + "/Camera/");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                direc    /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        System.err.println("Camera image is " + mCurrentPhotoPath);
        return image;
    }

    // Photo selection result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_PICKER_SELECT && resultCode == Activity.RESULT_OK) {
            BitmapWorkerTask task = new BitmapWorkerTask(data, false);
            task.execute();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            galleryAddPic();
            BitmapWorkerTask task = new BitmapWorkerTask(data, true);
            task.execute();
        }
    }

    // Asynchronously load bitmap into imageview
    class BitmapWorkerTask extends AsyncTask<Void, Void, String> {
        private final WeakReference<ImageView> imageViewReference;
        private int pos;
        private ImageView imageView;
        private Intent mData;
        private boolean fromCamera;

        public BitmapWorkerTask(Intent data, boolean camera) {
            fromCamera = camera;
            for (int i = 0; i < imageUri.length; i++) {
                if (imageUri[i] == null) {
                    pos = i;
                    /*if (mCurrentPhotoPath != null && fromCamera) {
                        imageUri[i] = mCurrentPhotoPath;
                    }*/
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
            this.fromCamera = camera;
            mData = data;
        }

        // Decode image in background.
        @Override
        protected String doInBackground(Void... params) {
            Context activity = getApplicationContext();
            if (fromCamera) {
                return mCurrentPhotoPath;
            } else {
                return getBitmapFromCameraData(mData, activity);
            }
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(String path) {
            if (path != null) {
                imageUri[pos] = path;
                final ImageView myView = imageViewReference.get();
                if (myView != null) {
                    int size = (int) convertPixelsToDp(150, getApplicationContext());
                    Bitmap downmap;
                    if (fromCamera) {
                        downmap = setPic(mCurrentPhotoPath, size, size);
                    } else {

                        downmap = decodeSampledBitmapFromResource(path, size, size);
                    }
                    downmap = rotateBitmap(path, downmap);
                    downmap = scaleCenterCrop(downmap, size, size);
                    myView.setImageBitmap(downmap);
                    myView.setVisibility(View.VISIBLE);
                    if (imageUri[0] != null && imageUri[1] != null && imageUri[2] != null && imageUri[3] != null) {
                        attachImage.setEnabled(false);
                        attachImage.setBackgroundColor(Color.LTGRAY);
                    }
                    if (!imageAttached) {
                        textCount -= 25;
                        imageAttached = true;
                        viewCharCounter.setText(String.valueOf(checkCharactersLeft(viewTweetEdit.getText())));
                    }
                }
            }
        }
    }

    // Function to get bitmap from gallery
    public String getBitmapFromCameraData(Intent data, Context context) {
        //imageUri[pos] = getRealPathFromURI(getApplicationContext(), data.getData());
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

        assert actionBar != null;
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
                serviceIntent.putExtra("IMAGE_ARRAY", imageUri);
            }

            if(inReplyToStatus != null) {
                serviceIntent.putExtra(TweetService.ARG_TWEET_ID, inReplyToStatus.getId());
            }

            startService(serviceIntent);
            NavUtils.navigateUpFromSameTask(NewTweetActivity.this);
        }
    }

    private int checkCharactersLeft(CharSequence str) {
        //return 140 - str.length();
        //return textCount - str.length();
        com.twitter.Validator valid = new com.twitter.Validator();
        return textCount - valid.getTweetLength(str.toString());
    }

    private class TweetTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (checkCharactersLeft(charSequence) < 0) {
                viewCharCounter.setTextColor(Color.RED);
            } else {
                viewCharCounter.setTextColor(Color.BLACK);
            }
            viewCharCounter.setText(String.valueOf(checkCharactersLeft(charSequence)));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

}
