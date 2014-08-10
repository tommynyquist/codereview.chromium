package com.chrome.codereview.issuelists;

import android.content.Context;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.requests.SearchOptions;
import com.chrome.codereview.requests.ServerCaller;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by sergeyv on 10/8/14.
 */
public class RecentlyClosedIssuesFragment extends BaseIssueListFragment{

    @Override
    public Callable<List<Issue>> getLoadAction() {
        ServerCaller serverCaller = ServerCaller.from(getActivity());
        String accountName = serverCaller.getAccountName();
        SearchOptions searchOptions = new SearchOptions.Builder().owner(accountName).closeState(SearchOptions.CloseState.CLOSED).withMessages().create();
        return serverCaller.createSearchCallable(searchOptions);
    }

    private static class ClosedAdapter extends IssuesAdapter {

        public ClosedAdapter(Context context) {
            super(context);
        }

        @Override
        public boolean isItemSwipable(int position) {
            return false;
        }
    }

    @Override
    public IssuesAdapter getIssuesAdapter() {
        return new ClosedAdapter(getActivity());
    }
}
