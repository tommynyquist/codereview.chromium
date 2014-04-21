package com.chrome.codereview;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.CachedLoader;

/**
 * Created by sergeyv on 18/4/14.
 */
public class IssueFragment extends Fragment implements LoaderManager.LoaderCallbacks<Issue> {

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

    private int issueId;
    private IssueAdapter issueAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        issueId = getActivity().getIntent().getIntExtra(EXTRA_ISSUE_ID, -1);
        if (issueId == -1) {
            throw new IllegalStateException("EXTRA_ISSUE_ID wasn't found in intent");
        }
        issueAdapter = new IssueAdapter(getActivity());
        getLoaderManager().initLoader(0, new Bundle(), this);
        View view =  inflater.inflate(android.R.layout.expandable_list_content, container);
        ExpandableListView listView = (ExpandableListView) view.findViewById(android.R.id.list);
        listView.setAdapter(issueAdapter);
        return view;
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
