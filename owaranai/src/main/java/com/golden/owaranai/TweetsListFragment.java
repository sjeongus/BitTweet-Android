package com.golden.owaranai;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.golden.owaranai.twitter.HomeTimelineContent;

import static com.golden.owaranai.twitter.HomeTimelineContent.statuses;

/**
 * A list fragment representing a list of Tweets. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TweetsDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class TweetsListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TweetsListFragment() {
    }

    // AsyncTask that executes getTimeline
    public class TimelineTask extends AsyncTask<SharedPreferences, Void, Void> {

        public TimelineTask() {}

        @Override
        protected void onPreExecute() {
            // before the network request begins, show a progress indicator
            //showProgressDialog("Fetching timeline...");
        }

        @Override
        protected Void doInBackground(SharedPreferences... mPreferences) {
            try {
                System.out.println("Getting timeline now!");
                HomeTimelineContent.getTimeline(mPreferences[0]);
                System.out.println("Got the timeline!");
            } catch (Exception e) {
                System.out.println("Error in fetching home timeline.");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // after the network request completes, hide the progress indicator
            //dismissProgressDialog();
            //showResult(tweets);
            System.out.println("Retrieved timeline! Printing...");
            for (int i = 0; i < statuses.size(); i++)
                System.out.println(statuses.get(i).status.getText());
            Activity activity = getActivity();
            TimelineAdapter adapter = new TimelineAdapter(activity);
            adapter.setStatuses(statuses);
            TweetsListFragment timeline = ((TweetsListFragment) getFragmentManager().findFragmentById(R.id.tweets_list));
            timeline.setListAdapter(adapter);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        //View view = getView();
        //TimelineAdapter adapter = new TimelineAdapter(activity);
        SharedPreferences mSharedPreferences = activity.getSharedPreferences("MyTwitter", 0);
        new TimelineTask().execute(mSharedPreferences);
        //adapter.setStatuses(statuses);
        //ListView timeline = (ListView) view.findViewById(R.id.tweets_list);
        //TweetsListFragment timeline2 = ((TweetsListFragment) getFragmentManager().findFragmentById(R.id.tweets_list));
        //timeline2.setListAdapter(adapter);

        /*setListAdapter(new ArrayAdapter<StatusItem>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                statuses));*/
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(statuses.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
