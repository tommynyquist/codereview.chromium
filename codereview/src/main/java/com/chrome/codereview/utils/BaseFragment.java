package com.chrome.codereview.utils;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chrome.codereview.R;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by sergeyv on 4/6/14.
 */
public abstract class BaseFragment extends Fragment {

    private PullToRefreshLayout pullToRefreshLayout;

    protected abstract int getLayoutRes();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(getLayoutRes(), container, false);
        pullToRefreshLayout = (PullToRefreshLayout) layout.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.SetupWizard setupWizard = ActionBarPullToRefresh.from(getActivity());
        setupWizard.allChildrenArePullable();
        setupWizard.listener(new OnRefreshListener() {
            @Override
            public void onRefreshStarted(View view) {
                refresh();
            }
        });

        setupWizard.setup(pullToRefreshLayout);
        setupWizard.options(Options.create().scrollDistance(0.75f).build());
        refresh();
        return layout;
    }

    protected abstract void refresh();

    protected void startProgress() {
        pullToRefreshLayout.setRefreshing(true);
    }

    protected void stopProgress() {
        pullToRefreshLayout.setRefreshComplete();
    }
}
