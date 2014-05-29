package org.bittweet.android.ui.adapters;

import org.bittweet.android.internal.StatusItem;

public interface TweetAdapter {
    public void recreateView(StatusItem item, TweetViewHolder holder);
}
