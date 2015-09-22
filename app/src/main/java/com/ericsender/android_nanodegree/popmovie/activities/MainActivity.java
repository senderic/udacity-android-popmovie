package com.ericsender.android_nanodegree.popmovie.activities;

import android.os.Bundle;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.fragments.MovieDetailsFragment;
import com.ericsender.android_nanodegree.popmovie.fragments.MovieListFragment;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            if (false || Utils.isTablet(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.main_activity_container, new MovieListFragment())
                        .add(R.id.details_activity_container, new MovieDetailsFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.main_activity_container, new MovieListFragment())
                        .commit();
            }
        }
    }

}
