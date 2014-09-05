package org.bittweet.android.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.NavUtils;

import org.bittweet.android.R;
import org.bittweet.android.internal.MyTwitterFactory;
import org.bittweet.android.services.TweetService;
import org.bittweet.android.ui.NewTweetActivity;
import org.bittweet.android.ui.TweetsListActivity;
import org.bittweet.android.ui.TwitterLoginActivity;

import twitter4j.Twitter;

/**
 * Created by soomin on 8/22/2014.
 */
public class SettingsFragment extends PreferenceFragment {
    private Preference logout;
    private Preference tweetfeedback;
    private SwitchPreference streaming;
    private SharedPreferences twitter;
    private Twitter mTwitter;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        twitter = this.getActivity().getSharedPreferences("MyTwitter", Context.MODE_PRIVATE);

        context = getActivity();
        System.err.println(context);
        // The logout preference. Pops up confirm dialog when pressed
        logout = findPreference("pref_key_logout");
        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                logoutDialog();
                return false;
            }
        });

        // Get device and BitTweet build version names
        String device = getDeviceName();
        String versionName = "n/a";

        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final String info = "@fuyutsukikaru #bittweet (" + device + ": " + versionName +")";

        // Feedback preference that opens tweet compose to send feedback
        tweetfeedback = findPreference("pref_key_feedback_twitter");
        tweetfeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent serviceIntent = new Intent(context, NewTweetActivity.class);
                serviceIntent.setAction(NewTweetActivity.INTENT_FEEDBACK);
                serviceIntent.putExtra(Intent.EXTRA_TEXT, info);
                startActivity(serviceIntent);
                return true;
            }
        });

        // Switch preference to enable or disable streaming
        streaming = (SwitchPreference) findPreference("pref_key_streaming");
        streaming.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.preference_streaming)
                        .setMessage(R.string.streaming_name)
                        .setNegativeButton(R.string.streaming_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Clicking this will launch the TweetsListActivity and restart it
                                Intent intent = new Intent(context, TweetsListActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("RESTART", true);
                                startActivity(intent);
                                return;
                            }
                        });
                // Create the AlertDialog object
                builder.show();
                return true;
            }
        });
    }

    // Function that creates the logout confirm dialog.
    public void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.logout_title)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.logout_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Clear all preferences
                        // TODO: Clear out user timeline and other info. Persists after logging out
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = settings.edit();
                        editor.clear().commit();
                        twitter.edit().clear().commit();
                        mTwitter = MyTwitterFactory.getInstance(context).getTwitter();
                        mTwitter.setOAuthAccessToken(null);

                        // Launches TweetsListActivity class with LOGOUT intent
                        Intent intent = new Intent(context, TweetsListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("LOGOUT", true);
                        System.err.println("LOGOUT passed. Logging out!");
                        startActivity(intent);
                        return;
                    }
                })
                .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object
        builder.show();
    }

    // Function to retrieve the device name
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    // Capitalize string
    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
