package com.chrome.codereview.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

/**
 * Created by sergeyv on 28/5/14.
 */
public class HeadedExpandableListAdapter extends MergeExpandableListAdapter {

    private static class HeaderAdapter extends LinearExpandableAdapter {

        private final int headerLayout;
        private final int headerResText;

        private HeaderAdapter(int headerLayout, int headerResText) {
            this.headerLayout = headerLayout;
            this.headerResText = headerResText;
        }

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(headerLayout, parent, false);
                ViewUtils.setText(convertView, android.R.id.text1, headerResText);
            }
            return convertView;
        }
    }

    public HeadedExpandableListAdapter(BaseExpandableListAdapter mainAdapter, int headerLayout, int headerResText) {
        super(new HeaderAdapter(headerLayout, headerResText), mainAdapter);
    }

    @Override
    public int getGroupCount() {
        int count = super.getGroupCount();
        //Just a header
        if (count == 1) {
            return 0;
        }
        return count;
    }
}
