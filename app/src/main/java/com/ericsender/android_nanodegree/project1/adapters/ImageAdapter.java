package com.ericsender.android_nanodegree.project1.adapters;

import android.content.Context;
import android.graphics.Movie;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.ericsender.android_nanodegree.project1.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends ArrayAdapter<MovieObj> {
    private final List<MovieObj> mMovies;
    private final Context mContext;
    private static final String LOG_TAG = ImageAdapter.class.getSimpleName();
    private final int mResource;

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    private String thumbUrl;

    public ImageAdapter(Context context, int resource, List<MovieObj> movies) {
        super(context, resource, movies);
        mContext = context;
        mMovies = movies;
        mResource = resource;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public MovieObj getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieObj movie = getItem(position);
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            // convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_list_fragment, parent, false);
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg").into(imageView);
        // imageView.setImageResource(mThumbIds[position]);
        // return (ImageView) parent.view
        return imageView;
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


}