package com.chrome.codereview.model;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 27/4/14.
 */
public class PublishData {

    private String message;
    private final int id;
    private String subject;
    private String cc;
    private String reviewers;
    private boolean addAsReviewer = true;
    private boolean sendMail = true;

    public PublishData(int id, String message, String subject, String cc, String reviewers) {
        this.message = message;
        this.id = id;
        this.subject = subject;
        this.cc = cc;
        this.reviewers = reviewers;
    }

    public int issueId() {
        return id;
    }

    private void add(List<NameValuePair> list, String name, String value) {
        list.add(new BasicNameValuePair(name, value != null ? value : ""));
    }

    private void add(List<NameValuePair> list, String name, boolean value) {
        if (value) {
            list.add(new BasicNameValuePair(name, "on"));
        }
    }

    public List<NameValuePair> toList() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("message", message));
        add(nameValuePairs, "subject", subject);
        add(nameValuePairs, "message", message);
        add(nameValuePairs, "send_mail", sendMail);
        add(nameValuePairs, "add_as_reviewer", addAsReviewer);
        add(nameValuePairs, "cc", cc);
        add(nameValuePairs, "reviewers", reviewers);
        return nameValuePairs;
    }

}
