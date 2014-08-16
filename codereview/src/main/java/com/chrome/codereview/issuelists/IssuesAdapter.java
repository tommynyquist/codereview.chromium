package com.chrome.codereview.issuelists;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chrome.codereview.R;
import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.Reviewer;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.SwipeListAdapter;
import com.chrome.codereview.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 10/8/14.
 */
public class IssuesAdapter extends SwipeListAdapter {

    private List<Issue> issues = new ArrayList<Issue>();
    private LayoutInflater inflater;
    private Context context;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int issueId = intent.getIntExtra(ServerCaller.EXTRA_ISSUE_ID, -1);
            long time = intent.getLongExtra(ServerCaller.EXTRA_MODIFICATION_TIME, 0l);
            for (Issue issue: issues) {
                if (issue.id() == issueId) {
                    issue.setLastModified(time);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    };

    public IssuesAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ServerCaller.ACTION_UPDATE_ISSUE_MODIFICATION_TIME);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    @Override
    public boolean isItemSwipable(int position) {
        return true;
    }

    @Override
    public void remove(int position) {
        issues.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return issues.size();
    }

    @Override
    public Issue getItem(int position) {
        return issues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Issue issue = getItem(position);
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

    @Override
    public boolean hasStableIds() {
        return true;
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

    public void setIssues(List<Issue> issues) {
        if (issues == null) {
            this.issues.clear();
        } else {
            this.issues = issues;
        }
        notifyDataSetChanged();
    }
}
