package org.bittweet.android.ui.adapters;

import android.content.Context;
import android.view.View;

import com.koushikdutta.ion.Ion;

import org.bittweet.android.R;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.ui.util.RoundedTransformation;
import org.bittweet.android.ui.util.TransparentLinkMovementMethod;
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

        holder.tweet.setMovementMethod(new TransparentLinkMovementMethod(holder.tweetContainer));
        holder.tweet.setText(TweetFormatter.formatStatusText(context, status));
        holder.displayName.setText(status.getUser().getName());
        holder.userName.setText("@" + status.getUser().getScreenName());
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

        Ion.with(holder.avatarImage).transform(new RoundedTransformation(250, 0))
                .animateGif(true)
                .load(status.getUser().getBiggerProfileImageURLHttps());
    }
}
