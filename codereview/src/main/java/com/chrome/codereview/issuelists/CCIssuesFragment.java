package com.chrome.codereview.issuelists;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.requests.SearchOptions;
import com.chrome.codereview.requests.ServerCaller;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by sergeyv on 10/8/14.
 */
public class CCIssuesFragment extends BaseIssueListFragment{

    @Override
    public Callable<List<Issue>> getLoadAction() {
        ServerCaller serverCaller = ServerCaller.from(getActivity());
        String accountName = serverCaller.getAccountName();
        SearchOptions searchOptions = new SearchOptions.Builder().cc(accountName).closeState(SearchOptions.CloseState.OPEN).withMessages().create();
        return serverCaller.createSearchCallable(searchOptions);
    }

}
