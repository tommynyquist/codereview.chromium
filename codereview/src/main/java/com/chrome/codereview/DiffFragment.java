package com.chrome.codereview;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;

import com.chrome.codereview.model.Comment;
import com.chrome.codereview.model.FileDiff;
import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.PatchSetFile;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.CachedLoader;
import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.ViewUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sergeyv on 29/4/14.
 */
public class DiffFragment extends ListFragment implements AdapterView.OnItemClickListener {

    public static final int RESULT_REFRESH = 10;
    public static final String COMMENTS_EXTRA = "COMMENTS_EXTRA";
    public static final String ISSUE_ID_EXTRA = "ISSUE_ID_EXTRA";
    public static final String PATCH_SET_ID_EXTRA = "PATCH_SET_ID_EXTRA";
    public static final String PATCH_ID_EXTRA = "PATCH_ID_EXTRA";

    private static final int DIFF_LOADER_ID = 0;
    private static final int INLINE_DRAFT_LOADER_ID = 1;
    private static final int PATCH_SET_LOADER_ID = 1;
    private static final String KEY_COMMENT = "comment";

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

    private static class InlineDraftLoader extends CachedLoader<Void> {

        private int issueId;
        private int patchSetId;
        private int patchId;
        private Comment comment;

        public InlineDraftLoader(Context context, int issueId, int patchSetId, int patchId, Comment comment) {
            super(context);
            this.issueId = issueId;
            this.patchSetId = patchSetId;
            this.patchId = patchId;
            this.comment = comment;
        }

