package com.ericsender.android_nanodegree.popmovie.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.parcelable.TrailerListObj;

import java.util.List;

/**
 * Created by g56147 on 9/15/2015.
 */
public class TrailerListViewAdapter extends ArrayAdapter<TrailerListObj> {

    private static final String LOG_TAG = TrailerListViewAdapter.class.getSimpleName();
    private final Context mContext;
    private final int mTrailerCellRes;
    private final ListView mTrailerListView;
    private final String strYouTubeUrl;
    private List<TrailerListObj> mRowObjs;
    private final String strTrailerTitleItr;


    public TrailerListViewAdapter(Context context, int trailerCellRes, List<TrailerListObj> rowObjs, ListView trailerListView) {
        super(context, trailerCellRes, rowObjs);
        mContext = context;
        mTrailerCellRes = trailerCellRes;
        mRowObjs = rowObjs;
        mTrailerListView = trailerListView;
        strTrailerTitleItr = mContext.getString(R.string.trailer_title_iter);
        strYouTubeUrl = mContext.getString(R.string.youtube_url);
    }

    public void setRowData(List<TrailerListObj> mRowObjs) {
        this.mRowObjs = mRowObjs;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        final TrailerListObj trailer = getItem(position);
//        Log.d(LOG_TAG, String.format("row is being set for position (%d/%d), trailer: (%s)",
//                position, getCount() - 1, trailer.title));
        // Log.v(LOG_TAG, String.format("Full movie list: %s", mRowObjs));
        ViewHolder holder;
        if (row == null || !((ViewHolder) row.getTag()).isSet) {
            row = ((Activity) mContext).getLayoutInflater().inflate(mTrailerCellRes, parent, false);
            holder = new ViewHolder();
            holder.playIcon = (ImageView) row.findViewById(R.id.trailer_play);
            holder.trailerTitle = (TextView) row.findViewById(R.id.trailer_name);
            holder.trailerTitle.setText(trailer.title);
            holder.isSet = trailer != null;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(String.format(strYouTubeUrl, trailer.youtube_key))));
                }
            });
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        return row;
    }


    @Override
    public int getCount() {
        return mRowObjs.size();
    }

    @Override
    public TrailerListObj getItem(int position) {
        return mRowObjs.get(position);
    }

    private class ViewHolder {
        boolean isSet;
        TextView trailerTitle;
        ImageView playIcon;
    }
}
