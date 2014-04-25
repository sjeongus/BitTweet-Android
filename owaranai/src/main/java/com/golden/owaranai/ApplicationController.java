package com.golden.owaranai;

import android.app.Application;
import com.golden.owaranai.internal.HomeTimelineContent;
import com.golden.owaranai.internal.MentionsTimelineContent;
import com.golden.owaranai.internal.StatusItem;
import com.golden.owaranai.internal.TimelineContent;

import java.util.HashMap;

public class ApplicationController extends Application {
    private TimelineContent homeTimelineContent;
    private TimelineContent mentionsTimelineContent;
    private TimelineContent dmTimelineContent;
    private HashMap<String, StatusItem> statusMap;

    @Override
    public void onCreate() {
        super.onCreate();

        statusMap = new HashMap<String, StatusItem>();
        homeTimelineContent = new HomeTimelineContent(this, statusMap);
        mentionsTimelineContent = new MentionsTimelineContent(this, statusMap);
    }

    public TimelineContent getHomeTimelineContent() {
        return homeTimelineContent;
    }

    public TimelineContent getMentionsTimelineContent() {
        return mentionsTimelineContent;
    }

    public TimelineContent getDmTimelineContent() {
        return dmTimelineContent;
    }

    public StatusItem getStatus(String id) {
        return statusMap.get(id);
    }
}
