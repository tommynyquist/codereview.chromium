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

        SearchOptions create() {
            return new SearchOptions(owner, closeState);
        }

    }

    private final String owner;
    private final CloseState closeState;

    public SearchOptions(String owner, CloseState closeState) {
        this.owner = owner;
        this.closeState = closeState;
    }

    public void fillParameters(Uri.Builder builder) {
        if (owner != null) {
            builder.appendQueryParameter("owner", owner);
        }

        if (closeState != CloseState.UNKNOWN) {
            builder.appendQueryParameter("closed", closeState.toString());
        }
    }


}
