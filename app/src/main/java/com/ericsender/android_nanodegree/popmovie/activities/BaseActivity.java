package com.ericsender.android_nanodegree.popmovie.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.squareup.picasso.Picasso;

public class BaseActivity extends ActionBarActivity {

    private SharedPreferences mPrefManager;
    private Preference mPref;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Utils.log(getClass().getSimpleName());
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_delete_db:
                Utils.eraseDatabase(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.log(getClass().getSimpleName());
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);
        // Picasso.with(getApplicationContext()).setLoggingEnabled(true);
    }
}
