package com.chrome.codereview;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
class IssueDetailsAdapter extends BaseExpandableListAdapter {

    private static final int PATCH_SET_GROUP_TYPE = 0;
    private static final int MESSAGE_GROUP_TYPE = 1;
    private static final int GROUP_HEADER = 2;

    private Issue issue;
    private Context context;
    private LayoutInflater inflater;

    public IssueDetailsAdapter(Context context) {
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

    public View getPatchSetView(int position, View convertView, ViewGroup parent, boolean isExpanded) {
        PatchSet patchSet = (PatchSet) getGroup(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.patchset_item, parent, false);
        }
        String patchSetText = context.getString(R.string.patchset, position) + patchSet.message();
        ViewUtils.setText(convertView, R.id.patchset_message, patchSetText);
        fillStatsView(convertView, patchSet.linesAdded(), patchSet.linesRemoved(), patchSet.numComments());
        convertView.findViewById(R.id.list_divider).setVisibility(!isExpanded && position == issue.patchSets().size() ? View.INVISIBLE : View.VISIBLE);
        return convertView;
    }

    public View getGroupHeader(View convertView, ViewGroup parent, int position) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_group_header, parent, false);
        }
        int stringRes = position == 0 ? R.string.patchsets : R.string.messages;
        ViewUtils.setText(convertView, android.R.id.text1, context.getString(stringRes));
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
        return issue.patchSets().size() + issue.messages().size() + 2;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        switch (getGroupType(groupPosition)) {
            case PATCH_SET_GROUP_TYPE:
                return ((PatchSet) getGroup(groupPosition)).files().size();
            case GROUP_HEADER:
            case MESSAGE_GROUP_TYPE:
                return 0;
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (groupPosition <= issue.patchSets().size()) {
            return issue.patchSets().get(groupPosition - 1);
        }
        return issue.messages().get(groupPosition - issue.patchSets().size() - 2);
    }

    @Override
    public PatchSetFile getChild(int groupPosition, int childPosition) {
        return issue.patchSets().get(groupPosition - 1).files().get(childPosition);
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
        View result = null;
        switch (getGroupType(groupPosition)) {
            case PATCH_SET_GROUP_TYPE:
                result = getPatchSetView(groupPosition, convertView, parent, isExpanded);
                break;
            case MESSAGE_GROUP_TYPE:
                result = getMessageView((Message) getGroup(groupPosition), convertView, parent, isExpanded);
                break;
            case GROUP_HEADER:
                result = getGroupHeader(convertView, parent, groupPosition);
                break;
        }
        if (result == null) {
            throw new IllegalStateException("Unknown type " + getGroupType(groupPosition));
        }

        ImageView imageView = (ImageView) result.findViewById(R.id.expander);
        if (imageView == null) {
            return result;
        }
        if (isExpanded) {
            imageView.getDrawable().setState(new int[] {android.R.attr.state_expanded});
        } else {
            imageView.getDrawable().setState(new int[] {});
        }

        return result;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (getGroupType(groupPosition) == MESSAGE_GROUP_TYPE) {
            throw new IllegalStateException("No child for message type");
        }
        PatchSetFile patchSetFile = getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.patchset_file_item, parent, false);
        }
        ViewUtils.setText(convertView, R.id.file_name, patchSetFile.path());
        fillFileStatusView((TextView) convertView.findViewById(R.id.file_status), patchSetFile.status());
        fillStatsView(convertView, patchSetFile.numAdded(), patchSetFile.numRemoved(), patchSetFile.numberOfComments());
        return convertView;
    }


    private void fillFileStatusView(TextView view, PatchSetFile.Status status) {
        int color = 0;
        int text = 0;
        switch (status) {
            case MODIFIED:
                color = android.R.color.holo_blue_dark;
                text = R.string.file_status_modified;
                break;
            case ADDED:
                color = android.R.color.holo_green_dark;
                text = R.string.file_status_added;
                break;
            case DELETED:
                color = android.R.color.holo_red_dark;
                text = R.string.file_status_deleted;
                break;
        }
        view.setTextColor(context.getResources().getColor(color));
        view.setText(text);
    }

    public void fillStatsView(View convertView, int numAdded, int numRemoved, int numComments) {
        ForegroundColorSpan greenSpan = new ForegroundColorSpan(context.getResources().getColor(android.R.color.holo_green_dark));
        ForegroundColorSpan redSpan = new ForegroundColorSpan(context.getResources().getColor(android.R.color.holo_red_dark));
        SpannableStringBuilder builder = new SpannableStringBuilder();
        CharSequence linesAdded = context.getString(R.string.lines_added, numAdded);
        CharSequence linesRemoved = context.getString(R.string.lines_removed, numRemoved);
        builder.append(linesAdded);
        builder.setSpan(greenSpan, 0, linesAdded.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(", ");
        builder.append(linesRemoved);
        builder.setSpan(redSpan, linesAdded.length() + 2, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ViewUtils.setText(convertView, R.id.diff_stats, builder);
        ViewUtils.setText(convertView, R.id.comments_number, context.getString(R.string.comments_number, numComments));
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
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
        if (groupPosition == 0) {
            return GROUP_HEADER;
        }
        int diff = groupPosition - issue.patchSets().size() - 1;
        if (diff < 0) {
            return PATCH_SET_GROUP_TYPE;
        } else if (diff == 0) {
            return GROUP_HEADER;
        } else {
            return MESSAGE_GROUP_TYPE;
        }
    }
}
