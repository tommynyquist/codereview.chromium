package com.chrome.codereview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.Message;
import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.PatchSetFile;
import com.chrome.codereview.utils.ViewUtils;

/**
* Created by sergeyv on 22/4/14.
*/
class IssueAdapter extends BaseExpandableListAdapter {

    private static final int PATCH_SET_GROUP_TYPE = 0;
    private static final int MESSAGE_GROUP_TYPE = 1;

    private Issue issue;
    private LayoutInflater inflater;

    public IssueAdapter(Context context) {
        super();
        inflater = LayoutInflater.from(context);
    }

    public View getMessageView(Message message, View convertView, ViewGroup parent, boolean isExpanded) {
        if ((convertView == null && !isExpanded) || (convertView != null && convertView.getId() != R.id.message_collapsed && !isExpanded)) {
            convertView = inflater.inflate(R.layout.message_collapsed, parent, false);
        }
        if ((convertView == null && isExpanded) || (convertView != null && convertView.getId() != R.id.message_expanded && isExpanded)) {
            convertView = inflater.inflate(R.layout.message_expanded, parent, false);
        }
        ViewUtils.setText(convertView, android.R.id.text1, message.getText());
        ViewUtils.setText(convertView, android.R.id.text2, message.getSender());
        return convertView;
    }

    public View getPatchSetView(PatchSet patchSet, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
        }
        ViewUtils.setText(convertView, android.R.id.text1, patchSet.message());
        return convertView;
    }

    void setIssue(Issue issue) {
        this.issue = issue;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        if (issue == null) {
            return 0;
        }
        return issue.patchSets().size() + issue.messages().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        switch (getGroupType(groupPosition)) {
            case PATCH_SET_GROUP_TYPE:
                return ((PatchSet) getGroup(groupPosition)).files().size();
            case MESSAGE_GROUP_TYPE:
                return 0;
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (groupPosition < issue.patchSets().size()) {
            return issue.patchSets().get(groupPosition);
        }
        return issue.messages().get(groupPosition - issue.patchSets().size());
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        switch (getGroupType(groupPosition)) {
            case PATCH_SET_GROUP_TYPE:
                return getPatchSetView((PatchSet) getGroup(groupPosition), convertView, parent);
            case MESSAGE_GROUP_TYPE:
                return getMessageView((Message) getGroup(groupPosition), convertView, parent, isExpanded);
        }
        throw new IllegalStateException("Unknown type " + getGroupType(groupPosition));
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (getGroupType(groupPosition) == MESSAGE_GROUP_TYPE) {
            throw new IllegalStateException("No child for message type");
        }
        PatchSetFile patchSetFile = issue.patchSets().get(groupPosition).files().get(childPosition);
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        ViewUtils.setText(convertView, android.R.id.text1, patchSetFile.path());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public int getGroupTypeCount() {
        return 2;
    }

    @Override
    public int getChildTypeCount() {
        return 1;
    }

    @Override
    public int getGroupType(int groupPosition) {
        return groupPosition < issue.patchSets().size() ? PATCH_SET_GROUP_TYPE : MESSAGE_GROUP_TYPE;
    }
}
