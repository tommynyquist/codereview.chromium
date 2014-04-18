package com.chrome.codereview.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.Collection;

/**
 * Created by sergeyv on 19/4/14.
 */
public abstract class BaseArrayAdapter<T> extends ArrayAdapter<T> {

    private int resource;
    private LayoutInflater inflater;

    public BaseArrayAdapter(Context context, int resource) {
        super(context, 0);
        this.resource = resource;
        inflater = LayoutInflater.from(context);
    }

    public abstract void fillView(View view, T item);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
        }
        fillView(convertView, getItem(position));
        return convertView;
    }

    public void setData(Collection<T> data) {
        if (data == null) {
            clear();
            return;
        }
        addAll(data);
    }
}
