package com.golden.owaranai;

import android.app.Application;
import com.golden.owaranai.internal.DmTimelineContent;
import com.golden.owaranai.internal.HomeTimelineContent;
import com.golden.owaranai.internal.MentionsTimelineContent;
import com.golden.owaranai.internal.TimelineContent;

public class ApplicationController extends Application {
    private TimelineContent homeTimelineContent;
    private TimelineContent mentionsTimelineContent;
    private TimelineContent dmTimelineContent;

    @Override
    public void onCreate() {
        super.onCreate();

        homeTimelineContent = new HomeTimelineContent(this);
        mentionsTimelineContent = new MentionsTimelineContent(this);
        dmTimelineContent = new DmTimelineContent(this);
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
}
