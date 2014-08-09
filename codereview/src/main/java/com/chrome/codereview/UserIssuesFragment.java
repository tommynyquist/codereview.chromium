package com.chrome.codereview;

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

import com.chrome.codereview.data.IssueStateProvider;
import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.UserIssues;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.BaseListFragment;
import com.chrome.codereview.utils.CachedLoader;
import com.chrome.codereview.utils.SwipeListView;


/**
 * Created by sergeyv on 13/4/14.
 */
public class UserIssuesFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<UserIssues>, SwipeListView.OnSwipeListener {

    public interface IssueSelectionListener {
        void onIssueSelected(Issue issue);
    }

    private static class IssuesLoader extends CachedLoader<UserIssues> {

        private final String userName;

        public IssuesLoader(Context context, String userName) {
            super(context);
            this.userName = userName;
        }

        @Override
        public UserIssues loadInBackground() {
            UserIssues userIssues = ServerCaller.from(getContext()).loadIssuesForUser(userName);
            Cursor cursor = getContext().getContentResolver().query(IssueStateProvider.HIDDEN_ISSUES_URI, null, null, null, null);
            SparseArray<Long> idToModificationTime  = new SparseArray<Long>();
            int columnId = cursor.getColumnIndex(IssueStateProvider.COLUMN_ISSUE_ID);
            int columnModification = cursor.getColumnIndex(IssueStateProvider.COLUMN_MODIFICATION_TIME);
            while (cursor.moveToNext()) {
                int issueId = cursor.getInt(columnId);
                long lastModification = cursor.getLong(columnModification);
                idToModificationTime.put(issueId, lastModification);
            }
            cursor.close();
            userIssues.filter(idToModificationTime);
            return userIssues;
        }
    }

    private UserIssuesAdapter issuesAdapter;
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
    public Loader<UserIssues> onCreateLoader(int i, Bundle bundle) {
        startProgress();
        return new IssuesLoader(this.getActivity(), ServerCaller.from(getActivity()).getAccountName());
    }

    @Override
    public void onLoadFinished(Loader<UserIssues> listLoader, UserIssues issues) {
        issuesAdapter.setUserIssues(issues);
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

    @Override
    public void onLoaderReset(Loader<UserIssues> listLoader) {
        issuesAdapter.setUserIssues(null);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_user_issues;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        issuesAdapter = new UserIssuesAdapter(getActivity());
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        SwipeListView listView = (SwipeListView) layout.findViewById(android.R.id.list);
        listView.setSwipeListener(this);
        BackgroundContainer mBackgroundContainer = (BackgroundContainer) layout.findViewById(R.id.ptr_layout);
        listView.setBackgroundToggle(mBackgroundContainer);
        return layout;
    }

    @Override
    protected void refresh() {
        getLoaderManager().restartLoader(0, new Bundle(), UserIssuesFragment.this);
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
        values.put(IssueStateProvider.COLUMN_MODIFICATION_TIME, direction == SwipeListView.DIRECTION_RIGHT ? Long.MAX_VALUE : issue.lastModified().getTime() );
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.insert(IssueStateProvider.HIDDEN_ISSUES_URI, values);
    }


}
