package com.chrome.codereview.utils;

import android.widget.BaseAdapter;

/**
 * Created by sergeyv on 9/8/14.
 */
public abstract class SwipeListAdapter extends BaseAdapter {

    public abstract boolean isItemSwipable(int position);

    public abstract void remove(int position);

}
