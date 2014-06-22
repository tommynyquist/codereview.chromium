package com.chrome.codereview;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

import com.chrome.codereview.model.Comment;
import com.chrome.codereview.model.FileDiff;
import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sergeyv on 22/6/14.
 */
public abstract class DiffAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnClickListener {

    public interface CommentActionListener {

        void removeDraft(Comment comment);

        void editDraft(Comment comment);

        void doneComment(Comment comment);

        void replyComment(Comment comment);

        void writeComment(int line, boolean left);
    }

    protected final List<FileDiff.DiffLine> diffLines;
    protected final List<Object> linesWithComments = new ArrayList<Object>();
    protected final LayoutInflater inflater;
    protected final Context context;
    protected CommentActionListener commentActionListener;

    public DiffAdapter(Context context, FileDiff fileDiff, List<Comment> comments) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        diffLines = fileDiff.content();
    }

    protected abstract void rebuildWithComments(HashMap<Pair<Integer, Boolean>, List<Comment>> lineToComments);

    public void resetComments(List<Comment> comments) {
        HashMap<Pair<Integer, Boolean>, List<Comment>> lineToComment = new HashMap<Pair<Integer, Boolean>, List<Comment>>();
        for (Comment comment : comments) {
            Pair<Integer, Boolean> key = new Pair<Integer, Boolean>(comment.line(), comment.left());
            if (!lineToComment.containsKey(key)) {
                lineToComment.put(key, new ArrayList<Comment>());
            }
            lineToComment.get(key).add(comment);
        }
        linesWithComments.clear();
        rebuildWithComments(lineToComment);
        notifyDataSetChanged();
    }

    public void setCommentActionListener(CommentActionListener commentActionListener) {
        this.commentActionListener = commentActionListener;
    }

    public void fillCommentView(Comment comment, View commentView) {
        if (comment.isDraft()) {
            ViewUtils.setText(commentView, R.id.author, context.getString(R.string.draft_author));
        } else {
            ViewUtils.setText(commentView, R.id.author, comment.author());
        }
        ViewUtils.setText(commentView, R.id.comment_text, comment.text());
        ViewUtils.setText(commentView, R.id.date, DateUtils.createAgoText(context, comment.date()));
        initImageButton(commentView, R.id.img1, R.drawable.ic_action_reply, R.drawable.ic_action_edit, comment);
        initImageButton(commentView, R.id.img2, R.drawable.ic_action_accept, R.drawable.ic_action_remove, comment);
    }

    private void initImageButton(View convertView, int buttonRes, int mainDrawableRes, int draftDrawableRes, Comment comment) {
        ImageButton image = (ImageButton) convertView.findViewById(buttonRes);
        if (comment.isDraft()) {
            image.setImageResource(draftDrawableRes);
        } else {
            image.setImageResource(mainDrawableRes);
        }
        image.setOnClickListener(this);
        image.setTag(comment);
    }

    @Override
    public void onClick(View v) {
        if (commentActionListener == null) {
            return;
        }
        Comment comment = (Comment) v.getTag();
        if (v.getId() == R.id.img2) {
            if (comment.isDraft()) {
                commentActionListener.removeDraft(comment);
            } else {
                commentActionListener.doneComment(comment);
            }
            return;
        }
        if (comment.isDraft()) {
            commentActionListener.editDraft(comment);
        } else {
            commentActionListener.replyComment(comment);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return linesWithComments.size();
    }

    @Override
    public Object getItem(int position) {
        return linesWithComments.get(position);
    }

}
