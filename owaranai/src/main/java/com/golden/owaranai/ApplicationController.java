package com.golden.owaranai;

import android.app.Application;
import com.golden.owaranai.twitter.HomeTimelineContent;

public class ApplicationController extends Application {
    public HomeTimelineContent homeTimelineContent;

    @Override
    public void onCreate() {
        super.onCreate();
        homeTimelineContent = new HomeTimelineContent(this);
    }
}
