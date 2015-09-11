package com.ericsender.android_nanodegree.popmovie.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ericsender.android_nanodegree.popmovie.Application.STATE.REFRESH_GRID;

public class MovieDetailsFragment extends Fragment {
    private TextView titleTextView;
    private ImageView imageView;
    private TextView yearTextView;
    private TextView mDurationTextView;
    private TextView ratingTextView;
    private TextView overviewTextView;

    private MovieGridObj mMovieObj;
    private ProgressBar mDurationProgress;
    private View mRootView;
    private boolean isLoadFinished;
    private Button mFavButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isLoadFinished = false;
        mRootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

        boolean success = handleFirst();
        if (success) {
            handleOffThread();
            isLoadFinished = handleLast();
        } else throw new RuntimeException("handleFirst() returned false.");

        return mRootView;
    }

    public MovieDetailsFragment() {
    }

    private void handleOffThread() {
        getMoreMovieDetails();
    }

    private void getMoreMovieDetails() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        Uri builtUri = Uri.parse(getString(R.string.tmdb_api_base_movie_url) + mMovieObj.id).buildUpon()
                .appendQueryParameter(getString(R.string.tmdb_param_api), getString(R.string.private_tmdb_api))
                .build();
        String url = "";
        try {
            url = new URL(builtUri.toString()).toString();
        } catch (MalformedURLException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            return;
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DetailsActivity", "Response received.");
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
                        Toast.makeText(getActivity(), "Error connecting to server.", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(jsObjRequest);
        Log.d(getClass().getSimpleName(), "getMoreMovieDetails() - url = " + url);// .substring(0, url.length() - 16));
    }

    private boolean handleFirst() {
        mMovieObj = getActivity().getIntent().getParcelableExtra("movieObj");
        imageView = (ImageView) mRootView.findViewById(R.id.movie_thumb);
        mDurationTextView = (TextView) mRootView.findViewById(R.id.movie_duration);
        mDurationProgress = (ProgressBar) mRootView.findViewById(R.id.movie_duration_progressBar);
        if (Utils.isTablet(getActivity())) imageView.setAdjustViewBounds(true);
        Picasso.with(getActivity().getApplicationContext()).load(getString(R.string.tmdb_image_base_url) + getString(R.string.tmdb_image_size) + mMovieObj.poster_path)
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
                Toast.makeText(getActivity(), Utils.f("Added %s to Favorites", mMovieObj.title),
                        Toast.LENGTH_SHORT).show();
            } else {
                // remove from favorites?
                getActivity().getContentResolver().delete(MovieContract.FavoriteEntry.buildFavoriteUri(),
                        MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{mMovieObj.id.toString()});
                Toast.makeText(getActivity(), Utils.f("Removed %s from Favorites", mMovieObj.title),
                        Toast.LENGTH_SHORT).show();
            }

            c.close();
        } else
            Toast.makeText(getActivity(), "Please Wait... still loading", Toast.LENGTH_SHORT).show();

        PopMoviesApplication Me = ((PopMoviesApplication) getActivity().getApplication());
        AtomicBoolean o = (AtomicBoolean) Me.getStateManager().getState(REFRESH_GRID);
        o.set(true);
    }
}
