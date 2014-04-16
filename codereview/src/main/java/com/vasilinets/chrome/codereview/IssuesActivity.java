package com.vasilinets.chrome.codereview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.vasilinets.chrome.codereview.requests.ServerCaller;

public class IssuesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ServerCaller.from(this).getState() != ServerCaller.State.OK) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        setContentView(R.layout.activity_issues);

        getFragmentManager().beginTransaction().add(R.id.issues_main, new IssuesListFragment()).commit();
    }

}
