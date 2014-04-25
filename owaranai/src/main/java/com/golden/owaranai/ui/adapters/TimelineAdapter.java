package com.golden.owaranai.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.StatusItem;
import com.golden.owaranai.ui.util.RoundedTransformation;
import com.squareup.picasso.Picasso;
import twitter4j.Status;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by soomin on 3/23/2014.
 */
public class TimelineAdapter extends BaseAdapter {
    private static final String TAG = "TimelineAdapter";
    private final Context context;
    private List<StatusItem> statusItems;
    private DateFormat dateFormat;

    public TimelineAdapter(Context context) {
        this.context = context;
        this.statusItems = new ArrayList<StatusItem>();
        this.dateFormat = new SimpleDateFormat("MMM d yyyy, K:mm a", Locale.getDefault());
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
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;

        // Reuse old view if it's present. But remember: Always set all of the view elements, because otherwise
        // the old state will be preserved (basically, don't rely on TextViews &co having the state you last set them to)
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.tweets_list_item, parent, false);
        } else {
            rowView = convertView;
        }

        StatusItem item = (StatusItem) getItem(position);
        Status status = item.getStatus();
        Status originalStatus = status;
        boolean isRT = false;

        if(status.isRetweet()) {
            isRT = true;
            status = status.getRetweetedStatus();
        }

        ImageView avatarImage = (ImageView) rowView.findViewById(R.id.avatar);
        TextView userName = (TextView) rowView.findViewById(R.id.username);
        TextView displayName = (TextView) rowView.findViewById(R.id.displayname);
        TextView time = (TextView) rowView.findViewById(R.id.time);
        TextView tweet = (TextView) rowView.findViewById(R.id.tweet);
        View accent = rowView.findViewById(R.id.accent_container);
        TextView rtBy = (TextView) rowView.findViewById(R.id.retweeted_by);

        tweet.setText(status.getText());
        displayName.setText(status.getUser().getName());
        userName.setText("@" + status.getUser().getScreenName());
        time.setText(dateFormat.format(status.getCreatedAt()));

        // Use Picasso to retrieve and set profile images. Get from cache if already exists.
        Picasso.with(context)
                .load(status.getUser().getBiggerProfileImageURL())
                .transform(new RoundedTransformation(50, 0))
                .into(avatarImage);

        if(isRT) {
            rtBy.setVisibility(View.VISIBLE);
            rtBy.setText(String.format(context.getString(R.string.retweeted_by), originalStatus.getUser().getScreenName()));

            if(item.isMention()) {
                rowView.setBackgroundColor(context.getResources().getColor(R.color.reply_background));
            } else {
                rowView.setBackgroundColor(0x000000);
            }

            accent.setBackgroundColor(context.getResources().getColor(R.color.retweet_accent));
        } else {
            rtBy.setVisibility(View.GONE);

            if(status.isFavorited()) {
                accent.setBackgroundColor(context.getResources().getColor(R.color.favourite_accent));
                rowView.setBackgroundColor(0x000000);
            } else if(item.isMention()) {
                accent.setBackgroundColor(context.getResources().getColor(R.color.reply_accent));
                rowView.setBackgroundColor(context.getResources().getColor(R.color.reply_background));
            } else {
                accent.setBackgroundColor(0x00000000);
                rowView.setBackgroundColor(0x000000);
            }
        }

        return rowView;
    }

    public void setStatuses(List<StatusItem> data) {
        statusItems = data;
    }
}
