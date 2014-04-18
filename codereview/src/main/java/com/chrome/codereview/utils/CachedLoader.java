package com.chrome.codereview.utils;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by sergeyv on 19/4/14.
 */
public abstract class CachedLoader<T> extends AsyncTaskLoader<T> {

    private T result;

    public CachedLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (result != null) {
            deliverResult(result);
            return;
        }
        forceLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        result = null;
    }

    @Override
    public void deliverResult(T data) {
        result = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

}
