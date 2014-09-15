package org.bittweet.android.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.bittweet.android.R;
import org.bittweet.android.internal.GeneralTimelineContent;
import org.bittweet.android.internal.TimelineContent;
import org.bittweet.android.ui.adapters.TimelineAdapter;
import org.bittweet.android.ui.util.ConnectionDetector;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class TweetsListFragment extends Fragment implements OnRefreshListener, AdapterView.OnItemClickListener {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String STATE_SCROLL = "scroll_position";
    private static final String FIRST_RUN = "first_run";
    private static final String TAG = "TweetsListFragment";

    private Callbacks callbacks = dummyCallbacks;
    private int activatedPosition = ListView.INVALID_POSITION;

    private Activity activity;
    private TimelineContent timelineContent;
    private TimelineAdapter adapter;

    private PullToRefreshLayout pullToRefreshLayout;
    private ListView listView;
    private Button loadMoreBtn;
    private boolean activateOnItemClick;
    private int lastUsedScrollY = 0;

    private SharedPreferences prefs;
    private SharedPreferences twitPrefs;
    private boolean streaming;
    private Handler handler;
    private Runnable streamrun;

    public boolean isMentionsTimeline() {
        return false;
    }

    // TODO: Clear user timeline on logout
    public void clearTimeline() {
        //listView.setAdapter(null);
        //adapter.clearList();
    }

    @Override
    public void onRefreshStarted(View view) {
        updateAdapter();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        System.out.println("Item was clicked!");
        callbacks.onItemSelected(timelineContent.getStatusItemAt(position).getId());
    }

    public interface Callbacks {
        public void onItemSelected(String id);
    }

    private static Callbacks dummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {}
    };

    public TweetsListFragment() {}

    // AsyncTask that executes update
    public class TimelineTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            try {
                timelineContent.update(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.attachStatusesList(timelineContent.getStatusItems());
            if (!streaming) {
                pullToRefreshLayout.setRefreshComplete();
            }
        }
    }

    private class LoadMoreTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                timelineContent.loadMore(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.attachStatusesList(timelineContent.getStatusItems());
        }
    }

    public void updateAdapter() {
        new TimelineTask().execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        activity = getActivity();
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        twitPrefs = getActivity().getSharedPreferences("MyTwitter", Context.MODE_PRIVATE);
        // Check if the device is connected to WiFi, and set streaming based on setting
        if (ConnectionDetector.isOnWifi(getActivity())) {
            streaming = prefs.getBoolean("pref_key_streaming", false);
        } else {
            streaming = false;
        }
        adapter = new TimelineAdapter(activity, TweetsListFragment.this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweets_list, container, false);

        pullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        listView = (ListView) view.findViewById(android.R.id.list);
        loadMoreBtn = new Button(getActivity());

        // After the inheriting class defined its timelineContent instance, we should connect our adapter
        // to its existing statusItems List object, in case it already contains tweets. This happens before
        // we restore savedState, so *then* we are able to return to the saved scroll position.
        adapter.attachStatusesList(timelineContent.getStatusItems());

        loadMoreBtn.setText("Load more");
        listView.addFooterView(loadMoreBtn);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Item was clicked!");
                callbacks.onItemSelected(timelineContent.getStatusItemAt(position).getId());
            }
        });*/
        listView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);

        loadMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadMoreTask().execute();
            }
        });

        // Check if first time app is being run. If so, refresh.
        //if (twitPrefs.getBoolean("FIRST_RUN", false)) {
            updateAdapter();
            twitPrefs.edit().putBoolean("FIRST_RUN", false).commit();
        //}

        // Check if streaming is enabled. If not, set up pull to refresh
        if (!streaming && twitPrefs.getInt("Rate_Limited", 0) > 0) {
            ActionBarPullToRefresh.from(activity)
                    .allChildrenArePullable()
                    .listener(this)
                    .setup(pullToRefreshLayout);
        // If streaming is detached and enabled, attach the stream to adapter
        } else if(!isDetached() && streaming) {
            // Streaming
            // Check if user is currently rate limited. If so, start stream after rate limit end
            int seconds = twitPrefs.getInt("Rate_Limited", 0);
            if (seconds > 0) {
                handler = new Handler();
                handler.postDelayed(streamrun = new Runnable() {
                    @Override
                    public void run() {
                        updateAdapter();
                        ((GeneralTimelineContent) timelineContent).attachStreamToAdapter(adapter);
                    }
                }, seconds * 1000);
            // If not rate limited, update adapter and start stream
            } else {
                //updateAdapter();
                ((GeneralTimelineContent) timelineContent).attachStreamToAdapter(adapter);
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        // TODO: Recover scrolled postion
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SCROLL)) {
            listView.scrollTo(0, savedInstanceState.getInt(STATE_SCROLL));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        callbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (handler != null) {
            handler.removeCallbacks(streamrun);
        }

        // Reset the active callbacks interface to the dummy implementation.
        callbacks = dummyCallbacks;

        // Detach streaming
        ((GeneralTimelineContent) timelineContent).detachStream();

        // Save scroll position while we still can
        lastUsedScrollY = listView.getScrollY();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (activatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, activatedPosition);
        }

        outState.putInt(STATE_SCROLL, lastUsedScrollY);
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        this.activateOnItemClick = activateOnItemClick;
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            listView.setItemChecked(activatedPosition, false);
        } else {
            listView.setItemChecked(position, true);
        }

        activatedPosition = position;
    }

    protected void setTimelineContent(TimelineContent content) {
        this.timelineContent = content;
    }
}
