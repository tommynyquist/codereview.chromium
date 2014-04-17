package com.chrome.codereview.requests;

import android.net.Uri;

/**
 * Created by sergeyv on 13/4/14.
 */
public class SearchOptions {

    private static enum CloseState {
        UNKNOWN(0),
        OPEN(1),
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

        Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        Builder closeState(CloseState state) {
            if (state == null) {
                throw new IllegalArgumentException();
            }
            this.closeState = state;
            return this;
        }

        Builder withMessages() {
            this.withMessages = true;
            return this;
        }

        SearchOptions create() {
            return new SearchOptions(owner, closeState, withMessages);
        }

    }

    private final String owner;
    private final CloseState closeState;
    private final boolean withMessages;

    public SearchOptions(String owner, CloseState closeState, boolean withMessages) {
        this.owner = owner;
        this.closeState = closeState;
        this.withMessages = withMessages;
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
    }

}
