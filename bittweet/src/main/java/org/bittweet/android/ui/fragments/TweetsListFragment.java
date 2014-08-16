package org.bittweet.android.ui.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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

    @Override
    public void onRefreshStarted(View view) {
        updateAdapter();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
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
        private final boolean firstRun;

        public TimelineTask(boolean firstRun) {
            this.firstRun = firstRun;
        }

        @Override
        protected Void doInBackground(Void... args) {
            try {
                timelineContent.update();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.attachStatusesList(timelineContent.getStatusItems());
            pullToRefreshLayout.setRefreshComplete();

            if(firstRun && !isDetached() && ConnectionDetector.isOnWifi(getActivity())) {
                // Streaming
                ((GeneralTimelineContent) timelineContent).attachStreamToAdapter(adapter);
            }
        }
    }

    private class LoadMoreTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                timelineContent.loadMore();
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

    public void updateAdapter(boolean firstRun) {
        new TimelineTask(firstRun).execute();
    }

    public void updateAdapter() {
        updateAdapter(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        activity = getActivity();
        adapter = new TimelineAdapter(activity);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateAdapter(true);
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
        listView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);

        loadMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadMoreTask().execute();
            }
        });

        ActionBarPullToRefresh.from(activity)
                .allChildrenArePullable()
                .listener(this)
                .setup(pullToRefreshLayout);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

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
