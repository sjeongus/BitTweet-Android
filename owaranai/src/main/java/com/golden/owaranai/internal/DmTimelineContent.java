package com.golden.owaranai.internal;

import android.content.Context;

import java.util.List;

public class DmTimelineContent implements TimelineContent {
    private Context context;

    public DmTimelineContent(Context context) {
        this.context = context;
    }

    @Override
    public StatusItem getStatusItem(String id) {
        return null;
    }

    @Override
    public StatusItem getStatusItemAt(int position) {
        return null;
    }

    @Override
    public List<StatusItem> getStatusItems() {
        return null;
    }

    @Override
    public void update() {

    }

    @Override
    public void loadMore() {

    }
}