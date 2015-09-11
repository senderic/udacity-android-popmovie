package com.ericsender.android_nanodegree.popmovie.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GridViewAdapter extends ArrayAdapter<MovieGridObj> {

    private final GridView mMovieGrid;
    private List<MovieGridObj> mGridData;
    private final Context mContext;
    private static final String LOG_TAG = GridViewAdapter.class.getSimpleName();
    private static final AtomicInteger count = new AtomicInteger();
    private final int mResource;
    private final String baseUrl;

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    private String thumbUrl;

    public void setGridData(List<MovieGridObj> gridData) {
        mGridData = gridData;
        notifyDataSetChanged();
        // mMovieGrid.setAdapter(this);
    }

    public GridViewAdapter(Context context, int resource, List<MovieGridObj> movies, GridView mMovieGrid) {
        super(context, resource, movies);
        mContext = context;
        mGridData = movies;
        mResource = resource;
        this.mMovieGrid = mMovieGrid;
        baseUrl = context.getString(R.string.tmdb_image_base_url) + context.getString(R.string.tmdb_image_size);
    }

    public int getCount() {
        return mGridData.size() > 0 ? mGridData.size() : 20;
    }

    public MovieGridObj getItem(int position) {
        return mGridData.get(position);
    }

//    public long getItemId(int position) {
//        return mMovieGrid.getItemIdAtPosition(position);
//    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View row, ViewGroup parent) {
        MovieGridObj movie = null;
        try {
            movie = getItem(position);
        } catch (IndexOutOfBoundsException e) {
        }
        ViewHolder holder;
        ImageView imageView;
        if (row == null || !((ViewHolder) row.getTag()).isSet) {
            row = ((Activity) mContext).getLayoutInflater().inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) row.findViewById(R.id.grid_item_image);
            if (Utils.isTablet(mContext)) holder.imageView.setAdjustViewBounds(true);
            holder.isSet = movie != null;
            row.setTag(holder);
            // Log.d(getClass().getSimpleName(), holder.isSet ? "Setting row for " + movie.title : "Setting row for null");
        } else {
            holder = (ViewHolder) row.getTag();
        }

        String load = holder.isSet ? baseUrl + movie.poster_path : "null";
        String title = holder.isSet ? movie.title : "null";

        // Picasso.with(getContext()).setLoggingEnabled(true);
        // Log.d(getClass().getSimpleName(), String.format("%d>> Loading image: %s - %s", count.incrementAndGet(), title, load));

        Picasso.with(mContext.getApplicationContext())
                .load(load)
                .placeholder(R.drawable.abc_btn_rating_star_on_mtrl_alpha)
                .error(R.drawable.abc_btn_rating_star_off_mtrl_alpha)
                .resize(550, 775)
                .into(holder.imageView);

        // imageView.setImageResource(mThumbIds[position]);
        // return (ImageView) parent.view

        return row;
    }

    static class ViewHolder {
        ImageView imageView;
        boolean isSet = false;
    }
}