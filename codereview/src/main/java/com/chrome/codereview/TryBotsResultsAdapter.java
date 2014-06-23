package com.chrome.codereview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.TryBotResult;
import com.chrome.codereview.utils.ViewUtils;

import java.util.List;

/**
 * Created by sergeyv on 24/6/14.
 */
public class TryBotsResultsAdapter extends ArrayAdapter<TryBotResult> {

    private final LayoutInflater inflater;

    public TryBotsResultsAdapter(Context context, PatchSet patchSet) {
        super(context, 0, patchSet.tryBotResults());
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        ViewUtils.setText(convertView, android.R.id.text1, getItem(position).builder());
        return convertView;
    }
}
