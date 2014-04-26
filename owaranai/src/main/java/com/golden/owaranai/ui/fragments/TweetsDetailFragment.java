package com.golden.owaranai.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.golden.owaranai.ApplicationController;
import com.golden.owaranai.R;
import com.golden.owaranai.internal.StatusItem;
import com.golden.owaranai.ui.util.TweetFormatter;

public class TweetsDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private StatusItem statusItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TweetsDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            statusItem = ((ApplicationController) getActivity().getApplication()).getStatus(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tweets_detail, container, false);

        if (statusItem != null) {
            ((TextView) rootView.findViewById(R.id.tweets_detail)).setText(TweetFormatter.formatStatusText(getActivity(), statusItem.getStatus()));
        }

        return rootView;
    }
}
