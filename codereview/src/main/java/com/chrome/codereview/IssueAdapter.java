package com.chrome.codereview;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.Message;
import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.PatchSetFile;
import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.ViewUtils;

/**
 * Created by sergeyv on 22/4/14.
 */
class IssueAdapter extends BaseExpandableListAdapter {

    private static final int PATCH_SET_GROUP_TYPE = 0;
    private static final int MESSAGE_GROUP_TYPE = 1;
    private static final int MESSAGE_GROUP_HEADER = 2;

    private Issue issue;
    private Context context;
    private LayoutInflater inflater;

    public IssueAdapter(Context context) {
        super();
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public View getMessageView(Message message, View convertView, ViewGroup parent, boolean isExpanded) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.message_item, parent, false);
        }
        TextView messageView = (TextView) convertView.findViewById(R.id.message_text);
        messageView.setSingleLine(!isExpanded);
        if (message.text().isEmpty()) {
            messageView.setText(R.string.empty_message);
            messageView.setTypeface(null, Typeface.ITALIC);
        } else {
            messageView.setText(message.text());
            messageView.setTypeface(null, Typeface.NORMAL);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.expander);
        if (isExpanded) {
            imageView.getDrawable().setState(new int[] {android.R.attr.state_expanded});
        } else {
            imageView.getDrawable().setState(new int[] {});
        }
        ViewUtils.setText(convertView, R.id.sender, message.sender());
        ViewUtils.setText(convertView, R.id.date, DateUtils.createAgoText(context, message.date()));
        View stateView = convertView.findViewById(R.id.state);
        if (message.decoration() != null) {
            stateView.setVisibility(View.VISIBLE);
            stateView.setBackgroundColor(message.decoration().color(context));
        } else {
            stateView.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public View getPatchSetView(int position, View convertView, ViewGroup parent) {
        PatchSet patchSet = (PatchSet) getGroup(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.patchset_collapsed_item, parent, false);
        }
        String patchSetText = context.getString(R.string.patchset, position + 1) + patchSet.message();
        ViewUtils.setText(convertView, R.id.patchset_message, patchSetText);
        return convertView;
    }

    public View getMessageGroupHeader(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_group_header, parent, false);
        }
        ViewUtils.setText(convertView, android.R.id.text1, context.getString(R.string.messages));
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
        return issue.patchSets().size() + issue.messages().size() + 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        switch (getGroupType(groupPosition)) {
            case PATCH_SET_GROUP_TYPE:
                return ((PatchSet) getGroup(groupPosition)).files().size();
            case MESSAGE_GROUP_HEADER:
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
        return issue.messages().get(groupPosition - issue.patchSets().size() - 1);
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
                return getPatchSetView(groupPosition, convertView, parent);
            case MESSAGE_GROUP_TYPE:
                return getMessageView((Message) getGroup(groupPosition), convertView, parent, isExpanded);
            case MESSAGE_GROUP_HEADER:
                return getMessageGroupHeader(convertView, parent);
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
            convertView = inflater.inflate(R.layout.patchset_file_item, parent, false);
        }
        ViewUtils.setText(convertView, R.id.file_name, patchSetFile.path());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public int getGroupTypeCount() {
        return 3;
    }

    @Override
    public int getChildTypeCount() {
        return 1;
    }

    @Override
    public int getGroupType(int groupPosition) {
        int diff = groupPosition - issue.patchSets().size();
        if (diff < 0) {
            return PATCH_SET_GROUP_TYPE;
        } else if (diff == 0) {
            return MESSAGE_GROUP_HEADER;
        } else {
            return MESSAGE_GROUP_TYPE;
        }
    }
}
