package com.chrome.codereview.model;

import com.chrome.codereview.utils.EmailUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sergeyv on 20/4/14.
 */
public class Reviewer {

    private final String email;
    private final String name;
    private final Message.Decoration decoration;

    public Reviewer(String email, Message.Decoration decoration) {
        this.name = EmailUtils.retrieveAccountName(email);
        this.email = email;
        this.decoration = decoration;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public Message.Decoration decoration() {
        return decoration;
    }

    @Override
    public String toString() {
        return name;
    }

    public static List<Reviewer> from(JSONArray reviewerEmails, List<Message> issuesMessages) throws JSONException {
        Map<String, Message.Decoration> reviewerOpinion = new HashMap<String, Message.Decoration>();
        for (Message message : issuesMessages) {
            Message.Decoration opinion = message.decoration();
            if (opinion != null) {
                reviewerOpinion.put(message.senderEmail(), opinion);
            }
        }
        List<Reviewer> reviewers = new ArrayList<Reviewer>(reviewerEmails.length());
        for (int i = 0; i < reviewerEmails.length(); i++) {
            String reviewerEmail = reviewerEmails.getString(i);
            Message.Decoration decoration = reviewerOpinion.get(reviewerEmail);
            reviewers.add(new Reviewer(reviewerEmail, decoration));
        }
        return reviewers;
    }

}
