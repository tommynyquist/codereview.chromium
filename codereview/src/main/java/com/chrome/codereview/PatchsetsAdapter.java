package com.chrome.codereview;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.PatchSetFile;
import com.chrome.codereview.model.TryBotResult;
import com.chrome.codereview.utils.BaseIDExpandableAdapter;
import com.chrome.codereview.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sergeyv on 29/5/14.
 */
public class PatchSetsAdapter extends BaseIDExpandableAdapter {

    private static final int TYPE_PATCH_SET = 0;
    private static final int TYPE_BOT_RESULTS = 1;

    private List<PatchSet> patchsets = new ArrayList<PatchSet>();
    private LayoutInflater inflater;
    private Context context;

    public PatchSetsAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setPatchsets(List<PatchSet> patchsets) {
        this.patchsets = patchsets;
    }

    @Override
    public int getGroupCount() {
        return patchsets.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        PatchSet patchSet = getGroup(groupPosition);
        return patchSet.files().size() + (patchSet.hasTryBotsResults() ? 1 : 0);
    }

    @Override
    public PatchSet getGroup(int groupPosition) {
        return patchsets.get(groupPosition);
    }

    @Override
    public int getChildTypeCount() {
        return 2;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        PatchSet patchSet = getGroup(groupPosition);
        return patchSet.files().size() == childPosition ? TYPE_BOT_RESULTS : TYPE_PATCH_SET;
    }

    @Override
    public PatchSetFile getChild(int groupPosition, int childPosition) {
        int childType = getChildType(groupPosition, childPosition);
        return childType == TYPE_PATCH_SET ? patchsets.get(groupPosition).files().get(childPosition) : null;
    }

    @Override
    public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
        PatchSet patchSet = getGroup(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.patchset_item, parent, false);
        }
        String message = patchSet.message();
        String patchSetText = position + ": " + (TextUtils.isEmpty(message) ? context.getString(R.string.empty_message) : message);
        ViewUtils.setText(convertView, R.id.patchset_message, patchSetText);
        fillStatsView(convertView, patchSet.linesAdded(), patchSet.linesRemoved(), patchSet.numComments(), patchSet.numDrafts());
        convertView.findViewById(R.id.patchset_card).setBackgroundResource(isExpanded ? R.drawable.patchset_bg_expanded : R.drawable.patchset_bg_collapsed);
        convertView.findViewById(R.id.list_divider).setVisibility(isExpanded ? View.VISIBLE : View.INVISIBLE);
        convertView.findViewById(R.id.bottom_space).setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        ViewUtils.expandView(convertView, isExpanded);
        return convertView;
    }

    private View getPatchSetFileView(int groupPosition, int childPosition, View convertView, ViewGroup parent) {
        PatchSetFile patchSetFile = getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.patchset_file_item, parent, false);
        }
        ViewUtils.setText(convertView, R.id.file_name, patchSetFile.path());
        fillFileStatusView((TextView) convertView.findViewById(R.id.file_status), patchSetFile.status());
        fillStatsView(convertView, patchSetFile.numAdded(), patchSetFile.numRemoved(), patchSetFile.numberOfComments(), patchSetFile.numberOfDrafts());
        return convertView;
    }

    private View getTryBotResultsView(int groupPosition, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.try_bot_results_item, parent, false);
        }
        PatchSet patchSet = getGroup(groupPosition);
        String resultsString = getTryBotResultsString(patchSet.botToState(), TryBotResult.Result.FAILURE);

        if (resultsString == null) {
            resultsString = getTryBotResultsString(patchSet.botToState(), TryBotResult.Result.RUNNING);
        }

        if (resultsString == null) {
            resultsString = "Succeed on all bots";
        }
        ViewUtils.setText(convertView, R.id.try_bot_results, resultsString);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (getChildType(groupPosition, childPosition) == TYPE_PATCH_SET) {
            convertView = getPatchSetFileView(groupPosition, childPosition, convertView, parent);
        } else {
            convertView = getTryBotResultsView(groupPosition, convertView, parent);
        }
        convertView.findViewById(R.id.list_divider).setVisibility(isLastChild ? View.INVISIBLE : View.VISIBLE);
        convertView.setBackgroundResource(isLastChild ? R.drawable.patchset_file_bg_last : R.drawable.patchset_file_bg);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void fillFileStatusView(TextView view, PatchSetFile.Status status) {
        int color = 0;
        switch (status) {
            case MOVED_MODIFIED:
            case MODIFIED:
                color = R.color.scheme_blue;
                break;
            case ADDED:
                color = R.color.scheme_green;
                break;
            case DELETED:
                color = R.color.scheme_red;
                break;
        }
        view.setTextColor(context.getResources().getColor(color));
        view.setText(status.toString());
    }

    public void fillStatsView(View convertView, int numAdded, int numRemoved, int numComments, int numDrafts) {
        ForegroundColorSpan greenSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.scheme_green));
        ForegroundColorSpan redSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.scheme_red));
        SpannableStringBuilder linesStats = createSpannedString(greenSpan, R.string.lines_added, numAdded, redSpan, R.string.lines_removed, numRemoved, ", ");
        ViewUtils.setText(convertView, R.id.diff_stats, linesStats);

        boolean showCommentsView = numDrafts != 0 || numComments != 0;
        convertView.findViewById(R.id.comments_number).setVisibility(showCommentsView ? View.VISIBLE : View.INVISIBLE);
        SpannableStringBuilder commentsStats = createSpannedString(null, R.string.comments_number, numComments, null, R.string.drafts_number, numDrafts, ", ");
        ViewUtils.setText(convertView, R.id.comments_number, commentsStats);
    }

    private SpannableStringBuilder createSpannedString(Object what1, int res1, int count1, Object what2, int res2, int count2, CharSequence separator) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String text1 = context.getString(res1, count1);
        if (count1 != 0) {
            builder.append(text1);
        }
        if (what1 != null) {
            builder.setSpan(what1, 0, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        if (count2 == 0) {
            return builder;
        }
        if (count1 != 0) {
            builder.append(separator);
        }
        int start = builder.length();
        builder.append(context.getString(res2, count2));

        if (what2 != null) {
            builder.setSpan(what2, start, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    private static List<String> findAll(Map<String, TryBotResult.Result> botToState, TryBotResult.Result query) {
        List<String> result = new ArrayList<String>();

        for (String bot: botToState.keySet()) {
            if (botToState.get(bot) == query) {
                result.add(bot);
            }
        }
        return result;
    }

    private static String getTryBotResultsString(Map<String, TryBotResult.Result> botToState, TryBotResult.Result query) {
        List<String> filtered = findAll(botToState, query);
        return  filtered.isEmpty() ? null : TextUtils.join(", ", filtered);
    }

}
