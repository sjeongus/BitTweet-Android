package com.golden.owaranai.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.StatusItem;
import com.golden.owaranai.services.TweetService;
import com.golden.owaranai.ui.NewTweetActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by soomin on 3/23/2014.
 */
public class TimelineAdapter extends BaseAdapter {
    private static final String TAG = "TimelineAdapter";
    private final Context context;
    private final TweetAdapter tweetAdapter;
    private List<StatusItem> statusItems;

    public TimelineAdapter(Context context) {
        this.context = context;
        this.statusItems = new ArrayList<StatusItem>();
        this.tweetAdapter = new ExpandedTweetAdapter(context);
    }

    @Override
    public int getCount() {
        return statusItems.size();
    }

    @Override
    public Object getItem(int position) {
        return statusItems.get(position);
    }

    @Override
    public long getItemId(int i) {
        return statusItems.get(i).getStatus().getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;

        // Use view holders
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.tweets_list_item, parent, false);
            holder = new ViewHolder();

            holder.initialize(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ((SwipeListView) parent).recycle(convertView, position);
        final StatusItem item = (StatusItem) getItem(position);

        tweetAdapter.recreateView(item, holder);

        // Fix item clicks
        final View finalConvertView = convertView;

        holder.tweetContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) finalConvertView.getParent()).performItemClick(finalConvertView, position, getItemId(position));
            }
        });

        // Buttons
        holder.replyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent replyIntent = new Intent(context, NewTweetActivity.class);
                replyIntent.setAction(NewTweetActivity.INTENT_REPLY);
                replyIntent.putExtra(NewTweetActivity.ARG_REPLY_TO_ID, item.getId());
                context.startActivity(replyIntent);
            }
        });

        holder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent favIntent = new Intent(context, TweetService.class);
                favIntent.setAction(TweetService.ACTION_FAV);
                favIntent.putExtra(TweetService.ARG_TWEET_ID, item.getId());
                context.startService(favIntent);
            }
        });

        holder.retweetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent rtIntent = new Intent(context, TweetService.class);
                rtIntent.setAction(TweetService.ACTION_RT);
                rtIntent.putExtra(TweetService.ARG_TWEET_ID, item.getId());
                context.startService(rtIntent);
            }
        });

        return convertView;
    }

    public void attachStatusesList(List<StatusItem> data) {
        statusItems = data;
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        public ImageView avatarImage;
        public TextView userName;
        public TextView displayName;
        public TextView time;
        public TextView tweet;
        public View accent;
        public TextView rtBy;
        public ImageView mediaExpansion;
        public View frontView;
        public View tweetContainer;
        public ImageButton replyBtn;
        public ImageButton retweetBtn;
        public ImageButton favBtn;

        public void initialize(View container) {
            this.avatarImage = (ImageView) container.findViewById(R.id.avatar);
            this.userName = (TextView) container.findViewById(R.id.username);
            this.displayName = (TextView) container.findViewById(R.id.displayname);
            this.time = (TextView) container.findViewById(R.id.time);
            this.tweet = (TextView) container.findViewById(R.id.tweet);
            this.accent = container.findViewById(R.id.accent_container);
            this.rtBy = (TextView) container.findViewById(R.id.retweeted_by);
            this.mediaExpansion = (ImageView) container.findViewById(R.id.media_expansion);
            this.frontView = container.findViewById(R.id.front_view);
            this.tweetContainer = container.findViewById(R.id.tweet_text_container);
            this.replyBtn = (ImageButton) container.findViewById(R.id.button_reply);
            this.retweetBtn = (ImageButton) container.findViewById(R.id.button_retweet);
            this.favBtn = (ImageButton) container.findViewById(R.id.button_fav);
        }
    }
}
