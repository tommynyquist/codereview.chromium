package com.chrome.codereview.model;

import org.apache.http.HttpEntity;
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

    public PublishData(String message, int id) {
        this.message = message;
        this.id = id;
    }

    public int issueId() {
        return id;
    }

    public List<NameValuePair> toList() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("message", message));
        return nameValuePairs;
    }

}
