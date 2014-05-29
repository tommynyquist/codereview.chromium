package com.chrome.codereview.utils;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 28/5/14.
 */
public class MergeExpandableListAdapter extends BaseIDExpandableAdapter {


    private static class ViewTyper {

        ArrayList<Integer> typeSums = new ArrayList<Integer>();

        private ViewTyper() {
            typeSums.add(0);
        }

        void addTypes(int typesCount) {
            typeSums.add(typesCount() + typesCount);
        }

        int typeOf(int adapterId, int viewType) {
            return typeSums.get(adapterId) + viewType;
        }

        int typesCount() {
            return typeSums.get(typeSums.size() - 1);
        }

    }

    private static class Position {
        final BaseExpandableListAdapter adapter;
        final int adapterPos;
        final int group;

        private Position(BaseExpandableListAdapter adapter, int adapterPos, int group) {
            this.adapter = adapter;
            this.adapterPos = adapterPos;
            this.group = group;
        }
    }

    private List<BaseExpandableListAdapter> adapters = new ArrayList<BaseExpandableListAdapter>();
    private ViewTyper groupTyper;
    private ViewTyper childTyper;

    private DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            notifyDataSetInvalidated();
        }
    };

    public MergeExpandableListAdapter(BaseExpandableListAdapter... adapters) {
        groupTyper = new ViewTyper();
        childTyper = new ViewTyper();
        for (BaseExpandableListAdapter adapter: adapters) {
            add(adapter);
        }
    }

    public void add(BaseExpandableListAdapter adapter) {
        groupTyper.addTypes(adapter.getGroupTypeCount());
        childTyper.addTypes(adapter.getChildTypeCount());
        adapter.registerDataSetObserver(observer);
        adapters.add(adapter);
    }

    private Position position(int groupPosition) {
        int i = 0;
        while (groupPosition >= adapters.get(i).getGroupCount()) {
            groupPosition -= adapters.get(i).getGroupCount();
            i++;
        }
        return new Position(adapters.get(i), i, groupPosition);
    }

    @Override
    public int getGroupCount() {
        int sum = 0;
        for (BaseExpandableListAdapter adapter : adapters) {
            sum += adapter.getGroupCount();
        }
        return sum;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Position position = position(groupPosition);
        return position.adapter.getChildrenCount(position.group);
    }

    @Override
    public Object getGroup(int groupPosition) {
        Position position = position(groupPosition);
        return position.adapter.getGroup(position.group);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Position position = position(groupPosition);
        return position.adapter.getChild(position.group, childPosition);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Position position = position(groupPosition);
        return position.adapter.getGroupView(position.group, isExpanded, convertView, parent);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Position position = position(groupPosition);
        return position.adapter.getChildView(position.group, childPosition, isLastChild, convertView, parent);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        Position position = position(groupPosition);
        return position.adapter.isChildSelectable(position.group, childPosition);
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        Position position = position(groupPosition);
        return childTyper.typeOf(position.adapterPos, position.adapter.getChildType(position.group, childPosition));
    }

    @Override
    public int getChildTypeCount() {
        return childTyper.typesCount();
    }

    @Override
    public int getGroupType(int groupPosition) {
        Position position = position(groupPosition);
        return groupTyper.typeOf(position.adapterPos, position.adapter.getGroupType(position.group));
    }

    @Override
    public int getGroupTypeCount() {
        return groupTyper.typesCount();
    }
}
