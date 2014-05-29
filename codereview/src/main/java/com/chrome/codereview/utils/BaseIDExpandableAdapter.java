package com.chrome.codereview.utils;

import android.widget.BaseExpandableListAdapter;

/**
 * Created by sergeyv on 29/5/14.
 */
public abstract class BaseIDExpandableAdapter extends BaseExpandableListAdapter {

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
