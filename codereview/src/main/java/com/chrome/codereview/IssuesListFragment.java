package com.chrome.codereview;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.requests.ServerCaller;

import java.util.List;

/**
 * Created by sergeyv on 13/4/14.
 */
public class IssuesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Issue>> {

    private IssuesAdapter issuesAdapter;

    @Override
    public Loader<List<Issue>> onCreateLoader(int i, Bundle bundle) {
        return new IssuesLoader(this.getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Issue>> listLoader, List<Issue> issues) {
        issuesAdapter.setIssues(issues);
    }

    @Override
    public void onLoaderReset(Loader<List<Issue>> listLoader) {

    }

    private static class IssuesLoader extends AsyncTaskLoader<List<Issue>> {

        public IssuesLoader(Context context) {
            super(context);
        }

        @Override
        public List<Issue> loadInBackground() {
            return ServerCaller.from(getContext()).loadMineIssues();
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }
    }

    private class IssuesAdapter extends ArrayAdapter<Issue> {

        private final LayoutInflater inflater;

        public IssuesAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
            inflater = LayoutInflater.from(context);
        }

        void setIssues(List<Issue> issues) {
            clear();
            if (issues != null) {
                addAll(issues);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            TextView subjectTextView = (TextView) convertView.findViewById(android.R.id.text1);
            TextView ownerTextView = (TextView) convertView.findViewById(android.R.id.text2);
            subjectTextView.setText(getItem(position).getSubject());
            ownerTextView.setText(getItem(position).getOwner());
            return convertView;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        issuesAdapter = new IssuesAdapter(getActivity());
        setListAdapter(issuesAdapter);
        getLoaderManager().initLoader(0, new Bundle(), this);
        getLoaderManager().getLoader(0).startLoading();
    }
}
