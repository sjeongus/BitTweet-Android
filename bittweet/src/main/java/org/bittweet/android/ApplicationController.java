package org.bittweet.android;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.bittweet.android.internal.HomeTimelineContent;
import org.bittweet.android.internal.MentionsTimelineContent;
import org.bittweet.android.internal.StatusItem;
import org.bittweet.android.internal.TimelineContent;
import org.bittweet.android.internal.TwitterStreamRouter;

import java.util.HashMap;

public class ApplicationController extends Application {
    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    private TimelineContent homeTimelineContent;
    private TimelineContent mentionsTimelineContent;
    private TwitterStreamRouter twitterStreamRouter;
    private HashMap<String, StatusItem> statusMap;
    HashMap<TrackerName, Tracker> mTrackers;

    @Override
    public void onCreate() {
        super.onCreate();

        mTrackers = new HashMap<TrackerName, Tracker>();
        statusMap = new HashMap<String, StatusItem>();
        homeTimelineContent = new HomeTimelineContent(getApplicationContext(), statusMap);
        mentionsTimelineContent = new MentionsTimelineContent(getApplicationContext(), statusMap);
        twitterStreamRouter = new TwitterStreamRouter(getApplicationContext());
    }

    public TimelineContent getHomeTimelineContent() {
        return homeTimelineContent;
    }

    public TimelineContent getMentionsTimelineContent() {
        return mentionsTimelineContent;
    }

    public StatusItem getStatus(String id) {
        return statusMap.get(id);
    }

    public TwitterStreamRouter getTwitterStreamRouter() {
        return twitterStreamRouter;
    }

    public void notifyStatusChanged() {
        homeTimelineContent.notifyDataSetChanged();
        mentionsTimelineContent.notifyDataSetChanged();
    }

    /*public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }*/
}
