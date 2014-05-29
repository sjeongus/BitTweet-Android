package org.bittweet.android.ui.fragments;

import android.os.Bundle;
import org.bittweet.android.ApplicationController;

public class HomeTweetsListFragment extends TweetsListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTimelineContent(((ApplicationController) getActivity().getApplication()).getHomeTimelineContent());
    }
}
