package com.ericsender.android_nanodegree.popmovie.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.application.PopMoviesApplication;
import com.ericsender.android_nanodegree.popmovie.fragments.MovieDetailsFragment;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;

public class MainActivity extends BaseActivity implements MovieDetailsFragment.Callback {


    private static final String DETAILFRAGMENT_TAG = "MDTAG";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private PopMoviesApplication.State appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appState = ((PopMoviesApplication) getApplication()).STATE;
        mTwoPane = findViewById(R.id.fragment_moviedetails_double) != null;
        appState.setTwoPane(mTwoPane);
        Log.d(LOG_TAG, (mTwoPane ? "two" : "single") + " pane mode");
        if (mTwoPane && savedInstanceState == null) {
            appState.setDetailsPaneShown(false);
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_moviedetails_double, new MovieDetailsFragment(), DETAILFRAGMENT_TAG)
//                    .commit();
        } else {
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    public void onItemSelected(MovieGridObj item) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            MovieDetailsFragment fragment = new MovieDetailsFragment();
            args.putLong(MovieDetailsFragment.MOVIE_ID_KEY, item.id);
            fragment.setArguments(args);
            if (!appState.isDetailsPaneShown()) {
                findViewById(R.id.fragment_moviedetails_double)
                        .setLayoutParams(
                                new LinearLayout.LayoutParams(
                                        0, RelativeLayout.LayoutParams.MATCH_PARENT, 4f));

                appState.setDetailsPaneShown(true);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_moviedetails_double, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailsActivity.class);
            ImageView imageView = (ImageView) findViewById(R.id.grid_item_image);

            // Interesting data to pass across are the thumbnail size/location, the
            // resourceId of the source bitmap, the picture description, and the
            // orientation (to avoid returning back to an obsolete configuration if
            // the device rotates again in the meantime)

            int[] screenLocation = new int[2];
            imageView.getLocationOnScreen(screenLocation);

            //Pass the image title and url to DetailsActivity
            // TODO: instead of putting the full movieObj in here, send just the movie_id and use a loader on the fragment to get the rest of the data.
            intent.putExtra("left", screenLocation[0])
                    .putExtra("top", screenLocation[1])
                    .putExtra(getString(R.string.movie_id_key), item.id.longValue())
                    .putExtra(getString(R.string.movie_obj_key), (Parcelable) item);

            //Start details activity
            startActivity(intent);
        }
    }
}