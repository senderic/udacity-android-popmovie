package com.ericsender.android_nanodegree.project1.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ericsender.android_nanodegree.project1.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends ArrayAdapter<MovieObj> {

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
//        mGridData.clear();
//        mGridData.addAll(gridData);
        mGridData = gridData;
        notifyDataSetChanged();
    }

    public ImageAdapter(Context context, int resource, List<MovieObj> movies) {
        super(context, resource, movies);
        mContext = context;
        mGridData = movies;
        mResource = resource;
        baseUrl = context.getString(R.string.tmdb_image_base_url) + context.getString(R.string.tmdb_image_size);
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public MovieObj getItem(int position) {
        return mGridData.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    static int count = 0;

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MovieObj movie = null;
        try {
            movie = getItem(position);
        } catch (IndexOutOfBoundsException e) {
            // Toast.makeText(mContext, "No grid to show! - " + ++count, Toast.LENGTH_SHORT).show();
        }
        ViewHolder holder;
        ImageView imageView;
        if (row == null) {
            row = ((Activity) mContext).getLayoutInflater().inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) row.findViewById(R.id.grid_item_title);
            holder.imageView = (ImageView) row.findViewById(R.id.grid_item_image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        //MovieObj item = mGridData.get(position);
        holder.titleTextView.setText("TITLE");
        if (movie != null) {
            Picasso.with(getContext()).setLoggingEnabled(true);
            // Picasso.with(mContext).load(baseUrl + movie.poster_path).into(holder.imageView);

            Picasso.with(mContext)
                    .load(baseUrl + movie.poster_path)
                    .placeholder(R.drawable.abc_ratingbar_full_material)
                            .error(R.drawable.abc_ratingbar_full_material)
                            .into(holder.imageView);


            Log.d(getClass().getSimpleName(), "Loading image: " + baseUrl + movie.poster_path);
        } else {
            Picasso.with(getContext()).setLoggingEnabled(true);
            Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185/qARJ35IrJNFzFWQGcyWP4r1jyXE.jpg").into(holder.imageView);

            Log.d(getClass().getSimpleName(), "movie is null");
        }
        // imageView.setImageResource(mThumbIds[position]);
        // return (ImageView) parent.view
        return row;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2,
            R.drawable.sample_2, R.drawable.sample_2
    };

    static class ViewHolder {
        TextView titleTextView;
        ImageView imageView;
    }
}