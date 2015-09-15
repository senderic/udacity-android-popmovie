package com.ericsender.android_nanodegree.popmovie.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ericsender.android_nanodegree.popmovie.Application.PopMoviesApplication;
import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.data.MovieContract;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ericsender.android_nanodegree.popmovie.Application.STATE.REFRESH_GRID;

public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private TextView titleTextView;
    private ImageView imageView;
    private TextView yearTextView;
    private TextView mDurationTextView;
    private TextView ratingTextView;
    private TextView overviewTextView;
    private FrameLayout mProgress;

    private MovieGridObj mMovieObj;
    private ProgressBar mDurationProgress;
    private View mRootView;
    private boolean isLoadFinished;
    private Button mFavButton;
    private Long mMovieId;
    private boolean mIsAlreadyFav = false;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.movie_obj_key), mMovieObj);
        outState.putLong(getString(R.string.movie_id_key), mMovieId);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isLoadFinished = false;
        mRootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        mProgress = (FrameLayout) mRootView.findViewById(R.id.movie_details_progress_bar);
        // runFragment();
        if (savedInstanceState != null) {
            mMovieObj = (MovieGridObj) savedInstanceState.getParcelable(getString(R.string.movie_obj_key));
            mMovieId = savedInstanceState.getLong(getString(R.string.movie_id_key));
            runFragment();
        } else {
            mMovieId = getActivity().getIntent().getLongExtra(getString(R.string.movie_id_key), -1L);
            Bundle b = new Bundle();
            b.putLong(getString(R.string.movie_id_key), mMovieId);
            getLoaderManager().initLoader(0, b, this);
        }
        return mRootView;
    }

    private void runFragment() {
        boolean success = handleFirst();
        if (success) {
            handleOffThread();
            isLoadFinished = handleLast();
            getActivity().setProgressBarIndeterminateVisibility(false);
            mProgress.setVisibility(View.GONE);
        } else throw new RuntimeException("handleFirst() returned false.");
    }


    public MovieDetailsFragment() {
    }

    private void handleOffThread() {
        getMoreMovieDetails();
    }

    private void getMoreMovieDetails() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        queue.add(getVideoDataAsync());
        queue.add(getReviewDataAsync());
        queue.add(getMinutesDataAsync());

        Log.d(getClass().getSimpleName(), String.format("getMoreMovieDetails() - movie %s", mMovieObj.title));// .substring(0, url.length() - 16));
    }

    private JsonObjectRequest getVideoDataAsync() {
        Uri builtUri = Uri.parse(String.format(getString(R.string.tmdb_api_movie_videos_url), mMovieObj.id)).buildUpon()
                .appendQueryParameter(getString(R.string.tmdb_param_api), getString(R.string.private_tmdb_api))
                .build();
        String url = "";
        try {
            url = new URL(builtUri.toString()).toString();
        } catch (MalformedURLException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DetailsActivity", "Video Response received.");
                        LinkedTreeMap<String, Object> map = Utils.getGson().fromJson(response.toString(), LinkedTreeMap.class);
                        try {
                            String rt = map.get("runtime").toString().trim();
//                            mDurationProgress.setVisibility(View.GONE);
//                            mDurationTextView.setText(Double.valueOf(rt).intValue() + " mins");
                        } catch (NumberFormatException | NullPointerException x) {
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getClass().getSimpleName(), error.getMessage(), error);
                        Snackbar.make(mRootView, "Error connecting to server.", Snackbar.LENGTH_SHORT).show();
                    }
                });
        return jsObjRequest;
    }

    private JsonObjectRequest getReviewDataAsync() {
        Uri builtUri = Uri.parse(String.format(getString(R.string.tmdb_api_movie_review_url), mMovieObj.id)).buildUpon()
                .appendQueryParameter(getString(R.string.tmdb_param_api), getString(R.string.private_tmdb_api))
                .build();
        String url = "";
        try {
            url = new URL(builtUri.toString()).toString();
        } catch (MalformedURLException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DetailsActivity", "Review Response received.");
                        LinkedTreeMap<String, Object> map = Utils.getGson().fromJson(response.toString(), LinkedTreeMap.class);
                        try {
                            String rt = map.get("runtime").toString().trim();
//                            mDurationProgress.setVisibility(View.GONE);
//                            mDurationTextView.setText(Double.valueOf(rt).intValue() + " mins");
                        } catch (NumberFormatException | NullPointerException x) {
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getClass().getSimpleName(), error.getMessage(), error);
                        Snackbar.make(mRootView, "Error connecting to server.", Snackbar.LENGTH_SHORT).show();
                    }
                });
        return jsObjRequest;
    }

    @NonNull
    private JsonObjectRequest getMinutesDataAsync() {
        Uri builtUri = Uri.parse(String.format(getString(R.string.tmdb_api_base_movie_url), mMovieObj.id)).buildUpon()
                .appendQueryParameter(getString(R.string.tmdb_param_api), getString(R.string.private_tmdb_api))
                .build();
        String url = "";
        try {
            url = new URL(builtUri.toString()).toString();
        } catch (MalformedURLException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DetailsActivity", "Minutes Response received.");
                        LinkedTreeMap<String, Object> map = Utils.getGson().fromJson(response.toString(), LinkedTreeMap.class);
                        try {
                            String rt = map.get("runtime").toString().trim();
                            mDurationProgress.setVisibility(View.GONE);
                            mDurationTextView.setText(Double.valueOf(rt).intValue() + " mins");
                        } catch (NumberFormatException | NullPointerException x) {
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getClass().getSimpleName(), error.getMessage(), error);
                        mDurationProgress.setVisibility(View.GONE);
                        mDurationTextView.setVisibility(View.GONE);
                        Snackbar.make(mRootView, "Error connecting to server.", Snackbar.LENGTH_SHORT).show();
                    }
                });
        return jsObjRequest;
    }

    private boolean handleFirst() {
        mMovieObj = getMovieObjFromIntent();
        imageView = (ImageView) mRootView.findViewById(R.id.movie_thumb);
        mDurationTextView = (TextView) mRootView.findViewById(R.id.movie_duration);
        mDurationProgress = (ProgressBar) mRootView.findViewById(R.id.movie_duration_progressBar);
        if (Utils.isTablet(getActivity())) imageView.setAdjustViewBounds(true);
        // Picasso *should* be caching these poster images, so this call should not require network access
        // TODO: (bonus if time) confirm picasso is using a cache to get this data by studying network logs?
        Picasso.with(getActivity().getApplicationContext()).load(
                String.format(getString(R.string.tmdb_image_base_url), getString(R.string.tmdb_image_size), mMovieObj.poster_path))
                .placeholder(R.drawable.blank)
                .error(R.drawable.blank)
                .resize(366, 516)
                .into(imageView);

        mFavButton = (Button) mRootView.findViewById(R.id.button_mark_fav);
        mFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFavoriteClick(v);
            }
        });
        return true;
    }

    private MovieGridObj getMovieObjFromIntent() {
        return mMovieObj == null ?
                (MovieGridObj) getActivity().getIntent().getParcelableExtra(getString(R.string.movie_obj_key)) : mMovieObj;
    }

    private boolean handleLast() {
        titleTextView = (TextView) mRootView.findViewById(R.id.movie_title);

        yearTextView = (TextView) mRootView.findViewById(R.id.movie_year);
        ratingTextView = (TextView) mRootView.findViewById(R.id.movie_rating);
        overviewTextView = (TextView) mRootView.findViewById(R.id.movie_overview);

        titleTextView.setText(mMovieObj.title);
        yearTextView.setText(mMovieObj.release_date.substring(0, 4));
        overviewTextView.setText(mMovieObj.overview);
        // TODO is there a String.format that will do %.1f and strip trailing zeros?
        double va = mMovieObj.vote_average.doubleValue();
        String roundRating = va == (long) va
                ?
                String.format("%d", (long) va)
                :
                String.format("%.1f", va);
        ratingTextView.setText(roundRating + "/10");
        return true;
    }

    public void handleFavoriteClick(View view) {
        if (isLoadFinished) {
            // check if its already pressed
            Cursor c = getActivity().getContentResolver().query(MovieContract.FavoriteEntry.buildFavoriteUri(mMovieObj.id),
                    null, null, null, null);
            if (!c.moveToFirst()) {
                // add to favorites
                ContentValues cv = new ContentValues();
                cv.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID, mMovieObj.id);
                Uri u = getActivity().getContentResolver().insert(MovieContract.FavoriteEntry.buildFavoriteUri(), cv);
                mIsAlreadyFav = true;
                mFavButton.setText(getString(R.string.is_already_fav));
                Toast.makeText(getActivity(), String.format("Added %s to Favorites", mMovieObj.title),
                        Toast.LENGTH_SHORT).show();
            } else {
                // remove from favorites?
                getActivity().getContentResolver().delete(MovieContract.FavoriteEntry.buildFavoriteUri(),
                        MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{mMovieObj.id.toString()});
                mIsAlreadyFav = false;
                mFavButton.setText(getString(R.string.is_no_longer_fav));
                Toast.makeText(getActivity(), String.format("Removed %s from Favorites", mMovieObj.title),
                        Toast.LENGTH_SHORT).show();
            }

            c.close();
        } else
            Toast.makeText(getActivity(), "Please Wait... still loading", Toast.LENGTH_SHORT).show();

        PopMoviesApplication Me = ((PopMoviesApplication) getActivity().getApplication());
        AtomicBoolean o = (AtomicBoolean) Me.getStateManager().getState(REFRESH_GRID);
        o.set(true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MovieContract.MovieEntry
                .buildMovieUnionFavoriteUri(args.getLong(getString(R.string.movie_id_key))),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst())
            Snackbar.make(mRootView, "No Data Loaded. Please go back and refresh", Snackbar.LENGTH_LONG).show();
        else {
            if (data.getCount() == 2) {
                mFavButton.setText(getString(R.string.is_already_fav));
                mIsAlreadyFav = true;
                data.moveToLast();
            }
            mMovieObj = (MovieGridObj) SerializationUtils.deserialize(data.getBlob(0));
            runFragment();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
