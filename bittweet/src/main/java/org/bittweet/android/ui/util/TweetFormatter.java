package org.bittweet.android.ui.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.bittweet.android.R;
import org.bittweet.android.ui.ProfileActivity;
import org.bittweet.android.ui.WebViewActivity;

import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
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

    public static SpannableString formatReplyText(Context context, Status status) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(status.getText());

        int movedBy = 0;

        // Make normal URLs and media URLs clickable
        movedBy = linkify(context, builder, status.getURLEntities(), movedBy);
        linkify(context, builder, status.getMediaEntities(), movedBy);

        return SpannableString.valueOf(builder);
    }

    public static SpannableString formatDescriptionText(Context context, User user) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(user.getDescription());

        int movedBy = 0;

        // Make normal URLs and media URLs clickable
        movedBy = linkify(context, builder, user.getDescriptionURLEntities(), movedBy);

        return SpannableString.valueOf(builder);
    }

    public static SpannableString formatUrlText(Context context, URLEntity urlEntity) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(urlEntity.getURL());
        URLEntity[] entities = { urlEntity };
        int movedBy = 0;
        // Make normal URLs and media URLs clickable
        movedBy = linkify(context, builder, entities, movedBy);
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
            linkTemp = new LinkSpan(context, url.getExpandedURL());
            builder.replace(url.getStart() + movedBy, url.getEnd() + movedBy, url.getDisplayURL());
            builder.setSpan(linkTemp, url.getStart() + movedBy, url.getStart() + url.getDisplayURL().length() + movedBy, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // By replacing the original URLs with expanded/display URLs, we change the length of the original span,
            // that way invalidating the getStart/getEnd values of the URLEntities. We have to account for that.
            movedBy += url.getDisplayURL().length() - url.getURL().length();
        }

        return movedBy;
    }

    // Abstract class that acts as template for UrlSpan and UserSpan
    public static abstract class TouchableSpan extends ClickableSpan {
        private boolean mIsPressed;

        public TouchableSpan() {
        }

        public void setPressed(boolean isSelected) {
            mIsPressed = isSelected;
        }
    }

    private static class LinkSpan extends TouchableSpan {
        private String url;
        private Context context;
        private boolean mIsPressed;

        public LinkSpan(Context context, String url) {
            this.url = url;
            this.context = context;
        }

        public void setPressed(boolean isSelected) {
            this.mIsPressed = isSelected;
        }

        @Override
        public void onClick(View view) {
            // Handle URL
            new UrlHandler(context, url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            // Set text color
            ds.setColor(mIsPressed ? Color.LTGRAY : context.getResources().getColor(R.color.unpressed_link_color));
            // Set to false to remove underline
            ds.setUnderlineText(false);
        }
    }

    private static class UserSpan extends TouchableSpan {
        private String username;
        private Context context;
        private boolean mIsPressed;

        public UserSpan(Context context, String username) {
            this.username = username;
            this.context = context;
        }

        public void setPressed(boolean isSelected) {
            this.mIsPressed = isSelected;
        }

        @Override
        public void onClick(View view) {
            // Start up profile activity when clicking on a link
            System.out.println("Clicked on @" + username);
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("USERNAME", username);
            context.startActivity(intent);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            // Set text color
            ds.setColor(mIsPressed ? Color.LTGRAY : context.getResources().getColor(R.color.unpressed_link_color));
            // Set to false to remove underline
            ds.setUnderlineText(false);
        }
    }
}
