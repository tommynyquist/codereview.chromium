package com.chrome.codereview;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.Message;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.BaseArrayAdapter;
import com.chrome.codereview.utils.CachedLoader;

/**
 * Created by sergeyv on 18/4/14.
 */
public class IssueFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Issue> {

    public static final String EXTRA_ISSUE_ID = "EXTRA_ISSUE_ID";

    private static class IssueLoader extends CachedLoader<Issue> {

        private int issueId;

        public IssueLoader(Context context, int issueId) {
            super(context);
            this.issueId = issueId;
        }

        @Override
        public Issue loadInBackground() {
            return ServerCaller.from(getContext()).loadIssue(issueId);
        }
    }

    private static class MessagesAdapter extends BaseArrayAdapter<Message> {

        public MessagesAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
        }

        @Override
        public void fillView(View view, Message message) {
            TextView messageView = (TextView) view.findViewById(android.R.id.text1);
            TextView senderView = (TextView) view.findViewById(android.R.id.text2);
            messageView.setText(message.getText());
            senderView.setText(message.getSender());
        }
    }

    private int issueId;
    private MessagesAdapter messagesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        issueId = getActivity().getIntent().getIntExtra(EXTRA_ISSUE_ID, -1);
        if (issueId == -1) {
            throw new IllegalStateException("EXTRA_ISSUE_ID wasn't found in intent");
        }
        messagesAdapter = new MessagesAdapter(getActivity());
        setListAdapter(messagesAdapter);
        getLoaderManager().initLoader(0, new Bundle(), this);
        return inflater.inflate(R.layout.fragment_issue_detail, container);
    }

    @Override
    public Loader<Issue> onCreateLoader(int i, Bundle bundle) {
        return new IssueLoader(getActivity(), issueId);
    }

    @Override
    public void onLoadFinished(Loader<Issue> issueLoader, Issue issue) {
        messagesAdapter.setData(issue.messages());
    }

    @Override
    public void onLoaderReset(Loader<Issue> issueLoader) {
        messagesAdapter.setData(null);
    }
}
