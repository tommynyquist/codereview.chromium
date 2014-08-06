package com.chrome.codereview;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.Reviewer;
import com.chrome.codereview.model.UserIssues;
import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 20/4/14.
 */
class UserIssuesAdapter extends BaseAdapter {

    private static final int TYPE_ISSUE = 0;
    private static final int TYPE_GROUP_HEADER = 1;

    private static class Box {

        Issue issue;
        int titleResource;

        private Box(int titleResource) {
            this.titleResource = titleResource;
        }

        private Box(Issue issue) {
            this.issue = issue;
        }

        boolean isBoxIssue() {
            return issue != null;
        }

    }

    private List<Box> boxes = new ArrayList<Box>();

    private final LayoutInflater inflater;
    private final Context context;

    public UserIssuesAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return boxes.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Issue getItem(int position) {
        Box box = boxes.get(position);
        return box.isBoxIssue() ? box.issue : null;
    }

    @Override
    public long getItemId(int position) {
        Box box = boxes.get(position);
        return box.isBoxIssue() ? box.issue.id() : -Math.abs(box.titleResource);
    }

    public void setUserIssues(UserIssues userIssues) {
        boxes.clear();
        if (userIssues == null) {
            notifyDataSetChanged();
            return;
        }
        addGroup(R.string.header_incoming_reviews, userIssues.incomingReviews());
        addGroup(R.string.header_outgoing_reviews, userIssues.outgoingReviews());
        addGroup(R.string.header_cced_reviews, userIssues.ccReviews());
        addGroup(R.string.header_recently_closed, userIssues.recentlyClosed());
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return boxes.get(position).isBoxIssue() ? TYPE_ISSUE : TYPE_GROUP_HEADER;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_GROUP_HEADER) {
            return getGroupHeaderView(boxes.get(position).titleResource, convertView, parent);
        }

        return getIssueView(position, convertView, parent);
    }

    private View getIssueView(int position, View convertView, ViewGroup parent) {
        Issue issue = boxes.get(position).issue;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.issue_item, parent, false);
        }
        ViewUtils.setText(convertView, R.id.subject, issue.subject());
        ViewUtils.setText(convertView, R.id.owner, issue.owner());
        ViewUtils.setText(convertView, R.id.issue_id, issue.id() + " by ");
        TextView reviewers = (TextView) convertView.findViewById(R.id.reviewers);
        reviewers.setText(reviewersSpannable(issue.reviewers()), TextView.BufferType.SPANNABLE);
        ViewUtils.setText(convertView, R.id.modified, DateUtils.createAgoText(context, issue.lastModified()));

        return convertView;
    }

    private View getGroupHeaderView(int titleRes, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_group_header, parent, false);
        }
        TextView titleView = (TextView) convertView.findViewById(android.R.id.text1);
        titleView.setText(titleRes);
        return convertView;
    }

    private void addGroup(int titleRes, List<Issue> issues) {
        if (issues.isEmpty()) {
            return;
        }
        boxes.add(new Box(titleRes));
        for (Issue issue : issues) {
            boolean contained = false;
            for (Box box : boxes) {
                if (box.isBoxIssue() && box.issue.id() == issue.id()) {
                    contained = true;
                    continue;
                }
            }
            if (!contained) {
                boxes.add(new Box(issue));
            }
        }
    }

    public Spannable reviewersSpannable(List<Reviewer> reviewers) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String reviewersPrefix = context.getString(R.string.reviewers);
        builder.append(reviewersPrefix + " ");
        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        boolean firstReviewer = true;
        for (Reviewer reviewer : reviewers) {
            if (!firstReviewer) {
                builder.append(", ");
            }
            int start = builder.length();
            int end = builder.length() + reviewer.name().length();
            builder.append(reviewer.name());
            if (reviewer.decoration() != null) {
                builder.setSpan(new ForegroundColorSpan(reviewer.decoration().color(context)), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            firstReviewer = false;
        }
        return builder;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
