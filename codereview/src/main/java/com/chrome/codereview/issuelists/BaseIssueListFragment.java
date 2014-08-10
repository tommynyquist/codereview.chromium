package com.chrome.codereview.issuelists;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.chrome.codereview.BackgroundContainer;
import com.chrome.codereview.R;
import com.chrome.codereview.data.IssueStateProvider;
import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.UserIssues;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.BaseListFragment;
import com.chrome.codereview.utils.CachedLoader;
import com.chrome.codereview.utils.SwipeListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by sergeyv on 10/8/14.
 */
public abstract class BaseIssueListFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<List<Issue>>, SwipeListView.OnSwipeListener {


    public interface IssueSelectionListener {
        void onIssueSelected(Issue issue);
    }

    private static class IssuesLoader extends CachedLoader<List<Issue>> {


        private final Callable<List<Issue>> listCallable;

        public IssuesLoader(Context context, Callable<List<Issue>> listCallable) {
            super(context);
            this.listCallable = listCallable;
        }

        @Override
        public List<Issue> loadInBackground() {
            List<Issue> issues;
            try {
                issues = listCallable.call();
            } catch (Exception e) {
                issues = new ArrayList<Issue>();
                e.printStackTrace();
            }
            Cursor cursor = getContext().getContentResolver().query(IssueStateProvider.HIDDEN_ISSUES_URI, null, null, null, null);
            SparseArray<Long> idToModificationTime = new SparseArray<Long>();
            int columnId = cursor.getColumnIndex(IssueStateProvider.COLUMN_ISSUE_ID);
            int columnModification = cursor.getColumnIndex(IssueStateProvider.COLUMN_MODIFICATION_TIME);
            while (cursor.moveToNext()) {
                int issueId = cursor.getInt(columnId);
                long lastModification = cursor.getLong(columnModification);
                idToModificationTime.put(issueId, lastModification);
            }
            cursor.close();
            return filterList(issues, idToModificationTime);
        }


    }


    private IssuesAdapter issuesAdapter;
    private IssueSelectionListener selectionListener;
    private boolean selectFirstIssue = false;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Issue issue = issuesAdapter.getItem(position);
        if (issue != null && selectionListener != null) {
            selectionListener.onIssueSelected(issue);
        }
    }

    @Override
    public Loader<List<Issue>> onCreateLoader(int i, Bundle bundle) {
        startProgress();
        return new IssuesLoader(this.getActivity(), getLoadAction());
    }

    @Override
    public void onLoadFinished(Loader<List<Issue>> listLoader, List<Issue> issues) {
        issuesAdapter.setIssues(issues);
        stopProgress();
        setListAdapter(issuesAdapter);
        if (!selectFirstIssue) {
            return;
        }
        selectFirstIssue = false;
        for (int i = 0; i < issuesAdapter.getCount(); i++) {
            Issue issue = issuesAdapter.getItem(i);
            if (issue == null) {
                continue;
            }
            getListView().setSelection(i);
            if (selectionListener != null) {
                selectionListener.onIssueSelected(issue);
            }
            break;
        }
    }


    public abstract Callable<List<Issue>> getLoadAction();

    public IssuesAdapter getIssuesAdapter() {
        return new IssuesAdapter(getActivity());
    }

    @Override
    public void onLoaderReset(Loader<List<Issue>> listLoader) {
        issuesAdapter.setIssues(null);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_user_issues;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        issuesAdapter = getIssuesAdapter();
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        SwipeListView listView = (SwipeListView) layout.findViewById(android.R.id.list);
        listView.setSwipeListener(this);
        BackgroundContainer mBackgroundContainer = (BackgroundContainer) layout.findViewById(R.id.ptr_layout);
        listView.setBackgroundToggle(mBackgroundContainer);
        return layout;
    }

    @Override
    protected void refresh() {
        getLoaderManager().restartLoader(0, new Bundle(), BaseIssueListFragment.this);
    }

    public void selectFirstIssue() {
        this.selectFirstIssue = true;
    }

    public void setIssueSelectionListener(IssueSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    @Override
    public void onSwipe(Object item, int direction) {
        if (item == null) {
            return;
        }
        Issue issue = (Issue) item;
        ContentValues values = new ContentValues();
        values.put(IssueStateProvider.COLUMN_ISSUE_ID, issue.id());
        values.put(IssueStateProvider.COLUMN_MODIFICATION_TIME, direction == SwipeListView.DIRECTION_RIGHT ? Long.MAX_VALUE : issue.lastModified().getTime());
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.insert(IssueStateProvider.HIDDEN_ISSUES_URI, values);
    }

    private static List<Issue> filterList(List<Issue> list, SparseArray<Long> idToModificationTime) {
        List<Issue> result = new ArrayList<Issue>();
        for (Issue issue : list) {
            long lastSavedModification = idToModificationTime.get(issue.id(), 1l);
            if (issue.lastModified().getTime() > lastSavedModification) {
                result.add(issue);
            }
        }
        return result;
    }

}
