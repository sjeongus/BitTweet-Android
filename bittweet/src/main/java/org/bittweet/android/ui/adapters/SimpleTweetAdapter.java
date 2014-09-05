package org.bittweet.android.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.koushikdutta.ion.Ion;

import org.bittweet.android.R;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.ui.ProfileActivity;
import org.bittweet.android.ui.util.LinkTouchMovementMethod;
import org.bittweet.android.ui.util.RoundedTransformation;
import org.bittweet.android.ui.util.TweetFormatter;

import java.text.SimpleDateFormat;
import java.util.Locale;

import twitter4j.Status;

public class SimpleTweetAdapter implements TweetAdapter {
    private final Context context;
    private final SimpleDateFormat dateFormat;

    public SimpleTweetAdapter(Context context) {
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM d yyyy, K:mm a", Locale.getDefault());
    }

    protected Context getContext() {
        return context;
    }

    @Override
    public void recreateView(StatusItem item, TweetViewHolder holder) {
        Status status = item.getStatus();

        if(status.isRetweet()) {
            status = status.getRetweetedStatus();
        }

        final String username = status.getUser().getScreenName();

        // Movement method to color timeline links and mentions gray when pressed
        holder.tweet.setMovementMethod(new LinkTouchMovementMethod(true));
        // Formats tweet to enable clicking of mentions and links, and sets status text
        holder.tweet.setText(TweetFormatter.formatStatusText(context, status));

        holder.displayName.setText(status.getUser().getName());
        holder.userName.setText("@" + username);
        holder.time.setText(dateFormat.format(status.getCreatedAt()));

        // Defaults
        holder.mediaExpansion.setVisibility(View.GONE);
        holder.accent.setVisibility(View.GONE);
        holder.rtBy.setVisibility(View.GONE);

        if(status.isRetweeted()) {
            holder.retweetBtn.setImageResource(R.drawable.ic_navigation_undo_refresh);
        } else {
            holder.retweetBtn.setImageResource(R.drawable.ic_navigation_refresh);
        }

        if(status.isFavorited()) {
            holder.favBtn.setImageResource(R.drawable.ic_rating_not_important);
        } else {
            holder.favBtn.setImageResource(R.drawable.ic_rating_important);
        }

        // Get and set avatar asynchronously
        Ion.with(holder.avatarImage)
                .resize(150, 150)
                .transform(new RoundedTransformation(250, 0))
                .animateGif(true)
                .load(status.getUser().getOriginalProfileImageURLHttps());

        // OnClick method for avatar image that launches user's profile
        holder.avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("USERNAME", username);
                context.startActivity(intent);
            }
        });
    }
}
