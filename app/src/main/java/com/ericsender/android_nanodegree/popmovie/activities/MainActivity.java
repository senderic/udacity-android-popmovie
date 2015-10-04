package com.ericsender.android_nanodegree.popmovie.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.animation.ShowAnimation;
import com.ericsender.android_nanodegree.popmovie.application.PopMoviesApplication;
import com.ericsender.android_nanodegree.popmovie.fragments.MovieDetailsFragment;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;

public class MainActivity extends BaseActivity implements MovieDetailsFragment.Callback {


    private static final String DETAILFRAGMENT_TAG = "MDTAG";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    private PopMoviesApplication.State appState;
    private View mMovieDetailsContainer;

    @Override
    public void onBackPressed() {
        Utils.log(getClass().getSimpleName());
        super.onBackPressed();
        if (mMovieDetailsContainer != null &&
                ((LinearLayout.LayoutParams) mMovieDetailsContainer.getLayoutParams()).weight != 0f) {
            Log.d(LOG_TAG, "Back button hit - shrinking details fragment");

            // TODO this animation is instant.. Guessing the 0f weight is short circuiting the animation?
            mMovieDetailsContainer.startAnimation(new ShowAnimation(mMovieDetailsContainer, 0f, 1000L));
            appState.setDetailsPaneShown(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.log(getClass().getSimpleName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appState = ((PopMoviesApplication) getApplication()).STATE;
    }

    @Override
    protected void onResume() {
        Utils.log(getClass().getSimpleName());
        super.onResume();
        mTwoPane = (mMovieDetailsContainer = findViewById(R.id.fragment_moviedetails_double)) != null;
        appState.setTwoPane(mTwoPane);
        Log.d(LOG_TAG, (mTwoPane ? "two" : "single") + " pane mode");
        if (mTwoPane) {
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
        Utils.log(getClass().getSimpleName());
        if (mTwoPane) {
            Bundle args = new Bundle();
            MovieDetailsFragment fragment = new MovieDetailsFragment();
            args.putLong(MovieDetailsFragment.MOVIE_ID_KEY, item.id);
            fragment.setArguments(args);
            if (!appState.isDetailsPaneShown()) {
                mMovieDetailsContainer.startAnimation(new ShowAnimation(mMovieDetailsContainer, 4f, 1000L));
                appState.setDetailsPaneShown(true);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_moviedetails_double, fragment, DETAILFRAGMENT_TAG)
                    .addToBackStack(DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailsActivity.class);

            //Pass the image title and url to DetailsActivity
            // TODO: instead of putting the full movieObj in here, send just the movie_id and use a loader on the fragment to get the rest of the data.
            intent.putExtra(getString(R.string.movie_id_key), item.id.longValue());

            //Start details activity
            startActivity(intent);
        }
    }
}