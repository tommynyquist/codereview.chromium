package com.chrome.codereview.issuelists;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
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
public abstract class BaseIssueListFragment extends BaseListFragment implements SwipeListView.OnSwipeListener {

    private static final int ISSUES_LOADER = 0;
    private static final int CURSOR_LOADER = 1;

    public interface IssueSelectionListener {
        void onIssueSelected(int issueId, boolean force);
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
            return issues;
        }

    }

    private IssuesAdapter issuesAdapter;
    private IssueSelectionListener selectionListener;
    private boolean selectFirstIssue = false;
    private List<Issue> issues;
    private SparseArray<Long> idToModificationTime;
    private boolean wasInited;
    private int selectedPosition;
    private int selectedIssuedId;

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cursorLoader = new CursorLoader(getActivity());
            cursorLoader.setUri(IssueStateProvider.HIDDEN_ISSUES_URI);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            initIdToModificationTimeMap(data);
            resetAdapter();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<List<Issue>> issuesLoadedCallback = new LoaderManager.LoaderCallbacks<List<Issue>>() {

        @Override
        public Loader<List<Issue>> onCreateLoader(int i, Bundle bundle) {
            startProgress();
            return new IssuesLoader(getActivity(), getLoadAction());
        }

        @Override
        public void onLoaderReset(Loader<List<Issue>> listLoader) {
        }

        @Override
        public void onLoadFinished(Loader<List<Issue>> listLoader, List<Issue> issues) {
            stopProgress();
            wasInited = true;
            BaseIssueListFragment.this.issues = issues;
            resetAdapter();
        }

    };

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        selectIssue(position, true);
    }

    public abstract Callable<List<Issue>> getLoadAction();

    public IssuesAdapter getIssuesAdapter() {
        return new IssuesAdapter(getActivity());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_user_issues;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        boolean refresh = false;
        if (!wasInited) {
            refresh = true;
            issuesAdapter = getIssuesAdapter();
        }
        getLoaderManager().restartLoader(CURSOR_LOADER, new Bundle(), cursorLoaderCallbacks);
        setListAdapter(issuesAdapter);
        View layout = super.onCreateView(inflater, container, savedInstanceState, refresh);
        SwipeListView listView = (SwipeListView) layout.findViewById(android.R.id.list);
        listView.setSwipeListener(this);
        BackgroundContainer backgroundContainer = (BackgroundContainer) layout.findViewById(R.id.ptr_layout);
        listView.setBackgroundToggle(backgroundContainer);
        return layout;
    }

    @Override
    protected void refresh() {
        getLoaderManager().restartLoader(ISSUES_LOADER, new Bundle(), issuesLoadedCallback);
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
        swipeIssue(issue, direction);
        if (issue.id() != selectedIssuedId) {
            return;
        }
        int count = issuesAdapter.getCount();
        if (count == 0) {
            if (selectionListener != null) {
                selectionListener.onIssueSelected(-1, false);
            }
            return;
        }
        int newPosition = selectedPosition < count ? selectedPosition : count - 1;
        selectIssue(newPosition, false);
    }

    public void swipeIssue(Issue issue, int direction) {
        long modificationTime = direction == SwipeListView.DIRECTION_RIGHT ? Long.MAX_VALUE : issue.lastModified().getTime();
        //This is a temporary value, serverCaller will trigger db update, and thus CursorLoader will requery new data, so this valus will be written
        idToModificationTime.put(issue.id(), Long.MAX_VALUE);
        ServerCaller.from(getActivity()).updateIssueState(issue, modificationTime);
    }

    private void initIdToModificationTimeMap(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        if (idToModificationTime == null) {
            idToModificationTime = new SparseArray<Long>();
        }
        int columnId = cursor.getColumnIndex(IssueStateProvider.COLUMN_ISSUE_ID);
        int columnModification = cursor.getColumnIndex(IssueStateProvider.COLUMN_MODIFICATION_TIME);
        while (cursor.moveToNext()) {
            int issueId = cursor.getInt(columnId);
            long lastModification = cursor.getLong(columnModification);
            idToModificationTime.put(issueId, lastModification);
        }
    }

    private void resetAdapter() {
        if (issues == null || idToModificationTime == null) {
            return;
        }
        issuesAdapter.setIssues(filter(issues, idToModificationTime));
        if (!selectFirstIssue || issuesAdapter.getCount() == 0) {
            return;
        }
        selectFirstIssue = false;
        selectIssue(0, false);
    }

    private void selectIssue(int position, boolean force) {
        if (position >= issuesAdapter.getCount()) {
            return;
        }
        if (selectionListener != null) {
            selectedPosition = position;
            Issue issue = issuesAdapter.getItem(position);
            selectedIssuedId = issue.id();
            selectionListener.onIssueSelected(issue.id(), force);
        }
    }

    protected List<Issue> filter(List<Issue> list, SparseArray<Long> idToModificationTime) {
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
