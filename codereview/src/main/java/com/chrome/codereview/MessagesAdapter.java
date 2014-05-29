package com.chrome.codereview;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chrome.codereview.model.Message;
import com.chrome.codereview.utils.DateUtils;
import com.chrome.codereview.utils.LinearExpandableAdapter;
import com.chrome.codereview.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 29/5/14.
 */
public class MessagesAdapter extends LinearExpandableAdapter {

    private LayoutInflater inflater;
    private Context context;

    private List<Message> messages = new ArrayList<Message>();

    public MessagesAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getGroupCount() {
        return messages.size();
    }

    @Override
    public Message getGroup(int groupPosition) {
        return messages.get(groupPosition);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.message_item, parent, false);
        }
        Message message = getGroup(groupPosition);
        TextView messageView = (TextView) convertView.findViewById(R.id.message_text);
        messageView.setSingleLine(!isExpanded);
        if (message.text().isEmpty()) {
            messageView.setText(R.string.empty_message);
            messageView.setTypeface(null, Typeface.ITALIC);
        } else {
            messageView.setText(isExpanded ? message.text() : message.text().substring(0, Math.min(100, message.text().length())));
            messageView.setTypeface(null, Typeface.NORMAL);
        }
        ViewUtils.setText(convertView, R.id.sender, message.sender());
        ViewUtils.setText(convertView, R.id.date, DateUtils.createAgoText(context, message.date()));
        View stateView = convertView.findViewById(R.id.state);
        if (message.decoration() != null) {
            stateView.setVisibility(View.VISIBLE);
            stateView.setBackgroundColor(message.decoration().color(context));
        } else {
            stateView.setVisibility(View.INVISIBLE);
        }
        ViewUtils.expandView(convertView, isExpanded);
        return convertView;
    }
}
