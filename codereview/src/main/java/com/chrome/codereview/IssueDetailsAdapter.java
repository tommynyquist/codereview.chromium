package com.chrome.codereview;

import android.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.utils.HeadedExpandableListAdapter;
import com.chrome.codereview.utils.LinearExpandableAdapter;
import com.chrome.codereview.utils.MergeExpandableListAdapter;

import java.util.ArrayList;

/**
 * Created by sergeyv on 22/4/14.
 */
class IssueDetailsAdapter extends MergeExpandableListAdapter {

    private static final String BUG_PREFIX = "\nBUG=";
    private static final String CRBUG="https://crbug.com/";

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
            SpannableString spannableDescription = new SpannableString(description);
            Linkify.addLinks(spannableDescription, Linkify.WEB_URLS);
            linkifyBug(description, spannableDescription);
            descriptionView.setText(spannableDescription);
            return convertView;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        private void linkifyBug(String description, SpannableString spannableString) {
            int bugPosition = description.indexOf(BUG_PREFIX);
            if (bugPosition == -1) {
                return;
            }
            int linksStart = description.indexOf('=', bugPosition);
            int linksEnd = description.indexOf('\n', linksStart);
            if (linksEnd == -1) {
                linksEnd = description.length();
            }
            int start = -1;
            for (int i = linksStart; i < linksEnd; i++) {
                if (start == -1 && Character.isDigit(description.charAt(i))) {
                    start = i;
                    continue;
                }
                if (start != - 1 && !Character.isDigit(description.charAt(i))) {
                    String bugId = description.substring(start, i);
                    spannableString.setSpan(new URLSpan(CRBUG + bugId), start, i, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    start = - 1;
                }
            }

            if (start != - 1) {
                String bugId = description.substring(start, linksEnd);
                spannableString.setSpan(new URLSpan(CRBUG + bugId), start, description.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private PatchSetsAdapter patchSetsAdapter;
    private MessagesAdapter messagesAdapter;
    private DescriptionAdapter descriptionAdapter;

    public IssueDetailsAdapter(Fragment fragment) {
        super();
        descriptionAdapter = new DescriptionAdapter();
        add(new HeadedExpandableListAdapter(descriptionAdapter, R.layout.list_group_header, R.string.description));
        patchSetsAdapter = new PatchSetsAdapter(fragment.getActivity());
        add(new HeadedExpandableListAdapter(patchSetsAdapter, R.layout.list_group_header, R.string.patchsets));
        messagesAdapter = new MessagesAdapter(fragment);
        add(new HeadedExpandableListAdapter(messagesAdapter, R.layout.list_group_header, R.string.messages));
    }

    void setIssue(Issue issue) {
        descriptionAdapter.setDescription(issue != null ? issue.description() : "");
        messagesAdapter.setIssue(issue);
        patchSetsAdapter.setPatchsets(issue != null ? issue.patchSets() : new ArrayList<PatchSet>());
        notifyDataSetChanged();
    }

}
