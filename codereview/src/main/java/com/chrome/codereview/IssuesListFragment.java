package com.chrome.codereview;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.phone.IssueDetailActivity;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.BaseArrayAdapter;
import com.chrome.codereview.utils.CachedLoader;

import java.util.List;

/**
 * Created by sergeyv on 13/4/14.
 */
public class IssuesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Issue>> {

    private static class IssuesLoader extends CachedLoader<List<Issue>> {

        public IssuesLoader(Context context) {
            super(context);
        }

        @Override
        public List<Issue> loadInBackground() {
            return ServerCaller.from(getContext()).loadMineIssues();
        }
    }

    private static class IssuesAdapter extends BaseArrayAdapter<Issue> {

        public IssuesAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
        }

        @Override
        public void fillView(View view, Issue issue) {
            TextView subjectTextView = (TextView) view.findViewById(android.R.id.text1);
            TextView ownerTextView = (TextView) view.findViewById(android.R.id.text2);
            subjectTextView.setText(issue.subject());
            ownerTextView.setText(issue.owner());
        }

    }

    private IssuesAdapter issuesAdapter;

    @Override
    public Loader<List<Issue>> onCreateLoader(int i, Bundle bundle) {
        return new IssuesLoader(this.getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Issue>> listLoader, List<Issue> issues) {
        issuesAdapter.setData(issues);
    }

    @Override
    public void onLoaderReset(Loader<List<Issue>> listLoader) {
        issuesAdapter.setData(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        issuesAdapter = new IssuesAdapter(getActivity());
        setListAdapter(issuesAdapter);
        getLoaderManager().initLoader(0, new Bundle(), this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Issue issue = issuesAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), IssueDetailActivity.class);
        intent.putExtra(IssueFragment.EXTRA_ISSUE_ID, issue.id());
        startActivity(intent);
    }
}
