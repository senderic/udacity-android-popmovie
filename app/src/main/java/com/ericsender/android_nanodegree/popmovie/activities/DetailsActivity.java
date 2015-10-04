package com.ericsender.android_nanodegree.popmovie.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;


public class DetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.log(getClass().getSimpleName());
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
        Utils.log(getClass().getSimpleName());
        MenuItem share = menu.findItem(R.id.action_share_youtube);
        if (share == null)
            menu.add(Menu.NONE, R.id.action_share_youtube, 3, getString(R.string.share_youtube_menu));
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Utils.log(getClass().getSimpleName());
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
