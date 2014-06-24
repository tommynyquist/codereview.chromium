package com.chrome.codereview;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.Message;
import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.PatchSetFile;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.LinearExpandableAdapter;
import com.chrome.codereview.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by sergeyv on 29/5/14.
 */
public class MessagesAdapter extends LinearExpandableAdapter {

    private class DiffUrlSpan extends URLSpan {

        public DiffUrlSpan(String url) {
            super(url);
        }

        @Override
        public void onClick(View widget) {
            if (issue == null) {
                return;
            }
            String url = getURL();
            String rest = "";
            String prefix1 = ServerCaller.BASE_URL.buildUpon().appendPath(issue.id() + "").appendPath("diff").toString();
            String prefix2 = ServerCaller.SECONDARY_URL.buildUpon().appendPath(issue.id() + "").appendPath("diff").toString();
            if (url.startsWith(prefix1)) {
                rest = url.substring(prefix1.length() + 1);
            } else if (url.startsWith(prefix2)) {
                rest = url.substring(prefix2.length() + 1);
            } else {
                safeSuperOnClick(widget);
                return;
            }

            PatchSet foundPatchSet = null;
            for (PatchSet patchSet : issue.patchSets()) {
                if (rest.startsWith(patchSet.id() + "")) {
                    foundPatchSet = patchSet;
                    break;
                }
            }
            if (foundPatchSet == null) {
                safeSuperOnClick(widget);
                return;
            }
            rest = rest.substring((foundPatchSet.id() + "").length() + 1);
            for (PatchSetFile file : foundPatchSet.files()) {
                if (rest.startsWith(file.path())) {
                    DiffActivity.startDiffActivity(fragment, IssueDetailsFragment.REQUEST_CODE_DIFF, issue.id(), foundPatchSet.id(), file);
                    return;
                }
            }
            safeSuperOnClick(widget);
        }

        private void safeSuperOnClick(View widget) {
            try {
                super.onClick(widget);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private LayoutInflater inflater;
    private Fragment fragment;
    private Issue issue;
    private Activity activity;
    private List<Message> messages = new ArrayList<Message>();

    public MessagesAdapter(Fragment fragment) {
        this.fragment = fragment;
        this.activity = fragment.getActivity();
        inflater = LayoutInflater.from(activity);
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
        messages = issue != null ? issue.messages() : new ArrayList<Message>();
    }

    @Override
    public int getGroupCount() {
        return messages.size();
    }

    @Override
    public Message getGroup(int groupPosition) {
        return messages.get(groupPosition);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.message_item, parent, false);
        }
        Message message = getGroup(groupPosition);
        TextView messageView = (TextView) convertView.findViewById(R.id.message_text);
        messageView.setSingleLine(!isExpanded);
        if (message.text().isEmpty()) {
            messageView.setText(R.string.empty_message);
            messageView.setTypeface(null, Typeface.ITALIC);
        } else {
            messageView.setText(prepareText(message.text(), isExpanded));
            messageView.setTypeface(null, Typeface.NORMAL);
        }
        ViewUtils.setText(convertView, R.id.sender, message.sender());
        ViewUtils.setText(convertView, R.id.date, DateUtils.createAgoText(activity, message.date()));
        View stateView = convertView.findViewById(R.id.state);
        if (message.decoration() != null) {
            stateView.setVisibility(View.VISIBLE);
            stateView.setBackgroundColor(message.decoration().color(activity));
        } else {
            stateView.setVisibility(View.INVISIBLE);
        }
        ViewUtils.expandView(convertView, isExpanded);
        return convertView;
    }

    private SpannableString prepareText(String message, boolean isExpanded) {
        String text = isExpanded ? message : message.substring(0, Math.min(100, message.length()));
        SpannableString spannableString = new SpannableString(text);
        Matcher m = Patterns.WEB_URL.matcher(message);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (start >= spannableString.length()) {
                break;
            }
            DiffUrlSpan span = new DiffUrlSpan(m.group(0));
            spannableString.setSpan(span, start, Math.min(end, spannableString.length()), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

}
