package com.golden.owaranai.ui.util;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class TweetFormatter {
    public static SpannableString formatStatusText(Status status) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(status.getText());

        // Make normal URLs and media URLs clickable
        linkify(builder, status.getURLEntities());
        linkify(builder, status.getMediaEntities());

        // Make user names clickable
        linkifyUsers(builder, status.getUserMentionEntities());

        return SpannableString.valueOf(builder);
    }

    private static void linkifyUsers(SpannableStringBuilder builder, UserMentionEntity[] userMentionEntities) {
        ClickableSpan linkTemp;

        for(UserMentionEntity mention : userMentionEntities) {
            linkTemp = new UserSpan(mention.getScreenName());
            builder.setSpan(linkTemp, mention.getStart(), mention.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
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

    private static class UserSpan extends ClickableSpan {
        private String username;

        public UserSpan(String username) {
            this.username = username;
        }

        @Override
        public void onClick(View view) {
            System.out.println("Clicked on @" + username);
        }
    }
}
