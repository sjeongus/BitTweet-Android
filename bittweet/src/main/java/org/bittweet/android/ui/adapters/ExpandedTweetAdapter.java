package org.bittweet.android.ui.adapters;

import android.content.Context;
import android.view.View;
import org.bittweet.android.R;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.ui.util.RoundedTransformation;
import twitter4j.MediaEntity;
import twitter4j.Status;

public class ExpandedTweetAdapter extends SimpleTweetAdapter {
    public ExpandedTweetAdapter(Context context) {
        super(context);
    }

    @Override
    public void recreateView(StatusItem item, TweetViewHolder holder) {
        super.recreateView(item, holder);

        Status status = item.getStatus();
        String retweetedByName = null;
        boolean isRT = false;

        if(status.isRetweet()) {
            isRT = true;
            retweetedByName = status.isRetweeted() ? getContext().getString(R.string.its_you) : "@" + status.getUser().getScreenName();
            status = status.getRetweetedStatus();
        } else if(status.isRetweeted()) {
            isRT = true;
            retweetedByName = getContext().getString(R.string.its_you);
        }

        holder.accent.setVisibility(View.VISIBLE);

        if(isRT) {
            holder.rtBy.setVisibility(View.VISIBLE);
            holder.rtBy.setText(String.format(getContext().getString(R.string.retweeted_by), retweetedByName));

            if(item.isMention()) {
                holder.frontView.setBackgroundColor(getContext().getResources().getColor(R.color.reply_background));
            } else {
                holder.frontView.setBackgroundColor(getContext().getResources().getColor(R.color.white));
            }

            holder.accent.setBackgroundColor(getContext().getResources().getColor(R.color.retweet_accent));
        } else {
            holder.rtBy.setVisibility(View.GONE);

            if(status.isFavorited()) {
                holder.accent.setBackgroundColor(getContext().getResources().getColor(R.color.favourite_accent));
                holder.frontView.setBackgroundColor(getContext().getResources().getColor(R.color.white));
            } else if(item.isMention()) {
                holder.accent.setBackgroundColor(getContext().getResources().getColor(R.color.reply_accent));
                holder.frontView.setBackgroundColor(getContext().getResources().getColor(R.color.reply_background));
            } else {
                holder.accent.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                holder.frontView.setBackgroundColor(getContext().getResources().getColor(R.color.white));
            }
        }

        // Load media
        MediaEntity[] mediaEntities = status.getMediaEntities();

        if(mediaEntities.length > 0) {
            MediaEntity displayedMedia = mediaEntities[0];

            getPicasso().load(displayedMedia.getMediaURLHttps())
                    .resizeDimen(R.dimen.media_expansion_size, R.dimen.media_expansion_size)
                    .centerCrop()
                    .transform(new RoundedTransformation(20, 0))
                    .into(holder.mediaExpansion);

            holder.mediaExpansion.setVisibility(View.VISIBLE);
        } else {
            holder.mediaExpansion.setVisibility(View.GONE);
        }
    }
}
