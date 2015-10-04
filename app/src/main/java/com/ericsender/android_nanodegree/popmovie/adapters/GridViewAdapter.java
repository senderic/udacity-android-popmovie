package com.ericsender.android_nanodegree.popmovie.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.SerializationUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GridViewAdapter extends CursorAdapter {

    private final String sImgSize;
    private final String sImgUrl;
    private List<MovieGridObj> mGridData;
    private static final String LOG_TAG = GridViewAdapter.class.getSimpleName();
    private static final AtomicInteger count = new AtomicInteger();

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    private String thumbUrl;

    public void setGridData(List<MovieGridObj> gridData) {
        Utils.log(getClass().getSimpleName());
        mGridData = gridData;
        notifyDataSetChanged();
    }

    public GridViewAdapter(Context context, Cursor cursor, int flag) {
        super(context, cursor, flag);
        sImgUrl = context.getString(R.string.tmdb_image_base_url);
        sImgSize = context.getString(R.string.tmdb_image_size);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Utils.log(getClass().getSimpleName());
        int layoutId = R.layout.movie_cell;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Utils.log(getClass().getSimpleName());
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        byte[] bMovieObj = cursor.getBlob(1);
        MovieGridObj movie = (MovieGridObj) SerializationUtils.deserialize(bMovieObj);
        String load = String.format(sImgUrl, sImgSize, movie.poster_path);

        Picasso.with(mContext.getApplicationContext())
                .load(load)
                .placeholder(R.drawable.abc_btn_rating_star_on_mtrl_alpha)
                .error(R.drawable.abc_btn_rating_star_off_mtrl_alpha)
                .resize(550, 775)
                .into(viewHolder.imageView);

    }

    public static class ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.grid_item_image);
        }
    }
}