package com.ericsender.android_nanodegree.popmovie.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.ericsender.android_nanodegree.popmovie.parcelable.ReviewListObj;

import java.util.List;

/**
 * Created by g56147 on 9/17/2015.
 */
public class ReviewListViewAdapter extends ArrayAdapter<ReviewListObj> {
    private List<ReviewListObj> rowData;

    public ReviewListViewAdapter(Context context, int resource, List<ReviewListObj> objects) {
        super(context, resource, objects);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public ReviewListObj getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(ReviewListObj item) {
        return super.getPosition(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    public void setRowData(List<ReviewListObj> rowData) {
        this.rowData = rowData;
    }
}
