package org.bittweet.android.ui.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import org.bittweet.android.ui.ProfileActivity;
import org.bittweet.android.ui.WebViewActivity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UrlHandler {
    private Context context;
    private String url;
    private static String youtubeUrl = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|watch\\?v%3D|%2Fvideos%2F|embed%‌​2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
    private static String twitterProfile = "|https?://(www\\.)?twitter\\.com/(#!/)?@?([^/]*)|";

    public UrlHandler(Context context, String url) {
        this.context = context;
        this.url = url;

        System.err.println("The url is " + url);
        Pattern youtubePattern = Pattern.compile(youtubeUrl, Pattern.CASE_INSENSITIVE);
        Matcher youtubeMatcher = youtubePattern.matcher(url);
        Pattern profilePattern = Pattern.compile(twitterProfile, Pattern.CASE_INSENSITIVE);
        Matcher profileMatcher = profilePattern.matcher(url);
        if (youtubeMatcher.find()) {
            System.err.println("Matched YouTube!");
            System.err.println("The id was " + youtubeMatcher.group());
            handleYoutubeUrl(youtubeMatcher.group());
        /*} else if (profileMatcher.find()) {
            System.err.println("Matched Twitter profile!");
            System.err.println("Username is " + profileMatcher.group(1));
            handleTwitterProfile(profileMatcher.group(1));*/
        } else {
            openWebview();
        }
    }

    public void handleYoutubeUrl(String id) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + id));
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            // Default youtube app not present or doesn't conform to the standard we know
            openWebview();
        } else {
            context.startActivity(i);
        }
    }

    public void handleTwitterProfile(String username) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("USERNAME", username);
        context.startActivity(intent);
    }

    public void openWebview() {
        Intent intent = new Intent(context, WebViewActivity.class);
        System.err.println("WebView intent " + intent.toString());
        System.err.println("Sending to WebView: " + url);
        intent.setData(Uri.parse(url));
        intent.putExtra("URL", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
