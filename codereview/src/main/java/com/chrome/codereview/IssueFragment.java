package com.chrome.codereview;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.PublishData;
import com.chrome.codereview.utils.CachedLoader;
import com.chrome.codereview.utils.ViewUtils;
import com.google.android.gms.auth.GoogleAuthException;

import org.apache.http.auth.AuthenticationException;

import java.io.IOException;

/**
 * Created by sergeyv on 18/4/14.
 */
public class IssueFragment extends Fragment implements DialogInterface.OnClickListener {

    public static final String EXTRA_ISSUE_ID = "EXTRA_ISSUE_ID";

    private static final int ISSUE_LOADER_ID = 0;
    private static final int PUBLISH_LOADER_ID = 1;

    private static class IssueLoader extends CachedLoader<Issue> {

        private int issueId;

        public IssueLoader(Context context, int issueId) {
            super(context);
            this.issueId = issueId;
        }

        @Override
        public Issue loadInBackground() {
            return serverCaller().loadIssueWithPatchSetData(issueId);
        }
    }

    private static class PublishLoaded extends CachedLoader<Void> {

        private PublishData publishData;

        public PublishLoaded(Context context, PublishData publishData) {
            super(context);
            this.publishData = publishData;
        }

        @Override
        public Void loadInBackground() {
            try {
                serverCaller().publish(publishData);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private LoaderManager.LoaderCallbacks<Issue> issueLoaderCallback = new LoaderManager.LoaderCallbacks<Issue>() {

        @Override
        public Loader<Issue> onCreateLoader(int id, Bundle args) {
            return new IssueLoader(getActivity(), issueId);
        }

        @Override
        public void onLoadFinished(Loader<Issue> loader, Issue issue) {
            IssueFragment.this.issue = issue;
            issueAdapter.setIssue(issue);
        }

        @Override
        public void onLoaderReset(Loader<Issue> loader) {
            issue = null;
            issueAdapter.setIssue(null);
        }
    };

    private LoaderManager.LoaderCallbacks<Void> publishLoaderCallback = new LoaderManager.LoaderCallbacks<Void>() {

        @Override
        public Loader<Void> onCreateLoader(int id, Bundle args) {
            return new PublishLoaded(getActivity(), lastPublishData);
        }

        @Override
        public void onLoadFinished(Loader<Void> loader, Void v) {
            lastPublishData = null;
            publishProgressDialog.dismiss();
            getLoaderManager().restartLoader(ISSUE_LOADER_ID, null, issueLoaderCallback);
        }

        @Override
        public void onLoaderReset(Loader<Void> loader) {
        }
    };


    private int issueId;
    private Issue issue;
    private PublishData lastPublishData;
    private AlertDialog publishDialog;
    private ProgressDialog publishProgressDialog;
    private IssueAdapter issueAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        issueId = getActivity().getIntent().getIntExtra(EXTRA_ISSUE_ID, -1);
        if (issueId == -1) {
            throw new IllegalStateException("EXTRA_ISSUE_ID wasn't found in intent");
        }
        issueAdapter = new IssueAdapter(getActivity());
        getLoaderManager().initLoader(ISSUE_LOADER_ID, new Bundle(), this.issueLoaderCallback);
        View view =  inflater.inflate(R.layout.fragment_issue_detail, container);
        ExpandableListView listView = (ExpandableListView) view.findViewById(android.R.id.list);
        listView.setAdapter(issueAdapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.issue_detail, menu);
    }

    private String getTextFromPublishDialog(int id) {
        return  ((EditText) publishDialog.findViewById(id)).getText().toString();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String message = getTextFromPublishDialog(R.id.publish_message);
        String subject = getTextFromPublishDialog(R.id.publish_subject);
        String cc = getTextFromPublishDialog(R.id.publish_cc);
        String reviewers = getTextFromPublishDialog(R.id.publish_reviewers);
        lastPublishData = new PublishData(issueId, message, subject, cc, reviewers);
        publishProgressDialog = new ProgressDialog(getActivity());
        publishProgressDialog.setIndeterminate(true);
        publishProgressDialog.setMessage(getActivity().getString(R.string.publish_progress_message));
        publishProgressDialog.show();
        getLoaderManager().restartLoader(PUBLISH_LOADER_ID, null, this.publishLoaderCallback);
    }

    public void showPublishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.publish_action);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View publishView = inflater.inflate(R.layout.publish_dialog, null);
        builder.setView(publishView);
        if (issue != null) {
            ViewUtils.setText(publishView, R.id.publish_subject, issue.subject());
            publishView.findViewById(R.id.publish_message).requestFocus();
            ViewUtils.setText(publishView, R.id.publish_reviewers, TextUtils.join(", ", issue.reviewers()));
            ViewUtils.setText(publishView, R.id.publish_cc, issue.ccdString());
        }
        builder.setPositiveButton(R.string.publish_action, this);
        builder.setNeutralButton(R.string.quick_lgtm, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        publishDialog = builder.create();
        publishDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_publish) {
            showPublishDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
