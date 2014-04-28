package com.chrome.codereview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.chrome.codereview.requests.ServerCaller;

public class UserIssuesActivity extends Activity {

    private static final int REQUEST_LOGIN = 1;

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
        getActionBar().setTitle(serverCaller.getAccountName());
        setContentView(R.layout.activity_issue_list);
    }

}
