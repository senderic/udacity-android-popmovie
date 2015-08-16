package com.ericsender.android_nanodegree.project1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ericsender.android_nanodegree.project1.fragments.MovieDetailsFragment;
import com.ericsender.android_nanodegree.project1.utils.Utils;
import com.ericsender.android_nanodegree.project1.fragments.MovieListFragment;
import com.ericsender.android_nanodegree.project1.R;

public class MainActivity extends ActionBarActivity {

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
