package com.chrome.codereview;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.SparseArray;
import android.view.Menu;

import com.chrome.codereview.issuelists.BaseIssueListFragment;
import com.chrome.codereview.issuelists.CCIssuesFragment;
import com.chrome.codereview.issuelists.HiddenIssuesFragment;
import com.chrome.codereview.issuelists.IncomingIssuesFragment;
import com.chrome.codereview.issuelists.OutgoingIssuesFragment;
import com.chrome.codereview.issuelists.RecentlyClosedIssuesFragment;
import com.chrome.codereview.model.Issue;
import com.chrome.codereview.phone.IssueDetailActivity;
import com.chrome.codereview.requests.ServerCaller;


public class UserIssueActivityWithDrawer extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final Class[] ISSUE_FRAGMENTS = new Class[] {IncomingIssuesFragment.class, OutgoingIssuesFragment.class, CCIssuesFragment.class, RecentlyClosedIssuesFragment.class, HiddenIssuesFragment.class};
    private static final int REQUEST_LOGIN = 1;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private IssueDetailsFragment issueDetailsFragment;

    private SparseArray<BaseIssueListFragment> fragments = new SparseArray<BaseIssueListFragment>();

    private class PhoneIssueSelectionListener implements BaseIssueListFragment.IssueSelectionListener {

        @Override
        public void onIssueSelected(Issue issue) {
            Intent intent = new Intent(UserIssueActivityWithDrawer.this, IssueDetailActivity.class);
            intent.putExtra(IssueDetailsFragment.EXTRA_ISSUE_ID, issue.id());
            startActivity(intent);
        }
    }

    private class TabletIssueSelectionListener implements BaseIssueListFragment.IssueSelectionListener {

        @Override
        public void onIssueSelected(Issue issue) {
            issueDetailsFragment.setIssueId(issue.id());
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        BaseIssueListFragment fragment = getFragment(position);
        initIssueDetailsFragment();
        fragment.setIssueSelectionListener(issueDetailsFragment == null ? new PhoneIssueSelectionListener() : new TabletIssueSelectionListener());
        if (issueDetailsFragment != null) {
            issueDetailsFragment.setIssueId(-1);
            fragment.selectFirstIssue();
        }
        getActionBar().setTitle(getResources().getStringArray(R.array.drawer_titles)[position]);
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServerCaller serverCaller = ServerCaller.from(this);
        if (serverCaller.getState() != ServerCaller.State.OK) {
            startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
            return;
        }
        initializeViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_LOGIN && resultCode != RESULT_OK) {
            return;
        }
        initializeViews();
    }

    private void initializeViews() {
        setContentView(R.layout.activity_user_issue_activity_with_drawer);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        CleanUpService.scheduleCleanUp(this);
    }

    private void initIssueDetailsFragment() {
        if (issueDetailsFragment == null) {
            issueDetailsFragment = (IssueDetailsFragment) getFragmentManager().findFragmentById(R.id.issue_details);
        }
    }

    private BaseIssueListFragment getFragment(int position) {
        BaseIssueListFragment result = fragments.get(position);
        if (result == null) {
            Class cl = ISSUE_FRAGMENTS[position];
            try {
                result = (BaseIssueListFragment) cl.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            fragments.put(position, result);
        }
        return result;
    }

}
