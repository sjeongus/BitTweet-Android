package org.bittweet.android.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

import org.bittweet.android.R;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.ui.fragments.TweetsListFragment;
import org.bittweet.android.ui.util.RoundedTransformation;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

public class ExpandedTweetAdapter extends SimpleTweetAdapter {
    private int width;
    private TweetsListFragment mFragment;

    public ExpandedTweetAdapter(Context context, TweetsListFragment fragment) {
        super(context);
        this.mFragment = fragment;
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

            if(item.isMention() && !mFragment.isMentionsTimeline()) {
                holder.frontView.setBackgroundColor(getContext().getResources().getColor(R.color.reply_background));
            } else {
                holder.frontView.setBackgroundResource(R.drawable.bittweet_tweet_background);
            }

            holder.accent.setBackgroundColor(getContext().getResources().getColor(R.color.retweet_accent));
        } else {
            holder.rtBy.setVisibility(View.GONE);

            if(status.isFavorited()) {
                holder.accent.setBackgroundColor(getContext().getResources().getColor(R.color.favourite_accent));
                holder.frontView.setBackgroundResource(R.drawable.bittweet_tweet_background);
            } else if(item.isMention() && !mFragment.isMentionsTimeline()) {
                holder.accent.setBackgroundColor(getContext().getResources().getColor(R.color.reply_accent));
                holder.frontView.setBackgroundColor(getContext().getResources().getColor(R.color.reply_background));
            } else {
                holder.accent.setBackgroundColor(Color.TRANSPARENT);
                holder.frontView.setBackgroundResource(R.drawable.bittweet_tweet_background);
            }
        }

        // Load media
        MediaEntity[] mediaEntities = status.getMediaEntities();
        URLEntity[] urlEntities = status.getURLEntities();
        final ImageView media = holder.mediaExpansion;

        if(mediaEntities.length > 0) {
            final MediaEntity displayedMedia = mediaEntities[0];

            // To get the dimensions of the image preview before it is drawn
            media.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    media.getViewTreeObserver().removeOnPreDrawListener(this);

                    // Get the width of the view and set height to 16:10 aspect ratio
                    width = media.getWidth();
                    setImage(media, width, width * 5/8, displayedMedia.getMediaURLHttps());
                    return true;
                }
            });

            holder.mediaExpansion.setVisibility(View.VISIBLE);
        } else if(urlEntities.length > 0) {
            for (final URLEntity urlEntity : urlEntities) {
                if (urlEntity.getExpandedURL().matches("^https?://(?:[a-z\\-]+\\.)+[a-z]{2,6}(?:/[^/#?]+)+\\.(?:jpe?g|gif|png)$")) {
                    // To get the dimensions of the image preview before it is drawn
                    media.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            media.getViewTreeObserver().removeOnPreDrawListener(this);

                            // Get the width of the view and set height to 16:10 aspect ratio
                            width = media.getWidth();
                            setImage(media, width, width * 5/8, urlEntity.getExpandedURL());
                            return true;
                        }
                    });
                    holder.mediaExpansion.setVisibility(View.VISIBLE);
                    break;
                }
            }
        } else {
            holder.mediaExpansion.setVisibility(View.GONE);
        }
    }

    // Set the image preview on timeline with Ion
    public void setImage(ImageView view, int width, int height, String url) {
        Ion.with(view)
                .animateGif(false)
                .resize(width, height)
                .centerCrop()
                .transform(new RoundedTransformation(20, 0))
                .load(url);
    }
}
