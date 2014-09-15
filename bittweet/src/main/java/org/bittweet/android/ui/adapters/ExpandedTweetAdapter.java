package org.bittweet.android.ui.adapters;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.bittweet.android.R;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.ui.ImageViewerActivity;
import org.bittweet.android.ui.fragments.TweetsListFragment;
import org.bittweet.android.ui.util.RoundedTransformation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

import static org.bittweet.android.ui.util.ImageUtils.convertDpToPixel;

public class ExpandedTweetAdapter extends SimpleTweetAdapter {
    private int width;
    private TweetsListFragment mFragment;
    private Context context;

    public ExpandedTweetAdapter(Context context, TweetsListFragment fragment) {
        super(context);
        this.context = context;
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

        if(status.isFavorited()) {
            holder.rtBy.setVisibility(View.GONE);
            holder.accent.setBackgroundColor(getContext().getResources().getColor(R.color.favourite_accent));
            if (item.isMention() && !mFragment.isMentionsTimeline()) {
                holder.frontView.setBackgroundColor(getContext().getResources().getColor(R.color.reply_background));
            } else {
                holder.frontView.setBackgroundResource(R.drawable.bittweet_tweet_background);
            }
        }
        else if(isRT) {
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

            if(item.isMention() && !mFragment.isMentionsTimeline()) {
                holder.accent.setBackgroundColor(getContext().getResources().getColor(R.color.reply_accent));
                holder.frontView.setBackgroundColor(getContext().getResources().getColor(R.color.reply_background));
            } else {
                holder.accent.setBackgroundColor(Color.TRANSPARENT);
                holder.frontView.setBackgroundResource(R.drawable.bittweet_tweet_background);
            }
        }

        // Load media
        //MediaEntity[] mediaEntities = status.getMediaEntities();
        MediaEntity[] mediaEntities = status.getExtendedMediaEntities();
        URLEntity[] urlEntities = status.getURLEntities();
        final MediaEntity[] finalMedia = mediaEntities;
        final URLEntity[] finalUrl = urlEntities;
        final ImageView media = holder.mediaExpansion;
        final View preview = holder.previewContainer;
        final ImageView preview1 = holder.preview1;
        final ImageView preview2 = holder.preview2;
        final ImageView preview3 = holder.preview3;
        final ImageView preview4 = holder.preview4;

        if(mediaEntities.length > 0) {
            holder.mediaExpansion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] mediaUrl = new String[finalMedia.length];
                    int i = 0;
                    for (MediaEntity media : finalMedia) {
                        mediaUrl[i] = media.getMediaURLHttps();
                        i++;
                    }
                    Bitmap bitmap = Bitmap.createBitmap(media.getWidth(), media.getHeight(), Bitmap.Config.ARGB_8888);
                    ActivityOptions animate = ActivityOptions.makeThumbnailScaleUpAnimation(media, bitmap, 0, 0);
                    Intent intent = new Intent(context, ImageViewerActivity.class);
                    intent.putExtra("MEDIA", mediaUrl);
                    intent.putExtra("POSITION", 0);
                    context.startActivity(intent, animate.toBundle());
                }
            });
            holder.preview1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] mediaUrl = new String[finalMedia.length];
                    int i = 0;
                    for (MediaEntity media : finalMedia) {
                        mediaUrl[i] = media.getMediaURLHttps();
                        i++;
                    }
                    Bitmap bitmap = Bitmap.createBitmap(preview1.getWidth(), preview1.getHeight(), Bitmap.Config.ARGB_8888);
                    ActivityOptions animate = ActivityOptions.makeThumbnailScaleUpAnimation(preview1, bitmap, 0, 0);
                    Intent intent = new Intent(context, ImageViewerActivity.class);
                    intent.putExtra("MEDIA", mediaUrl);
                    intent.putExtra("POSITION", 0);
                    context.startActivity(intent, animate.toBundle());
                }
            });
            holder.preview2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] mediaUrl = new String[finalMedia.length];
                    int i = 0;
                    for (MediaEntity media : finalMedia) {
                        mediaUrl[i] = media.getMediaURLHttps();
                        i++;
                    }
                    Bitmap bitmap = Bitmap.createBitmap(preview2.getWidth(), preview2.getHeight(), Bitmap.Config.ARGB_8888);
                    ActivityOptions animate = ActivityOptions.makeThumbnailScaleUpAnimation(preview2, bitmap, 0, 0);
                    Intent intent = new Intent(context, ImageViewerActivity.class);
                    intent.putExtra("MEDIA", mediaUrl);
                    intent.putExtra("POSITION", 1);
                    context.startActivity(intent, animate.toBundle());
                }
            });
            holder.preview3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] mediaUrl = new String[finalMedia.length];
                    int i = 0;
                    for (MediaEntity media : finalMedia) {
                        mediaUrl[i] = media.getMediaURLHttps();
                        i++;
                    }
                    Bitmap bitmap = Bitmap.createBitmap(preview3.getWidth(), preview3.getHeight(), Bitmap.Config.ARGB_8888);
                    ActivityOptions animate = ActivityOptions.makeThumbnailScaleUpAnimation(preview3, bitmap, 0, 0);
                    Intent intent = new Intent(context, ImageViewerActivity.class);
                    intent.putExtra("MEDIA", mediaUrl);
                    intent.putExtra("POSITION", 2);
                    context.startActivity(intent, animate.toBundle());
                }
            });
            holder.preview4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] mediaUrl = new String[finalMedia.length];
                    int i = 0;
                    for (MediaEntity media : finalMedia) {
                        mediaUrl[i] = media.getMediaURLHttps();
                        i++;
                    }
                    Bitmap bitmap = Bitmap.createBitmap(preview4.getWidth(), preview4.getHeight(), Bitmap.Config.ARGB_8888);
                    ActivityOptions animate = ActivityOptions.makeThumbnailScaleUpAnimation(preview4, bitmap, 0, 0);
                    Intent intent = new Intent(context, ImageViewerActivity.class);
                    intent.putExtra("MEDIA", mediaUrl);
                    intent.putExtra("POSITION", 3);
                    context.startActivity(intent, animate.toBundle());
                }
            });
            if (mediaEntities.length == 1) {
                final MediaEntity displayedMedia = mediaEntities[0];

                // To get the dimensions of the image preview before it is drawn
                media.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        media.getViewTreeObserver().removeOnPreDrawListener(this);

                        // Get the width of the view and set height to 16:10 aspect ratio
                        width = media.getWidth();
                        setImage(media, width, width * 5/8,
                                true, true, true, true, displayedMedia.getMediaURLHttps());
                        return true;
                    }
                });
                holder.mediaExpansion.setVisibility(View.VISIBLE);
                holder.preview1.setVisibility(View.GONE);
                holder.preview2.setVisibility(View.GONE);
                holder.preview3.setVisibility(View.GONE);
                holder.preview4.setVisibility(View.GONE);
            } else if (mediaEntities.length == 2) {
                // To get the dimensions of the image preview before it is drawn
                final MediaEntity firstpic = mediaEntities[0];
                final MediaEntity secondpic = mediaEntities[1];
                preview.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        preview.getViewTreeObserver().removeOnPreDrawListener(this);

                        // Get the width of the view and set height to 16:10 aspect ratio
                        width = preview.getWidth();
                        setImage(preview1, width/2, width * 5/8,
                                true, false, true, false, firstpic.getMediaURLHttps());
                        setImage(preview2, width/2, width * 5/8,
                                false, true, false, true, secondpic.getMediaURLHttps());
                        return true;
                    }
                });
                holder.preview1.setVisibility(View.VISIBLE);
                holder.preview2.setVisibility(View.VISIBLE);
                holder.preview3.setVisibility(View.GONE);
                holder.preview4.setVisibility(View.GONE);
            } else if (mediaEntities.length == 3) {
                // To get the dimensions of the image preview before it is drawn
                final MediaEntity firstpic = mediaEntities[0];
                final MediaEntity secondpic = mediaEntities[1];
                final MediaEntity thirdpic = mediaEntities[2];
                final int padding = (int) convertDpToPixel(3, getContext());
                preview.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        preview.getViewTreeObserver().removeOnPreDrawListener(this);

                        // Get the width of the view and set height to 16:10 aspect ratio
                        width = preview.getWidth();
                        setImage(preview1, width/2, width/2 * 5/8,
                                true, false, false, false, firstpic.getMediaURLHttps());
                        setImage(preview2, width/2, width * 5/8 + padding,
                                false, true, false, true, thirdpic.getMediaURLHttps());
                        setImage(preview3, width/2, width/2 * 5/8,
                                false, false, true, false, secondpic.getMediaURLHttps());
                        return true;
                    }
                });
                holder.preview1.setVisibility(View.VISIBLE);
                holder.preview2.setVisibility(View.VISIBLE);
                holder.preview3.setVisibility(View.VISIBLE);
                holder.preview4.setVisibility(View.GONE);
            } else if (mediaEntities.length == 4) {
                // To get the dimensions of the image preview before it is drawn
                final MediaEntity firstpic = mediaEntities[0];
                final MediaEntity secondpic = mediaEntities[1];
                final MediaEntity thirdpic = mediaEntities[2];
                final MediaEntity fourthpic = mediaEntities[3];
                preview.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        preview.getViewTreeObserver().removeOnPreDrawListener(this);

                        // Get the width of the view and set height to 16:10 aspect ratio
                        width = preview.getWidth();
                        setImage(preview1, width/2, width/2 * 5/8,
                                true, false, false, false, firstpic.getMediaURLHttps());
                        setImage(preview2, width/2, width/2 * 5/8,
                                false, true, false, false, secondpic.getMediaURLHttps());
                        setImage(preview3, width/2, width/2 * 5/8,
                                false, false, true, false, thirdpic.getMediaURLHttps());
                        setImage(preview4, width/2, width/2 * 5/8,
                                false, false, false, true, fourthpic.getMediaURLHttps());
                        return true;
                    }
                });
                holder.preview1.setVisibility(View.VISIBLE);
                holder.preview2.setVisibility(View.VISIBLE);
                holder.preview3.setVisibility(View.VISIBLE);
                holder.preview4.setVisibility(View.VISIBLE);
            }
        } else if(urlEntities.length > 0) {
            holder.mediaExpansion.setVisibility(View.GONE);
            holder.preview1.setVisibility(View.GONE);
            holder.preview2.setVisibility(View.GONE);
            holder.preview3.setVisibility(View.GONE);
            holder.preview4.setVisibility(View.GONE);
            final Pattern pixivID = Pattern.compile("^http://www\\.pixiv\\.net/(member_illust|index)\\.php\\?(?=.*mode=(medium|big))(?=.*illust_id=([0-9]+)).*$", Pattern.CASE_INSENSITIVE);
            for (final URLEntity urlEntity : urlEntities) {
                Matcher pixivMatcher = pixivID.matcher(urlEntity.getExpandedURL());
                if (urlEntity.getExpandedURL().matches("^https?://(?:[a-z\\-]+\\.)+[a-z]{2,6}(?:/[^/#?]+)+\\.(?:jpe?g|gif|png)$")) {
                    // To get the dimensions of the image preview before it is drawn
                    media.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            media.getViewTreeObserver().removeOnPreDrawListener(this);

                            // Get the width of the view and set height to 16:10 aspect ratio
                            width = media.getWidth();
                            setImage(media, width, width * 5/8,
                                    true, true, true, true, urlEntity.getExpandedURL());
                            return true;
                        }
                    });
                    holder.mediaExpansion.setVisibility(View.VISIBLE);
                    holder.mediaExpansion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] mediaUrl = new String[1];
                            int i = 0;
                            mediaUrl[0] = finalUrl[0].getExpandedURL();
                            Bitmap bitmap = Bitmap.createBitmap(media.getWidth(), media.getHeight(), Bitmap.Config.ARGB_8888);
                            ActivityOptions animate = ActivityOptions.makeThumbnailScaleUpAnimation(media, bitmap, 0, 0);
                            Intent intent = new Intent(context, ImageViewerActivity.class);
                            intent.putExtra("MEDIA", mediaUrl);
                            intent.putExtra("POSITION", 0);
                            context.startActivity(intent, animate.toBundle());
                        }
                    });
                    break;
                } else if (pixivMatcher.find()) {
                    String id = pixivMatcher.group(3);
                    System.err.println("Pixiv id is " + id);
                    final String url = "http://embed.pixiv.net/decorate.php?illust_id=" + id;
                    media.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            media.getViewTreeObserver().removeOnPreDrawListener(this);

                            // Get the width of the view and set height to 16:10 aspect ratio
                            width = media.getWidth();
                            setImage(media, width, width * 5/8,
                                    true, true, true, true, url);
                            return true;
                        }
                    });
                    holder.mediaExpansion.setVisibility(View.VISIBLE);
                    holder.mediaExpansion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] mediaUrl = new String[1];
                            int i = 0;
                            mediaUrl[0] = url;
                            Bitmap bitmap = Bitmap.createBitmap(media.getWidth(), media.getHeight(), Bitmap.Config.ARGB_8888);
                            ActivityOptions animate = ActivityOptions.makeThumbnailScaleUpAnimation(media, bitmap, 0, 0);
                            Intent intent = new Intent(context, ImageViewerActivity.class);
                            intent.putExtra("MEDIA", mediaUrl);
                            intent.putExtra("POSITION", 0);
                            context.startActivity(intent, animate.toBundle());
                        }
                    });
                }
            }
        } else {
            holder.mediaExpansion.setVisibility(View.GONE);
            holder.preview1.setVisibility(View.GONE);
            holder.preview2.setVisibility(View.GONE);
            holder.preview3.setVisibility(View.GONE);
            holder.preview4.setVisibility(View.GONE);
        }
    }

    // Set the image preview on timeline with Ion
    public void setImage(ImageView view, int width, int height,
                         boolean TL, boolean TR, boolean BL, boolean BR, String url) {
        Ion.with(view)
                .animateGif(false)
                .resize(width, height)
                .centerCrop()
                .transform(new RoundedTransformation(20, 0, TL, TR, BL, BR))
                .load(url);
    }
}
