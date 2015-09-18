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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.adapters.ReviewListViewAdapter;
import com.ericsender.android_nanodegree.popmovie.adapters.TrailerListViewAdapter;
import com.ericsender.android_nanodegree.popmovie.application.PopMoviesApplication;
import com.ericsender.android_nanodegree.popmovie.data.MovieContract;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.parcelable.ReviewListObj;
import com.ericsender.android_nanodegree.popmovie.parcelable.TrailerListObj;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ericsender.android_nanodegree.popmovie.application.STATE.REFRESH_GRID;

public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
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
    private boolean mIsLoadFinished;
    private Button mFavButton;
    private Long mMovieId;
    private boolean mIsAlreadyFav = false;
    private String sIsAlreadyFav;
    private String sMovieObjKey;
    private String sMovieIdKey;
    private String sVideoUrl;
    private String sParamApi;
    private String sApiKey;
    private String sReviewKey;
    private String sBaseUrl;
    private String sImgSize;
    private String sNoLongerFav;
    private String sImgUrl;
    private String sTrailerTitle;
    private ListView mTrailerListView;
    private ListView mReviewListView;
    private List<TrailerListObj> mTrailerList = new ArrayList<>();
    private List<ReviewListObj> mReviewList = new ArrayList<>();
    private TrailerListViewAdapter mTrailerListViewAdapter;
    private ReviewListViewAdapter mReviewListViewAdapter;
    private LinearLayout mMovieDetailsAsyncView;
    private RelativeLayout mMovieDetailsBodyView;
    private int mMovieDetailsBodyHeight;
    private int mMovieDetailsBodyWidth;
    private RelativeLayout.LayoutParams mMovieDetailsInitialBodyLayout;
    private RelativeLayout mMovieDetailsTrailerView;
    private RelativeLayout mMovieDetailsReviewView;
    private final AtomicBoolean isMovieDetailsAsycSectionNeeded = new AtomicBoolean();
    private final AtomicBoolean isMovieDetailTrailersNeeded = new AtomicBoolean();
    private final AtomicBoolean isMovieDetailReviewsNeeded = new AtomicBoolean();
    private int mMovieDetailsReviewViewWidth;
    private int mMovieDetailsReviewViewHeight;
    private int mMovieDetailsTrailerViewWidth;
    private int mMovieDetailsTrailerViewHeight;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(sMovieObjKey, mMovieObj);
        outState.putLong(sMovieIdKey, mMovieId);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setHasOptionsMenu(false);
    }

    // Limit use of getString since seeing a random null pointer crash regarding one of them.
    private void init() {
        sIsAlreadyFav = getString(R.string.is_already_fav);
        sMovieObjKey = getString(R.string.movie_obj_key);
        sMovieIdKey = getString(R.string.movie_id_key);
        sVideoUrl = getString(R.string.tmdb_api_movie_videos_url);
        sParamApi = getString(R.string.tmdb_param_api);
        sApiKey = getString(R.string.private_tmdb_api);
        sReviewKey = getString(R.string.tmdb_api_movie_review_url);
        sBaseUrl = getString(R.string.tmdb_api_base_movie_url);
        sImgUrl = getString(R.string.tmdb_image_base_url);
        sImgSize = getString(R.string.tmdb_image_size);
        sNoLongerFav = getString(R.string.is_no_longer_fav);
        sTrailerTitle = getString(R.string.trailer_title_iter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mIsLoadFinished = false;
        mRootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        mProgress = (FrameLayout) mRootView.findViewById(R.id.movie_details_progress_bar);
        imageView = (ImageView) mRootView.findViewById(R.id.movie_thumb);
        mDurationTextView = (TextView) mRootView.findViewById(R.id.movie_duration);
        mDurationProgress = (ProgressBar) mRootView.findViewById(R.id.movie_duration_progressBar);
        titleTextView = (TextView) mRootView.findViewById(R.id.movie_details_top_title);
        yearTextView = (TextView) mRootView.findViewById(R.id.movie_year);
        ratingTextView = (TextView) mRootView.findViewById(R.id.movie_rating);
        overviewTextView = (TextView) mRootView.findViewById(R.id.movie_overview);
        mTrailerListView = (ListView) mRootView.findViewById(R.id.list_trailers);
        mReviewListView = (ListView) mRootView.findViewById(R.id.list_reviews);
        mTrailerListViewAdapter = new TrailerListViewAdapter(getActivity(), R.layout.trailer_cell, mTrailerList, mTrailerListView);
        mReviewListViewAdapter = new ReviewListViewAdapter(getActivity(), R.layout.review_cell, mReviewList, mReviewListView);
        mTrailerListView.setAdapter(mTrailerListViewAdapter);
        mReviewListView.setAdapter(mReviewListViewAdapter);
        // TODO: when code is more hardened, maybe move this to the XML?
        mMovieDetailsAsyncView = (LinearLayout) mRootView.findViewById(R.id.movie_details_async_section);
        mMovieDetailsBodyView = (RelativeLayout) mRootView.findViewById(R.id.movie_details_body);
        mMovieDetailsReviewView = (RelativeLayout) mRootView.findViewById(R.id.movie_details_review_section);
        mMovieDetailsTrailerView = (RelativeLayout) mRootView.findViewById(R.id.movie_details_trailer_section);
        mMovieDetailsAsyncView.setVisibility(View.GONE);
        mMovieDetailsTrailerView.setVisibility(View.GONE);
        mMovieDetailsReviewView.setVisibility(View.GONE);
        mMovieDetailsBodyView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMovieDetailsBodyWidth = RelativeLayout.LayoutParams.MATCH_PARENT;
                mMovieDetailsBodyHeight = mMovieDetailsBodyView.getLayoutParams().height;
                if (mMovieDetailsBodyHeight != RelativeLayout.LayoutParams.MATCH_PARENT && !isMovieDetailsAsycSectionNeeded.get()) {
                    mMovieDetailsInitialBodyLayout = new RelativeLayout.LayoutParams(mMovieDetailsBodyWidth, mMovieDetailsBodyHeight);
                    mMovieDetailsBodyView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                }
            }
        });

        mFavButton = (Button) mRootView.findViewById(R.id.button_mark_fav);
        mFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFavoriteClick(v);
            }
        });

        if (Utils.isTablet(getActivity())) imageView.setAdjustViewBounds(true);
        // runFragment();
        if (savedInstanceState != null) {
            mMovieObj = (MovieGridObj) savedInstanceState.getParcelable(sMovieObjKey);
            mMovieId = savedInstanceState.getLong(sMovieIdKey);
            runFragment();
        } else {
            mMovieId = getActivity().getIntent().getLongExtra(sMovieIdKey, -1L);
            Bundle b = new Bundle();
            b.putLong(sMovieIdKey, mMovieId);
            getLoaderManager().initLoader(0, b, this);
        }
        return mRootView;
    }

    private void runFragment() {
        handleOffThread();
        mIsLoadFinished = handleLast();
        getActivity().setProgressBarIndeterminateVisibility(false);
        mProgress.setVisibility(View.GONE);
    }


    private void handleFirst() {
        mMovieObj = getMovieObjFromIntent();
    }

    private MovieGridObj getMovieObjFromIntent() {
        return mMovieObj == null ?
                (MovieGridObj) getActivity().getIntent().getParcelableExtra(sMovieObjKey) : mMovieObj;
    }

    private boolean handleLast() {
        // Picasso *should* be caching these poster images, so this call should not require network access

        // Picasso.with(getActivity().getApplicationContext()).setIndicatorsEnabled(true);
        // Picasso.with(getActivity().getApplicationContext()).setLoggingEnabled(true);

        Picasso.with(getActivity().getApplicationContext())
                .load(String.format(sImgUrl, sImgSize, mMovieObj.poster_path))
                .placeholder(R.drawable.blank)
                .error(R.drawable.blank)
                .resize(366, 516)
                .into(imageView);

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
        if (mIsLoadFinished) {
            // check if its already pressed
            Cursor c = getActivity().getContentResolver().query(MovieContract.FavoriteEntry.buildFavoriteUri(mMovieObj.id),
                    null, null, null, null);
            if (!c.moveToFirst()) {
                // add to favorites
                ContentValues cv = new ContentValues();
                cv.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID, mMovieObj.id);
                Uri u = getActivity().getContentResolver().insert(MovieContract.FavoriteEntry.buildFavoriteUri(), cv);
                mIsAlreadyFav = true;
                mFavButton.setText(sIsAlreadyFav);
                Snackbar.make(mRootView, String.format("%s %s to Favorites", "Added", mMovieObj.title),
                        Snackbar.LENGTH_SHORT).show();
            } else {
                // remove from favorites?
                getActivity().getContentResolver().delete(MovieContract.FavoriteEntry.buildFavoriteUri(),
                        MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{mMovieObj.id.toString()});
                mIsAlreadyFav = false;
                mFavButton.setText(sNoLongerFav);
                Snackbar.make(mRootView, String.format("%s %s from Favorites", "Removed", mMovieObj.title),
                        Snackbar.LENGTH_SHORT).show();
            }

            c.close();
        } else
            Snackbar.make(mRootView, "Please Wait... still loading", Snackbar.LENGTH_SHORT).show();

        PopMoviesApplication Me = ((PopMoviesApplication) getActivity().getApplication());
        AtomicBoolean o = (AtomicBoolean) Me.getStateManager().getState(REFRESH_GRID);
        o.set(true);
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
        Uri builtUri = Uri.parse(String.format(sVideoUrl, mMovieObj.id)).buildUpon()
                .appendQueryParameter(sParamApi, sApiKey)
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
                            List<LinkedTreeMap<String, String>> results = (ArrayList<LinkedTreeMap<String, String>>) map.get("results");
                            int count = 0;
                            Set<TrailerListObj> th = new HashSet<>(results.size());
                            for (LinkedTreeMap<String, String> r : results) {
                                String title = String.format(sTrailerTitle, ++count, r.get("name"));
                                String youtube_key = r.get("key");
                                th.add(new TrailerListObj(youtube_key, title));
                            }
                            mTrailerList.clear();
                            mTrailerList.addAll(th);
                            mTrailerListViewAdapter.setData(mTrailerList);
                            Section section = Section.TRAILER;
                            if (!mTrailerList.isEmpty() && !isMovieDetailTrailersNeeded.get())
                                showMovieDetailsAsyncView(section);
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
        Uri builtUri = Uri.parse(String.format(sReviewKey, mMovieObj.id)).buildUpon()
                .appendQueryParameter(sParamApi, sApiKey)
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
                            List<LinkedTreeMap<String, String>> results = (ArrayList<LinkedTreeMap<String, String>>) map.get("results");
                            Set<ReviewListObj> rev = new HashSet<>(results.size());
                            for (LinkedTreeMap<String, String> r : results) {
                                String content = r.get("content");
                                String author = r.get("author");
                                String url = r.get("url");
                                rev.add(new ReviewListObj(content, author, url));
                            }
                            mReviewList.clear();
                            mReviewList.addAll(rev);
                            mReviewListViewAdapter.setData(mReviewList);
                            Section section = Section.REVIEW;
                            if (!mReviewList.isEmpty() && !isMovieDetailReviewsNeeded.get())
                                showMovieDetailsAsyncView(section);
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

    private synchronized void showMovieDetailsAsyncView(Section section) {
        if (!isMovieDetailsAsycSectionNeeded.get()) {
            mMovieDetailsAsyncView.setVisibility(View.VISIBLE);
          // mMovieDetailsInitialBodyLayout.addRule(RelativeLayout.BELOW, R.id.movie_details_top_title);
            mMovieDetailsBodyView.setLayoutParams(mMovieDetailsInitialBodyLayout);
            isMovieDetailsAsycSectionNeeded.set(true);
        }
        RelativeLayout.LayoutParams p;
        switch (section) {
            case REVIEW:
                mMovieDetailsReviewView.setVisibility(View.VISIBLE);
                p = (RelativeLayout.LayoutParams) mMovieDetailsReviewView.getLayoutParams();
                if (!isMovieDetailTrailersNeeded.get()) {
                    mMovieDetailsReviewViewWidth = p.width;
                    mMovieDetailsReviewViewHeight = p.height;
                    p.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    p.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                //    p.addRule(RelativeLayout.BELOW, R.id.movie_details_body);
                } else {
                    RelativeLayout.LayoutParams p2 = (RelativeLayout.LayoutParams) mMovieDetailsTrailerView.getLayoutParams();
                    p2.width = mMovieDetailsTrailerViewWidth;
                    p2.height = mMovieDetailsTrailerViewHeight;
                //    p2.addRule(RelativeLayout.BELOW, R.id.movie_details_trailer_section);
                    mMovieDetailsTrailerView.setLayoutParams(p2);
             //       p.addRule(RelativeLayout.BELOW, R.id.movie_details_trailer_section);
                }
                mMovieDetailsReviewView.setLayoutParams(p);
                isMovieDetailReviewsNeeded.set(true);
                break;
            case TRAILER:
                mMovieDetailsTrailerView.setVisibility(View.VISIBLE);
                p = (RelativeLayout.LayoutParams) mMovieDetailsTrailerView.getLayoutParams();
                //p.addRule(RelativeLayout.BELOW, R.id.movie_details_body);
                if (isMovieDetailReviewsNeeded.get()) {
                    RelativeLayout.LayoutParams p2 = (RelativeLayout.LayoutParams) mMovieDetailsReviewView.getLayoutParams();
                    p2.width = mMovieDetailsReviewViewWidth;
                    p2.height = mMovieDetailsReviewViewHeight;
               //     p2.addRule(RelativeLayout.BELOW, R.id.movie_details_trailer_section);
                    mMovieDetailsReviewView.setLayoutParams(p2);
                } else {
                    mMovieDetailsTrailerViewWidth = p.width;
                    mMovieDetailsTrailerViewHeight = p.height;
                    p.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                    p.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                }
//                mMovieDetailsTrailerView.setLayoutParams(p);
                isMovieDetailTrailersNeeded.set(true);
                break;
        }
    }

    @NonNull
    private JsonObjectRequest getMinutesDataAsync() {
        Uri builtUri = Uri.parse(String.format(sBaseUrl, mMovieObj.id)).buildUpon()
                .appendQueryParameter(sParamApi, sApiKey)
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MovieContract.MovieEntry
                .buildMovieUnionFavoriteUri(args.getLong(sMovieIdKey)),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        handleFirst();
        if (!data.moveToFirst())
            Snackbar.make(mRootView, "No Data Loaded. Please go back and refresh", Snackbar.LENGTH_LONG).show();
        else {
            if (data.getCount() == 2) {
                mFavButton.setText(sIsAlreadyFav);
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

    private enum Section {
        REVIEW, TRAILER, DETAILS;
    }
}