        @Override
        public Void loadInBackground() {
            try {
                serverCaller().inlineDraft(issueId, patchSetId, patchId, comment);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class PatchSetLoader extends CachedLoader<PatchSet> {

        private int issueId;
        private int patchSetId;

        public PatchSetLoader(Context context, int issueId, int patchSetId) {
            super(context);
            this.issueId = issueId;
            this.patchSetId = patchSetId;
        }

        @Override
        public PatchSet loadInBackground() {
            return serverCaller().loadPatchSet(issueId, patchSetId);
        }
    }

    private class DiffAdapter extends BaseAdapter implements View.OnClickListener {

        public static final int LINE_TYPE = 0;
        public static final int COMMENT_TYPE = 1;
        private final List<Object> linesWithComments;
        private final List<FileDiff.DiffLine> diffs;
        private final LayoutInflater inflater;
        private final Context context;

        public DiffAdapter(Context context, List<FileDiff.DiffLine> diffs, List<Comment> comments) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.diffs = diffs;
            linesWithComments = new ArrayList<Object>(diffs.size() + comments.size());
            resetComments(comments);
        }

        private void addAll(List<Object> main, List<Comment> add) {
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
            if (comment.isDraft()) {
                ViewUtils.setText(convertView, R.id.author, context.getString(R.string.draft_author));
            } else {
                ViewUtils.setText(convertView, R.id.author, comment.author());
            }
            ViewUtils.setText(convertView, R.id.comment_text, comment.text());
            ViewUtils.setText(convertView, R.id.date, DateUtils.createAgoText(context, comment.date()));
            View viewById = convertView.findViewById(R.id.reply);
            viewById.setOnClickListener(this);
            viewById.setTag(comment);
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

        @Override
        public void onClick(View v) {
            Comment comment = (Comment) v.getTag();
            showWriteCommentDialog(comment.line(), comment.left());
        }

        void resetComments(List<Comment> comments) {
            HashMap<Pair<Integer, Boolean>, List<Comment>> lineToComment = new HashMap<Pair<Integer, Boolean>, List<Comment>>();
            for (Comment comment : comments) {
                Pair<Integer, Boolean> key = new Pair<Integer, Boolean>(comment.line(), comment.left());
                if (!lineToComment.containsKey(key)) {
                    lineToComment.put(key, new ArrayList<Comment>());
                }
                lineToComment.get(key).add(comment);
            }
            linesWithComments.clear();

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

            notifyDataSetChanged();
        }

    }

    private class InlineDraftDialogListener implements DialogInterface.OnClickListener {

        private final int line;
        private final boolean left;
        private final EditText text;

        private InlineDraftDialogListener(int line, boolean left, EditText text) {
            this.line = line;
            this.left = left;
            this.text = text;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            ServerCaller.from(getActivity()).getAccountName();
            Comment comment = new Comment(true, text.getText().toString(), ServerCaller.from(getActivity()).getAccountName(), line, left, new Date());
            Bundle arg = new Bundle();
            arg.putParcelable(KEY_COMMENT, comment);
            getLoaderManager().restartLoader(INLINE_DRAFT_LOADER_ID, arg, DiffFragment.this.inlineDraftCallback);
        }
    }

    private int issueId;
    private int patchSetId;
    private int patchId;
    private ArrayList<Comment> comments;
    private DiffAdapter diffAdapter;

    private LoaderManager.LoaderCallbacks<FileDiff> diffLoaderCallback = new LoaderManager.LoaderCallbacks<FileDiff>() {

        @Override
        public Loader<FileDiff> onCreateLoader(int id, Bundle args) {
            return new DiffLoader(getActivity(), issueId, patchSetId, patchId);
        }

        @Override
        public void onLoadFinished(Loader<FileDiff> loader, FileDiff data) {
            if (data == null) {
                return;
            }
            diffAdapter = new DiffAdapter(getActivity(), data.content(), comments);
            setListAdapter(diffAdapter);
            getListView().setOnItemClickListener(DiffFragment.this);
        }

        @Override
        public void onLoaderReset(Loader<FileDiff> loader) {
        }

    };

    private LoaderManager.LoaderCallbacks<Void> inlineDraftCallback = new LoaderManager.LoaderCallbacks<Void>() {

        @Override
        public Loader<Void> onCreateLoader(int id, Bundle args) {
            return new InlineDraftLoader(getActivity(), issueId, patchSetId, patchId, (Comment) args.getParcelable(KEY_COMMENT));
        }

        @Override
        public void onLoadFinished(Loader<Void> loader, Void data) {
            getLoaderManager().restartLoader(PATCH_SET_LOADER_ID, new Bundle(), DiffFragment.this.patchSetLoaderCallback);
            getActivity().setResult(RESULT_REFRESH);
        }

        @Override
        public void onLoaderReset(Loader<Void> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<PatchSet> patchSetLoaderCallback = new LoaderManager.LoaderCallbacks<PatchSet>() {
        @Override
        public Loader<PatchSet> onCreateLoader(int id, Bundle args) {
            return new PatchSetLoader(getActivity(), issueId, patchSetId);
        }

        @Override
        public void onLoadFinished(Loader<PatchSet> loader, PatchSet data) {
            if (data == null) {
                return;
            }

            for (PatchSetFile file : data.files()) {
                if (file.id() == patchId) {
                    diffAdapter.resetComments(file.comments());
                    return;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<PatchSet> loader) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        comments = intent.getParcelableArrayListExtra(COMMENTS_EXTRA);
        issueId = intent.getIntExtra(ISSUE_ID_EXTRA, -1);
        patchSetId = intent.getIntExtra(PATCH_SET_ID_EXTRA, -1);
        patchId = intent.getIntExtra(PATCH_ID_EXTRA, -1);
        getLoaderManager().initLoader(DIFF_LOADER_ID, new Bundle(), this.diffLoaderCallback);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (diffAdapter.getItemViewType(position) != DiffAdapter.LINE_TYPE) {
            return;
        }

        FileDiff.DiffLine diffLine = (FileDiff.DiffLine) diffAdapter.getItem(position);
        if (diffLine.type() == FileDiff.LineType.MARKER) {
            return;
        }
        int line = diffLine.type() == FileDiff.LineType.LEFT ? diffLine.leftLineNumber() : diffLine.rightLineNumber();
        showWriteCommentDialog(line, diffLine.type() == FileDiff.LineType.LEFT);
    }

    private void showWriteCommentDialog(int line, boolean left) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.new_comment);
        EditText editText = new EditText(getActivity());
        builder.setView(editText);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new InlineDraftDialogListener(line, left, editText));
        builder.create().show();
    }

}
