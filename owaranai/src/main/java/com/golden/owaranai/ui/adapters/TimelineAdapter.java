package com.golden.owaranai.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.StatusItem;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by soomin on 3/23/2014.
 */
public class TimelineAdapter extends BaseAdapter {
    private static final String TAG = "TimelineAdapter";
    private final Context context;
    private List<StatusItem> statusItems;

    public TimelineAdapter(Context context) {
        this.context = context;
        this.statusItems = new ArrayList<StatusItem>();
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

        CircleImageView avatarImage = (CircleImageView) rowView.findViewById(R.id.avatar);
        TextView userName = (TextView) rowView.findViewById(R.id.username);
        TextView displayName = (TextView) rowView.findViewById(R.id.displayname);
        TextView time = (TextView) rowView.findViewById(R.id.time);
        TextView tweet = (TextView) rowView.findViewById(R.id.tweet);

        tweet.setText(status.getText());
        displayName.setText(status.getUser().getName());
        userName.setText("@" + status.getUser().getScreenName());
        time.setText(status.getCreatedAt().toString());

        // Use Picasso to retrieve and set profile images. Get from cache if already exists.
        Picasso nAvatar = Picasso.with(context);
        nAvatar.setDebugging(true);
        nAvatar.load(status.getUser().getBiggerProfileImageURL()).into(avatarImage);

        if(status.isRetweet()) {
            // TODO: Color left border green
        }

        return rowView;
    }

    public void setStatuses(List<StatusItem> data) {
        statusItems = data;
    }
}
