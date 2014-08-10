package com.chrome.codereview.requests;

import android.net.Uri;

/**
 * Created by sergeyv on 13/4/14.
 */
public class SearchOptions {

    public static enum CloseState {
        UNKNOWN(1),
        OPEN(3),
        CLOSED(2);

        final int value;

        CloseState(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }

    }

    public static class Builder {
        private String owner;
        private CloseState closeState = CloseState.UNKNOWN;
        private boolean withMessages = false;
        private String reviewer;
        private String cc;

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder closeState(CloseState state) {
            if (state == null) {
                throw new IllegalArgumentException();
            }
            this.closeState = state;
            return this;
        }

        public Builder withMessages() {
            this.withMessages = true;
            return this;
        }

        public Builder reviewer(String reviewer) {
            this.reviewer = reviewer;
            return this;
        }

        public Builder cc(String cc) {
            this.cc = cc;
            return this;
        }

        public SearchOptions create() {
            return new SearchOptions(owner, closeState, withMessages, reviewer, cc);
        }

    }

    private final String owner;
    private final CloseState closeState;
    private final boolean withMessages;
    private final String reviewer;
    private final String cc;

    public SearchOptions(String owner, CloseState closeState, boolean withMessages, String reviewer, String cc) {
        this.owner = owner;
        this.closeState = closeState;
        this.withMessages = withMessages;
        this.reviewer = reviewer;
        this.cc = cc;
    }

    public void fillParameters(Uri.Builder builder) {
        if (owner != null) {
            builder.appendQueryParameter("owner", owner);
        }

        if (closeState != CloseState.UNKNOWN) {
            builder.appendQueryParameter("closed", closeState.toString());
        }

        if (withMessages) {
            builder.appendQueryParameter("with_messages", "true");
        }

        if (reviewer != null) {
            builder.appendQueryParameter("reviewer", reviewer);
        }

        if (cc != null) {
            builder.appendQueryParameter("cc", cc);
        }

    }

}
