package com.chrome.codereview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Window;

import com.chrome.codereview.model.PatchSetFile;

import java.util.ArrayList;

/**
 * Created by sergeyv on 29/4/14.
 */
public class DiffActivity extends Activity {

    public static final String PATH_EXTRA = "PATH_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_phone_diff);
        getActionBar().setTitle(Uri.parse(getIntent().getStringExtra(PATH_EXTRA)).getLastPathSegment());
    }

    public static void startDiffActivity(Activity activity, int requestCode, int issueId, int patchSetId, PatchSetFile file) {
        Intent intent = new Intent(activity, DiffActivity.class);
        intent.putExtra(DiffActivity.PATH_EXTRA, file.path());
        intent.putExtra(DiffFragment.ISSUE_ID_EXTRA, issueId);
        intent.putExtra(DiffFragment.PATCH_SET_ID_EXTRA, patchSetId);
        intent.putExtra(DiffFragment.PATCH_ID_EXTRA, file.id());
        intent.putParcelableArrayListExtra(DiffFragment.COMMENTS_EXTRA, new ArrayList<Parcelable>(file.comments()));
        activity.startActivityForResult(intent, requestCode);
    }
}
