package com.chrome.codereview;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.Message;
import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.utils.HeadedExpandableListAdapter;
import com.chrome.codereview.utils.LinearExpandableAdapter;
import com.chrome.codereview.utils.MergeExpandableListAdapter;

import java.util.ArrayList;

/**
 * Created by sergeyv on 22/4/14.
 */
class IssueDetailsAdapter extends MergeExpandableListAdapter {

    private static class DescriptionAdapter extends LinearExpandableAdapter {

        private String description;

        @Override
        public int getGroupCount() {
            return TextUtils.isEmpty(description) ? 0 : 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.description_item, parent, false);
            }
            TextView descriptionView = (TextView) convertView.findViewById(R.id.description_text);
            descriptionView.setText(description);
            return convertView;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    private PatchSetsAdapter patchSetsAdapter;
    private MessagesAdapter messagesAdapter;
    private DescriptionAdapter descriptionAdapter;

    public IssueDetailsAdapter(Context context) {
        super();
        descriptionAdapter = new DescriptionAdapter();
        add(new HeadedExpandableListAdapter(descriptionAdapter, R.layout.list_group_header, R.string.description));
        patchSetsAdapter = new PatchSetsAdapter(context);
        add(new HeadedExpandableListAdapter(patchSetsAdapter, R.layout.list_group_header, R.string.patchsets));
        messagesAdapter = new MessagesAdapter(context);
        add(new HeadedExpandableListAdapter(messagesAdapter, R.layout.list_group_header, R.string.messages));
    }

    void setIssue(Issue issue) {
        descriptionAdapter.setDescription(issue != null ? issue.description() : "");
        messagesAdapter.setMessages(issue != null ? issue.messages() : new ArrayList<Message>());
        patchSetsAdapter.setPatchsets(issue != null ? issue.patchSets() : new ArrayList<PatchSet>());
        notifyDataSetChanged();
    }

}
