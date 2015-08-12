package com.ericsender.android_nanodegree.project1;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericsender.android_nanodegree.project1.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.project1.utils.Utils;
import com.squareup.picasso.Picasso;


public class MovieDetailsActivity extends ActionBarActivity {
    private TextView titleTextView;
    private ImageView imageView;
    private TextView yearTextView;
    private TextView durationTextView;
    private TextView ratingTextView;
    private TextView overviewTextView;

    private MovieGridObj mMovieObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_movie_details);
        getSupportActionBar().hide();
        handleFirst();
        handleLast();
    }

    private void handleFirst() {
        mMovieObj = getIntent().getParcelableExtra("movieObj");
        imageView = (ImageView) findViewById(R.id.movie_thumb);
        if (Utils.isTablet(this)) imageView.setAdjustViewBounds(true);
        Picasso.with(this).load(getString(R.string.tmdb_image_base_url) + getString(R.string.tmdb_image_size) + mMovieObj.poster_path)
                .placeholder(R.drawable.blank)
                .error(R.drawable.blank)
                .resize(366, 516)
                .into(imageView);
    }

    private void handleLast() {
        titleTextView = (TextView) findViewById(R.id.movie_title);

        yearTextView = (TextView) findViewById(R.id.movie_year);
        durationTextView = (TextView) findViewById(R.id.movie_duration);
        ratingTextView = (TextView) findViewById(R.id.movie_rating);
        overviewTextView = (TextView) findViewById(R.id.movie_overview);

        titleTextView.setText(mMovieObj.title);
        yearTextView.setText(mMovieObj.release_date.substring(0, 4));
        durationTextView.setText("0:00:00");
        overviewTextView.setText(mMovieObj.overview);
        ratingTextView.setText(String.format("%.1f/10", mMovieObj.vote_average.doubleValue()));
    }



}
