package com.chrome.codereview;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.chrome.codereview.model.Comment;
import com.chrome.codereview.model.FileDiff;
import com.chrome.codereview.utils.CachedLoader;
import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.ViewUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sergeyv on 29/4/14.
 */
public class DiffFragment extends ListFragment implements LoaderManager.LoaderCallbacks<FileDiff> {

    public static final String COMMENTS_EXTRA = "COMMENTS_EXTRA";
    public static final String ISSUE_ID_EXTRA = "ISSUE_ID_EXTRA";
    public static final String PATCH_SET_ID_EXTRA = "PATCH_SET_ID_EXTRA";
    public static final String PATCH_ID_EXTRA = "PATCH_ID_EXTRA";

    private static class DiffLoader extends CachedLoader<FileDiff> {

        private final int issueId;
        private final int patchSetId;
        private final int patchId;

        public DiffLoader(Context context, int issueId, int patchSetId, int patchId) {
            super(context);
            this.issueId = issueId;
            this.patchSetId = patchSetId;
            this.patchId = patchId;
        }

        @Override
        public FileDiff loadInBackground() {
            try {
                return serverCaller().loadDiff(issueId, patchSetId, patchId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class DiffAdapter extends BaseAdapter {

        public static final int LINE_TYPE = 0;
        public static final int COMMENT_TYPE = 1;
        private final List<Object> linesWithComments;
        private final LayoutInflater inflater;
        private final Context context;

        public DiffAdapter(Context context, List<FileDiff.DiffLine> diffs, List<Comment> comments) {
            this.context = context;
            inflater = LayoutInflater.from(context);

            HashMap<Pair<Integer, Boolean>, List<Comment>> lineToComment = new HashMap<Pair<Integer, Boolean>, List<Comment>>();
            for (Comment comment : comments) {
                Pair<Integer, Boolean> key = new Pair<Integer, Boolean>(comment.line(), comment.left());
                if (!lineToComment.containsKey(key)) {
                    lineToComment.put(key, new ArrayList<Comment>());
                }
                lineToComment.get(key).add(comment);
            }
            linesWithComments = new ArrayList<Object>(diffs.size() + comments.size());
            for (FileDiff.DiffLine diffLine : diffs) {
                linesWithComments.add(diffLine);
                switch (diffLine.type()) {
                    case MARKER:
                        break;
                    case BOTH_SIDE:
                        addAll(linesWithComments, lineToComment.get(new Pair<Integer, Boolean>(diffLine.leftLineNumber(), true)));
                        addAll(linesWithComments, lineToComment.get(new Pair<Integer, Boolean>(diffLine.rightLineNumber(), false)));
                        break;
                    case RIGHT:
                        addAll(linesWithComments, lineToComment.get(new Pair<Integer, Boolean>(diffLine.rightLineNumber(), false)));
                        break;
                    case LEFT:
                        addAll(linesWithComments, lineToComment.get(new Pair<Integer, Boolean>(diffLine.leftLineNumber(), true)));
                        break;
                }
            }
        }

        private static void addAll(List<Object> main, List<Comment> add) {
            if (add != null) {
                main.addAll(add);
            }
        }

        @Override
        public int getCount() {
            return linesWithComments.size();
        }

        @Override
        public Object getItem(int position) {
            return linesWithComments.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
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
            ViewUtils.setText(convertView, R.id.author, comment.author());
            ViewUtils.setText(convertView, R.id.comment_text, comment.text());
            ViewUtils.setText(convertView, R.id.date, DateUtils.createAgoText(context, comment.date()));
            return convertView;
        }

        public View getDiffLineView(FileDiff.DiffLine line, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.diff_line, parent, false);
            }

            int color;
            switch (line.type()) {
                case LEFT:
                    color = R.color.diff_remove;
                    break;
                case RIGHT:
                    color = R.color.diff_add;
                    break;
                case MARKER:
                    color = R.color.diff_marker_bg;
                    break;
                default:
                    color = R.color.diff_default_background;
            }
            convertView.setBackgroundColor(context.getResources().getColor(color));
            ViewUtils.setText(convertView, android.R.id.text1, line.text());
            return convertView;
        }

    }

    private int issueId;
    private int patchSetId;
    private int patchId;
    private ArrayList<Comment> comments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        comments = intent.getParcelableArrayListExtra(COMMENTS_EXTRA);
        issueId = intent.getIntExtra(ISSUE_ID_EXTRA, -1);
        patchSetId = intent.getIntExtra(PATCH_SET_ID_EXTRA, -1);
        patchId = intent.getIntExtra(PATCH_ID_EXTRA, -1);
        getLoaderManager().initLoader(0, new Bundle(), this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Loader<FileDiff> onCreateLoader(int id, Bundle args) {
        return new DiffLoader(getActivity(), issueId, patchSetId, patchId);
    }

    @Override
    public void onLoadFinished(Loader<FileDiff> loader, FileDiff data) {
        if (data == null) {
            return;
        }
        setListAdapter(new DiffAdapter(getActivity(), data.content(), comments));
    }

    @Override
    public void onLoaderReset(Loader<FileDiff> loader) {
    }

}
