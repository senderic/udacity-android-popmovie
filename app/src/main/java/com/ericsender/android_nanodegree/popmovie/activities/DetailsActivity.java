package com.ericsender.android_nanodegree.popmovie.activities;

import android.os.Bundle;
import android.view.Window;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;


public class DetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.log();
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        // getSupportActionBar().hide();
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.details_activity_container, new MovieDetailsFragment())
//                    .commit();
//        }
    }
}
