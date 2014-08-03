package com.chrome.codereview;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.PatchSetFile;

/**
 * Created by sergeyv on 29/4/14.
 */
public class DiffActivity extends Activity implements ViewPager.OnPageChangeListener {

    private static final String PATCHSET_EXTRA = "PATCHSET_EXTRA";
    private static final String ISSUE_ID_EXTRA = "ISSUE_ID_EXTRA";
    private static final String PATCH_ID_EXTRA = "PATCH_ID_EXTRA";

    private PatchSet patchSet;
    private int issueId;
    private FragmentStatePagerAdapter fragmentStatePagerAdapter = new FragmentStatePagerAdapter(getFragmentManager()) {

        @Override
        public Fragment getItem(int position) {
            DiffFragment fragment = new DiffFragment();
            fragment.initialize(issueId, patchSet, position);
            return fragment;
        }

        @Override
        public int getCount() {
            return patchSet.files().size();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        patchSet = getIntent().getParcelableExtra(PATCHSET_EXTRA);
        issueId = getIntent().getIntExtra(ISSUE_ID_EXTRA, -1);
        setContentView(R.layout.activity_phone_diff);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(fragmentStatePagerAdapter);
        pager.setOnPageChangeListener(this);
        onPageSelected(0);
        int patchId = getIntent().getIntExtra(PATCH_ID_EXTRA, -1);
        int counter = 0;
        for (PatchSetFile file : patchSet.files()) {
            if (file.id() == patchId) {
                pager.setCurrentItem(counter);
                break;
            }
            counter++;
        }
    }

    public static void startDiffActivity(Fragment fragment, int requestCode, int issueId, PatchSet patchSet, int patchId) {
        Intent intent = new Intent(fragment.getActivity(), DiffActivity.class);
        intent.putExtra(PATCHSET_EXTRA, patchSet);
        intent.putExtra(ISSUE_ID_EXTRA, issueId);
        intent.putExtra(PATCH_ID_EXTRA, patchId);
        fragment.getActivity().startActivityFromFragment(fragment, intent, requestCode);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        String title = Uri.parse(patchSet.files().get(i).path()).getLastPathSegment();
        getActionBar().setTitle(title);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
