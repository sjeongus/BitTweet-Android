package org.bittweet.android.internal;

import android.app.Activity;

import java.util.List;

/**
 * Created by soomin on 3/26/2014.
 */

// An interface for timelines in order to implement with polymorphism
public interface TimelineContent {
    public StatusItem getStatusItem(String id);
    public StatusItem getStatusItemAt(int position);
    public List<StatusItem> getStatusItems();
    public void update(Activity activity);
    public void loadMore(Activity activity);
    public void notifyDataSetChanged();
}
