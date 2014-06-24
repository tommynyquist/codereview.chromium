package com.chrome.codereview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.phone.IssueDetailActivity;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.EmailUtils;

public class UserIssuesActivity extends Activity {

    private static final int REQUEST_LOGIN = 1;

    private class PhoneIssueSelectionListener implements UserIssuesFragment.IssueSelectionListener {

        @Override
        public void onIssueSelected(Issue issue) {
            Intent intent = new Intent(UserIssuesActivity.this, IssueDetailActivity.class);
            intent.putExtra(IssueDetailsFragment.EXTRA_ISSUE_ID, issue.id());
            startActivity(intent);
        }
    }

    private class TabletIssueSelectionListener implements UserIssuesFragment.IssueSelectionListener {

        @Override
        public void onIssueSelected(Issue issue) {
            issueDetailsFragment.setIssueId(issue.id());
        }
    }

    private IssueDetailsFragment issueDetailsFragment;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_LOGIN && resultCode != RESULT_OK) {
            return;
        }
        initializeViews(ServerCaller.from(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServerCaller serverCaller = ServerCaller.from(this);
        if (serverCaller.getState() != ServerCaller.State.OK) {
            startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
            return;
        }
        initializeViews(serverCaller);
    }

    private void initializeViews(ServerCaller serverCaller) {
        getActionBar().setTitle(getString(R.string.issues_title, EmailUtils.retrieveAccountName(serverCaller.getAccountName())));
        setContentView(R.layout.activity_issue_list);
        UserIssuesFragment userIssuesFragment = (UserIssuesFragment) getFragmentManager().findFragmentById(R.id.issue_list);
        issueDetailsFragment = (IssueDetailsFragment) getFragmentManager().findFragmentById(R.id.issue_details);
        userIssuesFragment.setIssueSelectionListener(issueDetailsFragment == null ? new PhoneIssueSelectionListener() : new TabletIssueSelectionListener());
        if (issueDetailsFragment != null) {
            userIssuesFragment.selectFirstIssue();
        }
    }

}
