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
        //Utils.log(getClass().getSimpleName());
        if (checkRowAndObj(row, position)) {
            final ReviewListObj rev = getItem(position);
            ViewHolder holder = new ViewHolder();
            holder.reviewHashCode = rev.hashCode();
            row = ((Activity) mContext).getLayoutInflater().inflate(mReviewCellRes, parent, false);
            holder.contentText = (TextView) row.findViewById(R.id.review_content);
            holder.authorText = (TextView) row.findViewById(R.id.review_author);
            holder.contentText.setText(rev.content);
            holder.authorText.setText(rev.author);
            holder.position = position;
            holder.authorText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rev.url)));
                }
            });
            row.setTag(holder);
        }
        return row;
    }

    private boolean checkRowAndObj(View row, int position) {
        if (row != null) {
            ViewHolder vh = (ViewHolder) row.getTag();
            return vh.position != position || vh.reviewHashCode != getItem(position).hashCode();
        } else
            return true;
    }

    public void setData(List<ReviewListObj> data) {
        mRowObjs = data;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        int position = -1;
        TextView contentText;
        TextView authorText;
        int reviewHashCode;
    }
}
