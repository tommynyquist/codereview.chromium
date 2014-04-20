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

    public enum Opinion {
        LGTM,
        NOT_LGTM,
        NO_OPINION
    }

    private final String email;
    private final String name;
    private final Opinion opinion;

    public Reviewer(String email, Opinion opinion) {
        this.name = EmailUtils.retrieveAccountName(email);
        this.email = email;
        this.opinion = opinion;
    }

    public String name() {
        return name;
    }

    public Opinion opinion() {
        return opinion;
    }

    public static List<Reviewer> from(JSONArray reviewerEmails, List<Message> issuesMessages) throws JSONException {
        Map<String, Opinion> reviewerOpinion = new HashMap<String, Opinion>();
        for (Message message : issuesMessages) {
            Opinion opinion = message.reviewerOpinion();
            if (opinion != Opinion.NO_OPINION) {
                reviewerOpinion.put(message.getSender(), opinion);
            }
        }
        List<Reviewer> reviewers = new ArrayList<Reviewer>(reviewerEmails.length());
        for (int i = 0; i < reviewerEmails.length(); i++) {
            String reviewerEmail = reviewerEmails.getString(i);
            Opinion opinion = reviewerOpinion.get(reviewerEmail);
            reviewers.add(new Reviewer(reviewerEmail, opinion != null ? opinion : Opinion.NO_OPINION));
        }
        return reviewers;
    }

}
