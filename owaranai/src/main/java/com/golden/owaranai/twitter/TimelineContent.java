package com.golden.owaranai.twitter;

import android.content.SharedPreferences;

/**
 * Created by soomin on 3/26/2014.
 */
public interface TimelineContent {

    public void addItem(StatusItem status);
    public void getTimeline(SharedPreferences mSharedPreferences);
}
