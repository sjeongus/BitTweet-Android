package com.golden.owaranai;

import android.app.Application;
import com.golden.owaranai.internal.HomeTimelineContent;

public class ApplicationController extends Application {
    public HomeTimelineContent homeTimelineContent;

    @Override
    public void onCreate() {
        super.onCreate();
        homeTimelineContent = new HomeTimelineContent(this);
    }
}
