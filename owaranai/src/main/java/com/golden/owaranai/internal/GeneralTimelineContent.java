package com.golden.owaranai.internal;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GeneralTimelineContent implements TimelineContent {
    private Context context;
    private Map<String, StatusItem> globalStatusMap;
    private Map<String, StatusItem> statusMap;
    private List<StatusItem> statuses;

    protected static final int PER_PAGE = 40;

    public GeneralTimelineContent(Context context, Map<String, StatusItem> globalStatusMap) {
        this.context = context;
        this.globalStatusMap = globalStatusMap;
        this.statuses = new ArrayList<StatusItem>();
        this.statusMap = new HashMap<String, StatusItem>();
    }

    protected Context getContext() {
        return context;
    }

    protected Map<String, StatusItem> getGlobalStatusMap() {
        return globalStatusMap;
    }

    protected Map<String, StatusItem> getStatusMap() {
        return statusMap;
    }

    protected List<StatusItem> getStatuses() {
        return statuses;
    }

    protected abstract boolean canAddItem(StatusItem statusItem);

    protected void addItem(StatusItem statusItem, boolean prepend) {
        if(statusMap.containsKey(statusItem.getId()) || !canAddItem(statusItem)) {
            return;
        }

        if(prepend) {
            statuses.add(0, statusItem);
        } else {
            statuses.add(statusItem);
        }

        statusMap.put(statusItem.getId(), statusItem);
        globalStatusMap.put(statusItem.getId(), statusItem);
    }


    @Override
    public StatusItem getStatusItem(String id) {
        return getStatusMap().get(id);
    }

    @Override
    public StatusItem getStatusItemAt(int position) {
        return getStatuses().get(position);
    }

    @Override
    public List<StatusItem> getStatusItems() {
        return getStatuses();
    }
}
