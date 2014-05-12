package com.chrome.codereview;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.chrome.codereview.model.Comment;
import com.chrome.codereview.model.FileDiff;
import com.chrome.codereview.utils.CachedLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 29/4/14.
 */
public class DiffFragment extends ListFragment implements LoaderManager.LoaderCallbacks<FileDiff> {

    public static final String COMMENTS_EXTRA = "COMMENTS_EXTRA";
    public static final String ISSUE_ID_EXTRA = "ISSUE_ID_EXTRA";
    public static final String PATCH_SET_ID_EXTRA = "PATCH_SET_ID_EXTRA";
    public static final String PATCH_ID_EXTRA = "PATCH_ID_EXTRA";

    private static class DiffLoader extends CachedLoader<FileDiff> {

        private final int issueId;
        private final int patchSetId;
        private final int patchId;

        public DiffLoader(Context context, int issueId, int patchSetId, int patchId) {
            super(context);
            this.issueId = issueId;
            this.patchSetId = patchSetId;
            this.patchId = patchId;
        }

        @Override
        public FileDiff loadInBackground() {
            try {
                return serverCaller().loadDiff(issueId, patchSetId, patchId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class DiffAdapter extends ArrayAdapter<String>{

        public DiffAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            String line = getItem(position);
            v.setBackgroundColor(getContext().getResources().getColor(R.color.diff_default_background));
            if (line.startsWith("+")) {
                v.setBackgroundColor(getContext().getResources().getColor(R.color.diff_add));
            }
            if (line.startsWith("-")) {
                v.setBackgroundColor(getContext().getResources().getColor(R.color.diff_remove));
            }
            if (line.startsWith("@")) {
                v.setBackgroundColor(getContext().getResources().getColor(R.color.diff_marker_bg));
            }
            return v;
        }
    }

    private int issueId;
    private int patchSetId;
    private int patchId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        ArrayList<Comment> comments = intent.getParcelableArrayListExtra(COMMENTS_EXTRA);
        issueId = intent.getIntExtra(ISSUE_ID_EXTRA, -1);
        patchSetId = intent.getIntExtra(PATCH_SET_ID_EXTRA, -1);
        patchId = intent.getIntExtra(PATCH_ID_EXTRA, -1);
        getLoaderManager().initLoader(0, new Bundle(), this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Loader<FileDiff> onCreateLoader(int id, Bundle args) {
        return new DiffLoader(getActivity(), issueId, patchSetId, patchId);
    }

    @Override
    public void onLoadFinished(Loader<FileDiff> loader, FileDiff data) {
        if (data == null) {
            return;
        }
        setListAdapter(new DiffAdapter(getActivity(), R.layout.diff_line, data.content()));
    }

    @Override
    public void onLoaderReset(Loader<FileDiff> loader) {
    }

}
