package com.chrome.codereview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.TryBotResult;
import com.chrome.codereview.utils.ViewUtils;

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
            convertView = inflater.inflate(R.layout.try_bot_result_item, parent, false);
        }
        ViewUtils.setText(convertView, R.id.builder_name, getItem(position).builder());
        int resource = 0;
        int textColor = 0;
        switch (getItem(position).result()) {
            case SUCCESS:
                textColor = R.color.scheme_green;
                resource = R.string.try_bot_success;
                break;
            case FAILURE:
                textColor = R.color.scheme_red;
                resource = R.string.try_bot_failure;
                break;
            case PENDING:
                textColor = R.color.scheme_blue;
                resource = R.string.try_bot_pending;
                break;
            case RUNNING:
                textColor = R.color.scheme_blue;
                resource = R.string.try_bot_running;
                break;
            case SKIPPED:
                textColor = R.color.scheme_blue;
                resource = R.string.try_bot_skipped;
                break;
            case UNKNOWN:
                textColor = android.R.color.darker_gray;
                resource = R.string.try_bot_unknown;
        }
        TextView resultView = (TextView) convertView.findViewById(R.id.result);
        resultView.setText(resource);
        resultView.setTextColor(getContext().getResources().getColor(textColor));
        return convertView;
    }

}
