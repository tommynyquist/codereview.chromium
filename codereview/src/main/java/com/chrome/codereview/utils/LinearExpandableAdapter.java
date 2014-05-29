package com.chrome.codereview.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sergeyv on 29/5/14.
 */
public abstract class LinearExpandableAdapter extends BaseIDExpandableAdapter {

    @Override
    public int getChildrenCount(int groupPosition) {
        return 0;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
