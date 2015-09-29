package com.ericsender.android_nanodegree.popmovie.activities;

import android.os.Bundle;
import android.util.Log;

import com.ericsender.android_nanodegree.popmovie.R;

public class MainActivity extends BaseActivity {


    private static final String DETAILFRAGMENT_TAG = "MDTAG";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTwoPane = findViewById(R.id.activity_main_2pane) != null;
        Log.d(LOG_TAG, (mTwoPane ? "two" : "single") + " pane mode");
//        if (mTwoPane && savedInstanceState == null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.activity_main_2pane, new MovieDetailsFragment(), DETAILFRAGMENT_TAG)
//                    .commit();
//        } else {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.main_activity_container, new MovieListFragment())
//                    .commit();
//        }
    }
}