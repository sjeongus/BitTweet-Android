package org.bittweet.android.ui.fragments;

import android.app.ActionBar;
import android.os.Bundle;

import org.bittweet.android.ApplicationController;
import org.bittweet.android.R;

public class HomeTweetsListFragment extends TweetsListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setIcon(R.drawable.ic_nav_timeline);
        setTimelineContent(((ApplicationController) getActivity().getApplication()).getHomeTimelineContent());
    }
}
