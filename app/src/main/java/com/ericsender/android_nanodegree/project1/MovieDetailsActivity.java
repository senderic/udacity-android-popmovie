package com.ericsender.android_nanodegree.project1;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericsender.android_nanodegree.project1.adapters.MovieGridObj;
import com.squareup.picasso.Picasso;

import java.util.Date;


public class MovieDetailsActivity extends ActionBarActivity {
    private TextView titleTextView;
    private ImageView imageView;
    private TextView yearTextView;
    private TextView durationTextView;
    private TextView ratingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_movie_details);

        getSupportActionBar().hide();

        MovieGridObj movie = getIntent().getParcelableExtra("movieObj");
        titleTextView = (TextView) findViewById(R.id.movie_title);
        imageView = (ImageView) findViewById(R.id.movie_thumb);
        yearTextView = (TextView) findViewById(R.id.movie_year);
        durationTextView = (TextView) findViewById(R.id.movie_duration);
        ratingTextView = (TextView) findViewById(R.id.movie_rating);

        titleTextView.setText(movie.title);
        yearTextView.setText(movie.release_date);
        durationTextView.setText("0:00:00");
        ratingTextView.setText(movie.vote_average.toString());

        Picasso.with(this).load(getString(R.string.tmdb_image_base_url) + getString(R.string.tmdb_image_size) + movie.poster_path).into(imageView);
    }

}
