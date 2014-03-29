package com.golden.owaranai;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.golden.owaranai.twitter.TimelineContent;

import twitter4j.ResponseList;

/**
 * Created by soomin on 3/23/2014.
 */
public class TimelineAdapter extends BaseAdapter {

    private Activity activity;

    private ResponseList mStatuses;

    private static LayoutInflater inflater = null;

    public TimelineAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        int count = 0;
        if (mStatuses != null) {
            count = mStatuses.size();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.tweet_row, null);
        }
        //SharedPreferences mSharedPreferences = activity.getSharedPreferences("MyPref", 0);
        //new TimelineContent.getTimelineTask().execute(mSharedPreferences);
        if (mStatuses != null) {
            ImageView avatarImage = (ImageView) vi.findViewById(R.id.avatar);
            TextView displayName = (TextView) vi.findViewById(R.id.displayname);
        }

        return vi;
    }

    public void setStatuses(ResponseList data) {
        mStatuses = data;
    }
}
