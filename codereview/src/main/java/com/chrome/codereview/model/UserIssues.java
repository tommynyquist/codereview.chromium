package com.chrome.codereview.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by sergeyv on 19/4/14.
 */
public class UserIssues {

    private List<Issue> incomingReviews;
    private List<Issue> outgoingReviews;
    private List<Issue> ccReviews;
    private List<Issue> recentlyClosed;

    public UserIssues(List<Issue> incoming, List<Issue> mineIssues, List<Issue> ccReviews) {
        this.incomingReviews = incoming;
        this.outgoingReviews = new ArrayList<Issue>();
        this.recentlyClosed = new ArrayList<Issue>();
        for (Issue issue : mineIssues) {
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

    public void filter(HashSet<Integer> ids) {
        incomingReviews = filterList(incomingReviews, ids);
        outgoingReviews = filterList(outgoingReviews, ids);
        ccReviews = filterList(ccReviews, ids);
        recentlyClosed = filterList(recentlyClosed, ids);
    }

    private static List<Issue> filterList(List<Issue> list, HashSet<Integer> ids) {
        List<Issue> result = new ArrayList<Issue>();
        for (Issue issue : list) {
            if (!ids.contains(issue.id())) {
                result.add(issue);
            }
        }
        return result;
    }
}
