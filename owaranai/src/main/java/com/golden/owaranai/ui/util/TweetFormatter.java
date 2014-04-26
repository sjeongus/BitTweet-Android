package com.golden.owaranai.ui.util;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

public class TweetFormatter {
    public static SpannableString formatStatusText(Status status) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String tweetText = status.getText();
        URLEntity[] urlEntities = status.getURLEntities();
        MediaEntity[] mediaEntities = status.getMediaEntities();

        builder.append(tweetText);

        // Make normal URLs and media URLs clickable
        linkify(builder, urlEntities);
        linkify(builder, mediaEntities);

        // Make user names clickable
        // TODO

        return SpannableString.valueOf(builder);
    }

    private static void linkify(SpannableStringBuilder builder, URLEntity[] urlEntities) {
        ClickableSpan linkTemp;

        for(URLEntity url : urlEntities) {
            linkTemp = new LinkSpan(url.getExpandedURL());
            builder.replace(url.getStart(), url.getEnd(), url.getDisplayURL());
            builder.setSpan(linkTemp, url.getStart(), url.getStart() + url.getDisplayURL().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static class LinkSpan extends ClickableSpan {
        private String url;

        public LinkSpan(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View view) {
            System.out.println("Clicked " + url);
        }
    }
}
