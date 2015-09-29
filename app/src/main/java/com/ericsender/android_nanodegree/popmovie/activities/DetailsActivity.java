package com.ericsender.android_nanodegree.popmovie.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.ericsender.android_nanodegree.popmovie.R;


public class DetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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


    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        MenuItem share = menu.findItem(R.id.action_share_youtube);
        if (share == null)
            menu.add(Menu.NONE, R.id.action_share_youtube, 3, getString(R.string.share_youtube_menu));
        return super.onPrepareOptionsPanel(view, menu);
    }


}
