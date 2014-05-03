package com.golden.owaranai.ui.adapters;

import android.content.Context;
import android.view.View;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.StatusItem;
import com.golden.owaranai.ui.util.RoundedTransformation;
import twitter4j.MediaEntity;
import twitter4j.Status;

public class ExpandedTweetAdapter extends SimpleTweetAdapter {
    public ExpandedTweetAdapter(Context context) {
        super(context);
    }

    @Override
    public void recreateView(StatusItem item, TimelineAdapter.ViewHolder holder) {
        super.recreateView(item, holder);

        Status status = item.getStatus();
        Status originalStatus = status;
        boolean isRT = false;

        if(status.isRetweet()) {
            isRT = true;
            status = status.getRetweetedStatus();
        }

        holder.accent.setVisibility(View.VISIBLE);

        if(isRT) {
            holder.rtBy.setVisibility(View.VISIBLE);
            holder.rtBy.setText(String.format(getContext().getString(R.string.retweeted_by), originalStatus.getUser().getScreenName()));

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
