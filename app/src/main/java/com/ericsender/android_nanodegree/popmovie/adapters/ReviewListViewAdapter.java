package com.ericsender.android_nanodegree.popmovie.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.parcelable.ReviewListObj;

import java.util.List;

/**
 * Created by g56147 on 9/17/2015.
 */
public class ReviewListViewAdapter extends ArrayAdapter<ReviewListObj> {
    private final Context mContext;
    private final int mReviewCellRes;
    private final ListView mReviewListView;
    private List<ReviewListObj> mRowObjs;

    public ReviewListViewAdapter(Context context, int reviewCellRes, List<ReviewListObj> rowObjs, ListView reviewListView) {
        super(context, reviewCellRes, rowObjs);
        mContext = context;
        mReviewCellRes = reviewCellRes;
        mRowObjs = rowObjs;
        mReviewListView = reviewListView;
    }

    @Override
    public int getCount() {
        return mRowObjs.size();
    }

    @Override
    public ReviewListObj getItem(int position) {
        return mRowObjs.get(position);
    }

    @Override
    public int getPosition(ReviewListObj item) {
        return mRowObjs.indexOf(item);
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        final ReviewListObj rev = getItem(position);
//        Log.d(LOG_TAG, String.format("row is being set for position (%d/%d), trailer: (%s)",
//                position, getCount() - 1, trailer.title));
        // Log.v(LOG_TAG, String.format("Full movie list: %s", mRowObjs));
        ViewHolder holder;
        if (row == null || !((ViewHolder) row.getTag()).isSet) {
            row = ((Activity) mContext).getLayoutInflater().inflate(mReviewCellRes, parent, false);
            holder = new ViewHolder();
            holder.contentText = (TextView) row.findViewById(R.id.review_content);
            holder.authorText = (TextView) row.findViewById(R.id.review_author);
            holder.contentText.setText(rev.content);
            holder.authorText.setText(rev.author);
            holder.isSet = rev != null;
            holder.authorText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rev.url)));
                }
            });
            row.setTag(holder);
        } else
            holder = (ViewHolder) row.getTag();


        return row;
    }

    public void setData(List<ReviewListObj> data) {
        int v = data.isEmpty() ? View.GONE : View.VISIBLE;
        mReviewListView.setVisibility(v);
        mRowObjs = data;
        notifyDataSetChanged();

    }

    private static class ViewHolder {
        boolean isSet;
        TextView contentText;
        TextView authorText;
    }
}
