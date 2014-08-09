package com.chrome.codereview.model;

import android.util.SparseArray;

import java.util.ArrayList;
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

    public void filter(SparseArray<Long> idToModificationTime) {
        incomingReviews = filterList(incomingReviews, idToModificationTime);
        outgoingReviews = filterList(outgoingReviews, idToModificationTime);
        ccReviews = filterList(ccReviews, idToModificationTime);
        recentlyClosed = filterList(recentlyClosed, idToModificationTime);
    }

    private static List<Issue> filterList(List<Issue> list, SparseArray<Long> idToModificationTime) {
        List<Issue> result = new ArrayList<Issue>();
        for (Issue issue : list) {
            long lastSavedModification = idToModificationTime.get(issue.id(), 1l);
            if (issue.lastModified().getTime() > lastSavedModification) {
                result.add(issue);
            }
        }
        return result;
    }
}
