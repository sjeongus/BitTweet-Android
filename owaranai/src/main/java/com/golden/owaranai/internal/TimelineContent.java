package com.golden.owaranai.internal;

import java.util.List;

/**
 * Created by soomin on 3/26/2014.
 */

// An interface for timelines in order to implement with polymorphism
public interface TimelineContent {
    public StatusItem getStatusItem(String id);
    public StatusItem getStatusItemAt(int position);
    public List<StatusItem> getStatusItems();
    public void update();
    public void loadMore();
}
