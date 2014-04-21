package com.chrome.codereview;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.Message;
import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.CachedLoader;
import com.chrome.codereview.utils.ViewUtils;

/**
 * Created by sergeyv on 18/4/14.
 */
public class IssueFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Issue> {

    public static final String EXTRA_ISSUE_ID = "EXTRA_ISSUE_ID";

    private static class IssueLoader extends CachedLoader<Issue> {

        private int issueId;

        public IssueLoader(Context context, int issueId) {
            super(context);
            this.issueId = issueId;
        }

        @Override
        public Issue loadInBackground() {
            return ServerCaller.from(getContext()).loadIssueWithPatchSetData(issueId);
        }
    }

    private static class IssueAdapter extends BaseAdapter {

        private Issue issue;
        private LayoutInflater inflater;

        public IssueAdapter(Context context) {
            super();
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position < issue.patchSets().size() ? 0 : 1;
        }

        @Override
        public int getCount() {
            if (issue == null) {
                return 0;
            }
            return issue.patchSets().size() + issue.messages().size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position < issue.patchSets().size()) {
                return getPatchSetView(issue.patchSets().get(position), convertView, parent);
            }
            return getMessageView(issue.messages().get(position - issue.patchSets().size()), convertView, parent);
        }

        public View getMessageView(Message message, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            ViewUtils.setText(convertView, android.R.id.text1, message.getText());
            ViewUtils.setText(convertView, android.R.id.text2, message.getSender());
            return convertView;
        }

        public View getPatchSetView(PatchSet patchSet, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            ViewUtils.setText(convertView, android.R.id.text1, patchSet.message());
            return convertView;
        }

        void setIssue(Issue issue) {
            this.issue = issue;
            notifyDataSetChanged();
        }
    }

    private int issueId;
    private IssueAdapter issueAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        issueId = getActivity().getIntent().getIntExtra(EXTRA_ISSUE_ID, -1);
        if (issueId == -1) {
            throw new IllegalStateException("EXTRA_ISSUE_ID wasn't found in intent");
        }
        issueAdapter = new IssueAdapter(getActivity());
        setListAdapter(issueAdapter);
        getLoaderManager().initLoader(0, new Bundle(), this);
        return inflater.inflate(R.layout.fragment_issue_detail, container);
    }

    @Override
    public Loader<Issue> onCreateLoader(int i, Bundle bundle) {
        return new IssueLoader(getActivity(), issueId);
    }

    @Override
    public void onLoadFinished(Loader<Issue> issueLoader, Issue issue) {
        issueAdapter.setIssue(issue);
    }

    @Override
    public void onLoaderReset(Loader<Issue> issueLoader) {
        issueAdapter.setIssue(null);
    }
}
