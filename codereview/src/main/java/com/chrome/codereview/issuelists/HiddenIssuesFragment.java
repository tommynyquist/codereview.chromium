package com.chrome.codereview.issuelists;

import android.util.SparseArray;

import com.chrome.codereview.R;
import com.chrome.codereview.model.Issue;
import com.chrome.codereview.requests.ServerCaller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by sergeyv on 10/8/14.
 */
public class HiddenIssuesFragment extends BaseIssueListFragment {

    @Override
    public Callable<List<Issue>> getLoadAction() {
        final ServerCaller serverCaller = ServerCaller.from(getActivity());
        final String accountName = serverCaller.getAccountName();
        final Callable<List<Issue>> callable = new Callable<List<Issue>>() {
            @Override
            public List<Issue> call() throws Exception {
                return serverCaller.loadIssuesForUser(accountName);
            }
        };
        return callable;
    }

    @Override
    protected List<Issue> filter(List<Issue> list, SparseArray<Long> idToModificationTime) {
        List<Issue> result = new ArrayList<Issue>();
        for (Issue issue : list) {
            long lastSavedModification = idToModificationTime.get(issue.id(), 1l);
            if (issue.lastModified().getTime() <= lastSavedModification) {
                result.add(issue);
            }
        }
        return result;
    }

    @Override
    public void swipeIssue(Issue issue, int direction) {
        updateIssueState(issue, 0l);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.hidden_user_issues;
    }
}
