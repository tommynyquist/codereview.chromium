package com.chrome.codereview;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 29/4/14.
 */
public class DiffFragment extends ListFragment {

    public static final String DIFF_EXTRA = "DIFF_EXTRA";

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ArrayList<String> diffExtra = getActivity().getIntent().getStringArrayListExtra(DIFF_EXTRA);
        setListAdapter(new DiffAdapter(getActivity(), R.layout.diff_line, diffExtra));
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
