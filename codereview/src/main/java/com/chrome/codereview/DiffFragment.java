package com.chrome.codereview;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.chrome.codereview.model.Comment;
import com.chrome.codereview.model.FileDiff;
import com.chrome.codereview.model.PatchSet;
import com.chrome.codereview.model.PatchSetFile;
import com.chrome.codereview.phone.UnifiedDiffAdapter;
import com.chrome.codereview.tablet.SideBySideDiffAdapter;
import com.chrome.codereview.utils.BaseListFragment;
import com.chrome.codereview.utils.CachedLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 29/4/14.
 */
public class DiffFragment extends BaseListFragment implements DiffAdapter.CommentActionListener {

    public static final int RESULT_REFRESH = 10;
    private static final String COMMENTS_ARG = "COMMENTS_ARG";
    private static final String ISSUE_ID_ARG = "ISSUE_ID_ARG";
    private static final String PATCH_SET_ID_ARG = "PATCH_SET_ID_ARG";
    private static final String PATCH_ID_ARG = "PATCH_ID_ARG";

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

    private class InlineDraftDialogListener implements DialogInterface.OnClickListener {

        private final int line;
        private final boolean left;
        private final EditText text;
        private final String messageId;

        private InlineDraftDialogListener(EditText text, String messageId, int line, boolean left) {
            this.line = line;
            this.left = left;
            this.text = text;
            this.messageId = messageId;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Comment comment = Comment.createDraft(text.getText().toString(), line, left, messageId);
            restartInlineDraftLoader(comment);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && loadDiffInProgress) {
            startProgress();
            return;
        }
        if (isVisibleToUser && loadDiffInProgress) {
            stopProgress();
            return;
        }
    }

    private void restartInlineDraftLoader(Comment comment) {
        Bundle arg = new Bundle();
        arg.putParcelable(KEY_COMMENT, comment);
        getLoaderManager().restartLoader(INLINE_DRAFT_LOADER_ID, arg, this.inlineDraftCallback);
    }

    private int issueId;
    private int patchSetId;
    private int patchId;
    private List<Comment> comments;
    private DiffAdapter diffAdapter;
    private boolean loadDiffInProgress;
    private LoaderManager.LoaderCallbacks<FileDiff> diffLoaderCallback = new LoaderManager.LoaderCallbacks<FileDiff>() {

        @Override
        public Loader<FileDiff> onCreateLoader(int id, Bundle args) {
            loadDiffInProgress = true;
            if (getUserVisibleHint()) {
                startProgress();
            }
            return new DiffLoader(getActivity(), issueId, patchSetId, patchId);
        }

        @Override
        public void onLoadFinished(Loader<FileDiff> loader, FileDiff data) {
            loadDiffInProgress = false;
            if (getUserVisibleHint()) {
                stopProgress();
            }
            if (data == null) {
                return;
            }
            boolean useSideBySideDiff = getResources().getBoolean(R.bool.use_side_by_side_diff);
            diffAdapter = useSideBySideDiff ? new SideBySideDiffAdapter(getActivity(), data, comments) : new UnifiedDiffAdapter(getActivity(), data, comments);
            diffAdapter.setCommentActionListener(DiffFragment.this);
            setListAdapter(diffAdapter);
            getListView().setOnItemClickListener(diffAdapter);
        }

        @Override
        public void onLoaderReset(Loader<FileDiff> loader) {
        }

    };

    private LoaderManager.LoaderCallbacks<Void> inlineDraftCallback = new LoaderManager.LoaderCallbacks<Void>() {

        @Override
        public Loader<Void> onCreateLoader(int id, Bundle args) {
            startProgress();
            return new InlineDraftLoader(getActivity(), issueId, patchSetId, patchId, (Comment) args.getParcelable(KEY_COMMENT));
        }

        @Override
        public void onLoadFinished(Loader<Void> loader, Void data) {
            refresh();
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
            stopProgress();
            if (data == null) {
                return;
            }

            for (PatchSetFile file : data.files()) {
                if (file.id() == patchId) {
                    comments = file.comments();
                    diffAdapter.resetComments(comments);
                    return;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<PatchSet> loader) {

        }
    };

    public void initialize(int issueId, PatchSet patchSet, int position) {
        Bundle args = new Bundle();
        args.putInt(ISSUE_ID_ARG, issueId);
        args.putInt(PATCH_SET_ID_ARG, patchSet.id());
        PatchSetFile patchSetFile = patchSet.files().get(position);
        args.putInt(PATCH_ID_ARG, patchSetFile.id());
        args.putParcelableArrayList(COMMENTS_ARG, new ArrayList<Parcelable>(patchSetFile.comments()));
        this.setArguments(args);
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        comments = args.getParcelableArrayList(COMMENTS_ARG);
        issueId = args.getInt(ISSUE_ID_ARG, -1);
        patchSetId = args.getInt(PATCH_SET_ID_ARG, -1);
        patchId = args.getInt(PATCH_ID_ARG, -1);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_diff;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DiffFragment.COMMENTS_ARG, new ArrayList<Parcelable>(comments));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState, false);
        List<Comment> savedComments = null;
        if (savedInstanceState != null) {
            savedComments = savedInstanceState.getParcelableArrayList(COMMENTS_ARG);
        }
        if (savedComments != null) {
            comments = savedComments;
        }
        getLoaderManager().initLoader(DIFF_LOADER_ID, new Bundle(), this.diffLoaderCallback);
        return layout;
    }

    @Override
    protected void refresh() {
        getLoaderManager().restartLoader(PATCH_SET_LOADER_ID, new Bundle(), DiffFragment.this.patchSetLoaderCallback);
    }

    private void showWriteCommentDialog(String text, String messageId, int line, boolean left) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.new_comment, line));
        EditText editText = new EditText(getActivity());
        editText.setText(text);
        builder.setView(editText);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new InlineDraftDialogListener(editText, messageId, line, left));
        builder.create().show();
    }

    @Override
    public void removeDraft(Comment comment) {
        restartInlineDraftLoader(Comment.createDraft("", comment.line(), comment.left(), comment.messageId()));
    }

    @Override
    public void editDraft(Comment comment) {
        showWriteCommentDialog(comment.text(), comment.messageId(), comment.line(), comment.left());
    }

    @Override
    public void doneComment(Comment comment) {
        restartInlineDraftLoader(Comment.createDraft(Comment.quote(comment) + "\nDone.", comment.line(), comment.left(), ""));
    }

    @Override
    public void replyComment(Comment comment) {
        showWriteCommentDialog(Comment.quote(comment), "", comment.line(), comment.left());
    }

    @Override
    public void writeComment(int line, boolean left) {
        showWriteCommentDialog("", "", line, left);
    }

}
