package com.chrome.codereview.utils;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by sergeyv on 4/6/14.
 */
public abstract class BaseFragment extends Fragment {

    private SmoothProgressBar progress;

    protected abstract int getLayoutRes();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(getLayoutRes(), container, false);
        progress = (SmoothProgressBar) layout.findViewById(android.R.id.progress);
        return layout;
    }

    protected void startProgress() {
        progress.setIndeterminate(true);
        progress.progressiveStart();
    }

    protected void stopProgress() {
        progress.progressiveStop();
    }
}
