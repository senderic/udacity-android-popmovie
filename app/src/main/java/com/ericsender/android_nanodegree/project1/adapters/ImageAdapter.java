package com.ericsender.android_nanodegree.project1.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericsender.android_nanodegree.project1.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends ArrayAdapter<MovieObj> {

    private final GridView mMovieGrid;
    private List<MovieObj> mGridData;
    private final Context mContext;
    private static final String LOG_TAG = ImageAdapter.class.getSimpleName();
    private final int mResource;
    private final String baseUrl;

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    private String thumbUrl;

    public void setGridData(List<MovieObj> gridData) {
        mGridData = gridData;
        notifyDataSetChanged();
    }

    public ImageAdapter(Context context, int resource, List<MovieObj> movies, GridView mMovieGrid) {
        super(context, resource, movies);
        mContext = context;
        mGridData = movies;
        mResource = resource;
        this.mMovieGrid = mMovieGrid;
        baseUrl = context.getString(R.string.tmdb_image_base_url) + context.getString(R.string.tmdb_image_size);
    }

    public int getCount() {
        return 20; //mMovieGrid.getCount();
    }

    public MovieObj getItem(int position) {
        return mGridData.get(position);
    }

    public long getItemId(int position) {
        return 0; //mMovieGrid.getItemIdAtPosition(position);
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MovieObj movie = null;
        try {
            movie = getItem(position);
        } catch (IndexOutOfBoundsException e) {
        }
        ViewHolder holder;
        ImageView imageView;
        if (row == null || !((ViewHolder) row.getTag()).isSet) {
            row = ((Activity) mContext).getLayoutInflater().inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) row.findViewById(R.id.grid_item_title);
            holder.imageView = (ImageView) row.findViewById(R.id.grid_item_image);
            holder.isSet = true;
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        //MovieObj item = mGridData.get(position);
        holder.titleTextView.setText("TITLE");
        // Picasso.with(getContext()).setLoggingEnabled(true);
        // Picasso.with(mContext).load(baseUrl + movie.poster_path).into(holder.imageView);

        String load = movie == null ? "//127.0.0.1/dev/null" : baseUrl + movie.poster_path;

        Picasso.with(mContext)
                .load(load)
                .placeholder(R.drawable.abc_ratingbar_full_material)
                .error(R.drawable.abc_ratingbar_full_material)
                .into(holder.imageView);

        Log.d(getClass().getSimpleName(), "Loading image: " + load);

        // imageView.setImageResource(mThumbIds[position]);
        // return (ImageView) parent.view

        notifyDataSetChanged();
        return row;
    }

    static class ViewHolder {
        TextView titleTextView;
        ImageView imageView;
        boolean isSet = false;
    }
}