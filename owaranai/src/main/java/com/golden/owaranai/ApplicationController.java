package com.golden.owaranai;

import android.app.Application;
import com.golden.owaranai.internal.DmTimelineContent;
import com.golden.owaranai.internal.HomeTimelineContent;
import com.golden.owaranai.internal.MentionsTimelineContent;
import com.golden.owaranai.internal.TimelineContent;

public class ApplicationController extends Application {
    public TimelineContent homeTimelineContent;
    public TimelineContent mentionsTimelineContent;
    public TimelineContent dmTimelineContent;

    @Override
    public void onCreate() {
        super.onCreate();

        homeTimelineContent = new HomeTimelineContent(this);
        mentionsTimelineContent = new MentionsTimelineContent(this);
        dmTimelineContent = new DmTimelineContent(this);
    }
}
