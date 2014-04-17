package com.chrome.codereview.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 13/4/14.
 */
public class Issue {

    private final String owner;

    private final String subject;
    private final boolean closed;


    private final List<Message> messages;

    public Issue(String owner, String subject, boolean closed, List<Message> messages) {
        this.owner = owner;
        this.subject = subject;
        this.closed = closed;
        this.messages = messages;
    }

    public static Issue fromJSONObject(JSONObject jsonObject) {
        try {
            String owner = jsonObject.getString("owner");
            String subject = jsonObject.getString("subject");
            boolean isClosed = jsonObject.getBoolean("closed");
            List<Message> messages = Message.from(jsonObject.getJSONArray("messages"));
            return new Issue(owner, subject, isClosed, messages);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Issue> fromJSONArray(JSONArray jsonArray) {
        int length = jsonArray.length();
        ArrayList<Issue> result = new ArrayList<Issue>(length);
        for (int i = 0; i < length; i++) {
            try {
                result.add(fromJSONObject(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public String getSubject() {
        return subject;
    }

    public String getOwner() {
        return owner;
    }

    public List<Message> getMessages() {
        return messages;
    }

}
