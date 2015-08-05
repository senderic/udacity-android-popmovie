package com.ericsender.android_nanodegree.project1.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericsender.android_nanodegree.project1.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends ArrayAdapter<MovieObj> {

    private final List<MovieObj> mGridData;
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

    public void setGridData(List<MovieObj> gridData){
        mGridData.clear();
        mGridData.addAll(gridData);
        notifyDataSetChanged();
    }

    public ImageAdapter(Context context, int resource, List<MovieObj> movies) {
        super(context, resource, movies);
        mContext = context;
        mGridData = movies;
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
        ViewHolder holder;
        ImageView imageView;
        if (convertView == null) {
            convertView = ((Activity) mContext).getLayoutInflater().inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.grid_item_title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.grid_item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //MovieObj item = mGridData.get(position);
        holder.titleTextView.setText("TITLE");

        Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg").into(holder.imageView);
        // imageView.setImageResource(mThumbIds[position]);
        // return (ImageView) parent.view
        return convertView;
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