package com.chrome.codereview;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by sergeyv on 29/4/14.
 */
public class DiffFragment extends ListFragment {

    public static final String DIFF_EXTRA = "DIFF_EXTRA";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ArrayList<String> diffExtra = getActivity().getIntent().getStringArrayListExtra(DIFF_EXTRA);
        setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, diffExtra));
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
