package com.chrome.codereview.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 19/4/14.
 */
public class UserIssues {

    private final List<Issue> incomingReviews;
    private final List<Issue> outgoingReviews;
    private final List<Issue> ccReviews;
    private final List<Issue> recentlyClosed;

    public UserIssues(List<Issue> incoming, List<Issue> mineIssues, List<Issue> ccReviews) {
        this.incomingReviews = incoming;
        this.outgoingReviews = new ArrayList<Issue>();
        this.recentlyClosed = new ArrayList<Issue>();
        for (Issue issue: mineIssues) {
            if (issue.isClosed()) {
                recentlyClosed.add(issue);
            } else {
                outgoingReviews.add(issue);
            }
        }
        this.ccReviews = ccReviews;
    }

    public List<Issue> incomingReviews() {
        return incomingReviews;
    }

    public List<Issue> outgoingReviews() {
        return outgoingReviews;
    }

    public List<Issue> ccReviews() {
        return ccReviews;
    }

    public List<Issue> recentlyClosed() {
        return recentlyClosed;
    }
}
