package com.chrome.codereview.phone;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.chrome.codereview.DiffAdapter;
import com.chrome.codereview.R;
import com.chrome.codereview.model.Comment;
import com.chrome.codereview.model.FileDiff;
import com.chrome.codereview.utils.ViewUtils;

import java.util.HashMap;
import java.util.List;

public class UnifiedDiffAdapter extends DiffAdapter implements AdapterView.OnItemClickListener{

    public static final int LINE_TYPE = 0;
    public static final int COMMENT_TYPE = 1;

    public UnifiedDiffAdapter(Context context, FileDiff fileDiff, List<Comment> comments) {
        super(context, fileDiff, comments);
        resetComments(comments);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return linesWithComments.get(position) instanceof FileDiff.DiffLine ? LINE_TYPE : COMMENT_TYPE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == LINE_TYPE) {
            return getDiffLineView((FileDiff.DiffLine) getItem(position), convertView, parent);
        }
        return getCommentView((Comment) getItem(position), convertView, parent);
    }

    public View getCommentView(Comment comment, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.comment_item, parent, false);
        }
        fillCommentView(comment, convertView);
        return convertView;
    }

    public View getDiffLineView(FileDiff.DiffLine line, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.diff_line, parent, false);
        }

        int resource;
        switch (line.type()) {
            case LEFT:
                resource = R.drawable.diff_removed_line_bg;
                break;
            case RIGHT:
                resource = R.drawable.diff_added_line_bg;
                break;
            case MARKER:
                resource = R.drawable.diff_marker_line_bg;
                break;
            default:
                resource = R.drawable.diff_default_line_bg;
        }
        convertView.setBackgroundDrawable(context.getResources().getDrawable(resource));
        ViewUtils.setText(convertView, android.R.id.text1, line.text());
        return convertView;
    }

    @Override
    protected void rebuildWithComments(HashMap<Pair<Integer, Boolean>, List<Comment>> lineToComments) {
        for (FileDiff.DiffLine diffLine : diffLines) {
            linesWithComments.add(diffLine);
            switch (diffLine.type()) {
                case MARKER:
                    break;
                case BOTH_SIDE:
                    addAll(linesWithComments, lineToComments.get(new Pair<Integer, Boolean>(diffLine.leftLineNumber(), true)));
                    addAll(linesWithComments, lineToComments.get(new Pair<Integer, Boolean>(diffLine.rightLineNumber(), false)));
                    break;
                case RIGHT:
                    addAll(linesWithComments, lineToComments.get(new Pair<Integer, Boolean>(diffLine.rightLineNumber(), false)));
                    break;
                case LEFT:
                    addAll(linesWithComments, lineToComments.get(new Pair<Integer, Boolean>(diffLine.leftLineNumber(), true)));
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (this.getItemViewType(position) != LINE_TYPE || commentActionListener == null) {
            return;
        }

        FileDiff.DiffLine diffLine = (FileDiff.DiffLine) getItem(position);
        if (diffLine.type() == FileDiff.LineType.MARKER) {
            return;
        }
        int line = diffLine.type() == FileDiff.LineType.LEFT ? diffLine.leftLineNumber() : diffLine.rightLineNumber();
        commentActionListener.writeComment(line, diffLine.type() == FileDiff.LineType.LEFT);
    }

    private static void addAll(List<Object> main, List<Comment> add) {
        if (add != null) {
            main.addAll(add);
        }
    }

}
