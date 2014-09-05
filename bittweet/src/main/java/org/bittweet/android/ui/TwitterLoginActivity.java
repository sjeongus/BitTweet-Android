package org.bittweet.android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.bittweet.android.R;
import org.bittweet.android.ui.util.Blur;
import org.bittweet.android.ui.util.ConnectionDetector;

public class TwitterLoginActivity extends FragmentActivity {

    // Views
    private Button loginButton;

    // Misc
    private ImageView blurredOverlay;
    private ImageView background;
    private Context context;

    private SharedPreferences twitPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = getApplicationContext();

        loginButton = (Button) findViewById(R.id.button_login);
        background = (ImageView) findViewById(R.id.background);
        blurredOverlay = (ImageView) findViewById(R.id.blurback);
        twitPrefs = context.getSharedPreferences("MyTwitter", MODE_PRIVATE);

        Bitmap blurred = Blur.loadBitmapFromView(background);
        Blur.blurBitmap(blurred, background, blurredOverlay, this);

        // When user clicks login button, should open up a web view that allows them to log in
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectionDetector.isOnline(TwitterLoginActivity.this)) {
                    twitPrefs.edit().putBoolean("FIRST_RUN", true).commit();
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("LOGIN", true);
                    startActivity(intent);
                    //finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TwitterLoginActivity.this);
                    builder.setTitle(R.string.connection_title)
                            .setMessage(R.string.connection_message)
                            .setNegativeButton(R.string.connection_cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });
                    // Create the AlertDialog object
                    builder.show();
                }
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("EXIT", false)) {
            finish();
            startActivity(new Intent(TwitterLoginActivity.this, TweetsListActivity.class));
        }
    }
}
