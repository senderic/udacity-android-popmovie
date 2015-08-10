package com.ericsender.android_nanodegree.project1;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericsender.android_nanodegree.project1.adapters.MovieGridObj;
import com.squareup.picasso.Picasso;


public class MovieDetailsActivity extends ActionBarActivity {
    private TextView titleTextView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_movie_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        MovieGridObj movie = getIntent().getParcelableExtra("movieObj");
        titleTextView = (TextView) findViewById(R.id.title);
        imageView = (ImageView) findViewById(R.id.movie_thumb);

        Picasso.with(this).load(getString(R.string.tmdb_image_base_url) + movie.poster_path).into(imageView);
    }

}
