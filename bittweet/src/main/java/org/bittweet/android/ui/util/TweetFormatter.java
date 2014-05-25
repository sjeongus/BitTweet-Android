package org.bittweet.android.ui.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class TweetFormatter {
    public static SpannableString formatStatusText(Context context, Status status) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(status.getText());

        // Make user names clickable
        linkifyUsers(context, builder, status.getUserMentionEntities());

        int movedBy = 0;

        // Make normal URLs and media URLs clickable
        movedBy = linkify(context, builder, status.getURLEntities(), movedBy);
        linkify(context, builder, status.getMediaEntities(), movedBy);

        return SpannableString.valueOf(builder);
    }

    private static void linkifyUsers(Context context, SpannableStringBuilder builder, UserMentionEntity[] userMentionEntities) {
        ClickableSpan linkTemp;

        for(UserMentionEntity mention : userMentionEntities) {
            linkTemp = new UserSpan(context, mention.getScreenName());
            builder.setSpan(linkTemp, mention.getStart(), mention.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static int linkify(Context context, SpannableStringBuilder builder, URLEntity[] urlEntities, int movedBy) {
        ClickableSpan linkTemp;

        for(URLEntity url : urlEntities) {
            linkTemp = new LinkSpan(context, url.getURL());
            builder.replace(url.getStart() + movedBy, url.getEnd() + movedBy, url.getDisplayURL());
            builder.setSpan(linkTemp, url.getStart() + movedBy, url.getStart() + url.getDisplayURL().length() + movedBy, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // By replacing the original URLs with expanded/display URLs, we change the length of the original span,
            // that way invalidating the getStart/getEnd values of the URLEntities. We have to account for that.
            movedBy += url.getDisplayURL().length() - url.getURL().length();
        }

        return movedBy;
    }

    private static class LinkSpan extends ClickableSpan {
        private String url;
        private Context context;

        public LinkSpan(Context context, String url) {
            this.url = url;
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        }
    }

    private static class UserSpan extends ClickableSpan {
        private String username;
        private Context context;

        public UserSpan(Context context, String username) {
            this.username = username;
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            System.out.println("Clicked on @" + username);
        }
    }
}
