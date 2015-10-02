package com.ericsender.android_nanodegree.popmovie.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.ericsender.android_nanodegree.popmovie.data.MovieProvider;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.parcelable.ReviewListObj;
import com.ericsender.android_nanodegree.popmovie.parcelable.TrailerListObj;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    public static final String MOVIE_ID_KEY = "movie_id_key";
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
    private Long mMovieId = Long.MIN_VALUE;
    private boolean mIsAlreadyFav = false;
    private static final AtomicBoolean isInit = new AtomicBoolean();
    private static String sIsAlreadyFav;
    private static String sMovieObjKey;
    private static String sMovieIdKey;
    private static String sVideoUrl;
    private static String sParamApi;
    private static String sApiKey;
    private static String sReviewKey;
    private static String sBaseUrl;
    private static String sImgSize;
    private static String sNoLongerFav;
    private static String sImgUrl;
    private static String sTrailerTitle;
    private static String sYoutubeUrl;
    private static final UriMatcher sUriMatcher = MovieProvider.buildUriMatcher();
    private static volatile PopMoviesApplication.State appState;
    private ListView mTrailerListView;
    private ListView mReviewListView;
    private List<TrailerListObj> mTrailerList = new ArrayList<>();
    private List<ReviewListObj> mReviewList = new ArrayList<>();
    private TrailerListViewAdapter mTrailerListViewAdapter;
    private ReviewListViewAdapter mReviewListViewAdapter;
    private LinearLayout mMovieDetailsAsyncView;
    private LinearLayout mMovieDetailsTrailerView;
    private LinearLayout mMovieDetailsReviewView;
    private final Object sync = new Object();
    private LinearLayout.LayoutParams mMovieDetailsReviewViewDefaultLayout;
    private LinearLayout.LayoutParams mMovieDetailsTrailerViewDefaultLayout;
    private RelativeLayout mMovieDetailsBodyView;
    private LinearLayout.LayoutParams mMovieDetailsBodyViewDefaultLayout;
    private LinearLayout.LayoutParams mMovieDetailsAsyncViewDefaultLayout;
    private TextView mMovieDetailsTitleView;
    private LinearLayout.LayoutParams mMovieDetailsTitleViewDefaultLayout;
    private TrailerListObj oFirstTrailer = null;
    private ShareActionProvider mShareActionProvider;
    private RequestQueue mVolleyRequestQueue;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(sMovieObjKey, mMovieObj);
        outState.putLong(sMovieIdKey, mMovieId);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVolleyRequestQueue = Volley.newRequestQueue(getActivity());
        staticInits();
        setHasOptionsMenu(true);
        if (savedInstanceState != null) synchronized (mMovieId) {
            mMovieObj = (MovieGridObj) savedInstanceState.getParcelable(sMovieObjKey);
            mMovieId = savedInstanceState.getLong(sMovieIdKey);
            try {
                mMovieId.notifyAll();
            } catch (IllegalMonitorStateException x) {
            }
        }
        runFragment();
    }

    // Limit use of getString since seeing a random null pointer crash regarding one of them.
    private void staticInits() {
        synchronized (MovieDetailsFragment.class) {
            if (!isInit.get()) {
                appState = ((PopMoviesApplication) getActivity().getApplication()).STATE;
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
                sYoutubeUrl = getString(R.string.youtube_url);
                isInit.set(true);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mIsLoadFinished = false;
        mRootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        mDurationProgress = (ProgressBar) mRootView.findViewById(R.id.movie_duration_progressBar);
        mDurationProgress.setVisibility(View.VISIBLE);
        mProgress = (FrameLayout) mRootView.findViewById(R.id.movie_details_progress_bar);
        imageView = (ImageView) mRootView.findViewById(R.id.movie_thumb);
        mDurationTextView = (TextView) mRootView.findViewById(R.id.movie_duration);
        titleTextView = (TextView) mRootView.findViewById(R.id.movie_details_top_title);
        yearTextView = (TextView) mRootView.findViewById(R.id.movie_year);
        //yearTextView.setText("");
        ratingTextView = (TextView) mRootView.findViewById(R.id.movie_rating);
        overviewTextView = (TextView) mRootView.findViewById(R.id.movie_overview);
        //overviewTextView.setText("");
        mTrailerListView = (ListView) mRootView.findViewById(R.id.list_trailers);
        mReviewListView = (ListView) mRootView.findViewById(R.id.list_reviews);
        mMovieDetailsBodyView = (RelativeLayout) mRootView.findViewById(R.id.movie_details_body);
        mTrailerListViewAdapter = new TrailerListViewAdapter(getActivity(), R.layout.trailer_cell, mTrailerList, mTrailerListView);
        mReviewListViewAdapter = new ReviewListViewAdapter(getActivity(), R.layout.review_cell, mReviewList, mReviewListView);
        mTrailerListView.setAdapter(mTrailerListViewAdapter);
        mReviewListView.setAdapter(mReviewListViewAdapter);
        // TODO: when code is more hardened, maybe move this to the XML?
        mMovieDetailsTitleView = (TextView) mRootView.findViewById(R.id.movie_details_top_title);
        mMovieDetailsAsyncView = (LinearLayout) mRootView.findViewById(R.id.movie_details_async_section);
        mMovieDetailsReviewView = (LinearLayout) mRootView.findViewById(R.id.movie_details_review_section);
        mMovieDetailsTrailerView = (LinearLayout) mRootView.findViewById(R.id.movie_details_trailer_section);
        // runFragment();
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMovieDetailsAsyncView.setVisibility(View.GONE);
        mMovieDetailsTrailerView.setVisibility(View.GONE);
        mMovieDetailsReviewView.setVisibility(View.GONE);
        LinearLayout.LayoutParams foo =
                mMovieDetailsBodyViewDefaultLayout =
                        (LinearLayout.LayoutParams) mMovieDetailsBodyView.getLayoutParams();
        mMovieDetailsBodyView.setLayoutParams(new LinearLayout.LayoutParams(foo.width, foo.height, foo.weight * 2));

        mFavButton = (Button) mRootView.findViewById(R.id.button_mark_fav);
        mFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFavoriteClick(v);
            }
        });

        if (Utils.isTablet(getActivity())) imageView.setAdjustViewBounds(true);
    }

    private void runFragment() {
        if (mMovieId == Long.MIN_VALUE) synchronized (mMovieId) {
            Bundle args = getArguments();
            mMovieId = args == null ?
                    getActivity().getIntent().getLongExtra(sMovieIdKey, Long.MIN_VALUE) :
                    args.getLong(sMovieIdKey, Long.MIN_VALUE);
            try {
                mMovieId.notifyAll();
            } catch (IllegalMonitorStateException x) {
            }
        }
        getMoreMovieDetails();
    }

    private void getMoreMovieDetails() {
        loaderDetails();
        loaderVideoData();
        loaderReviewData();
        loaderVideoData();
        loaderMinutesData();
    }

    private static enum TYPES {
        review, trailer, minute, details;
        public static final String KEY = "type_key";
    }

    private void loaderDetails() {
        makeBundleAndLoad(TYPES.details);
    }

    private void loaderVideoData() {
        makeBundleAndLoad(TYPES.trailer);
        // mVolleyRequestQueue.add(getVideoDataAsync());
    }

    private void loaderReviewData() {
        makeBundleAndLoad(TYPES.review);
        // mVolleyRequestQueue.add(getReviewDataAsync());
    }

    private void loaderMinutesData() {
        makeBundleAndLoad(TYPES.minute);
        // mVolleyRequestQueue.add(getMinutesDataAsync());
    }

    private void makeBundleAndLoad(TYPES type) {
        Bundle b = new Bundle();
        b.putLong(sMovieIdKey, mMovieId);
        getLoaderManager().initLoader(type.ordinal(), b, this);
    }

    private boolean handleMovieObjData(byte[] data) {
        mMovieObj = SerializationUtils.deserialize(data);
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
        getActivity().setProgressBarIndeterminateVisibility(false);
        mProgress.setVisibility(View.GONE);
        return true;
    }

    public void handleFavoriteClick(View view) {
        if (mIsLoadFinished) {
            // check if its already pressed
            Cursor c = getActivity().getContentResolver().query(MovieContract.FavoriteEntry.buildUri(mMovieObj.id),
                    null, null, null, null);
            if (!c.moveToFirst()) {
                // add to favorites
                ContentValues cv = new ContentValues();
                cv.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID, mMovieObj.id);
                Uri u = getActivity().getContentResolver().insert(MovieContract.FavoriteEntry.buildUri(), cv);
                mIsAlreadyFav = true;
                mFavButton.setText(sIsAlreadyFav);
                Snackbar.make(mRootView, String.format("%s %s to Favorites", "Added", mMovieObj.title),
                        Snackbar.LENGTH_SHORT).show();
            } else {
                // remove from favorites?
                getActivity().getContentResolver().delete(MovieContract.FavoriteEntry.buildUri(),
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

        appState.setIsRefreshGrid(true);
    }

    private void updateTrailerDataOrAskServer(Cursor data) {
        byte[] bTrailer = data == null ? null : data.getBlob(1);
        if (bTrailer == null || bTrailer.length == 0) getVideoDataAsync();
        else
            handleTrailerResults((List<LinkedHashMap<String, String>>) SerializationUtils.deserialize(bTrailer));
    }

    private void updateReviewsDataOrAskServer(Cursor data) {
        byte[] bReview = data == null ? null : data.getBlob(1);
        if (bReview == null || bReview.length == 0) getReviewDataAsync();
        else
            handleReviewResults((List<LinkedHashMap<String, String>>) SerializationUtils.deserialize(bReview));
    }

    private void updateMinutesDataOrAskServer(Cursor data) {
        int minutes = data == null ? -1 : data.getInt(1);
        if (minutes <= 0) getMinutesDataAsync();
        else loaderMinutesData();
    }

    private void getVideoDataAsync() {
        blockUntilMovieIdSet();
        Uri builtUri = Uri.parse(String.format(sVideoUrl, mMovieId)).buildUpon()
                .appendQueryParameter(sParamApi, sApiKey)
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
                        Log.d("DetailsActivity", "Video Response received.");
                        LinkedTreeMap<String, Object> map = Utils.getGson().fromJson(response.toString(), LinkedTreeMap.class);
                        try {
                            List<LinkedHashMap<String, String>> results = (ArrayList<LinkedHashMap<String, String>>) map.get("results");
                            handleTrailerResults(results);
                        } catch (NumberFormatException | NullPointerException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getClass().getSimpleName(), error.getMessage(), error);
                        Snackbar.make(mRootView, "Error connecting to server.", Snackbar.LENGTH_SHORT).show();
                    }
                });
        mVolleyRequestQueue.add(jsObjRequest);
    }

    private void getReviewDataAsync() {
        blockUntilMovieIdSet();
        Uri builtUri = Uri.parse(String.format(sReviewKey, mMovieId)).buildUpon()
                .appendQueryParameter(sParamApi, sApiKey)
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
                        Log.d("DetailsActivity", "Review Response received.");
                        LinkedTreeMap<String, Object> map = Utils.getGson().fromJson(response.toString(), LinkedTreeMap.class);
                        try {
                            List<LinkedHashMap<String, String>> results = (ArrayList<LinkedHashMap<String, String>>) map.get("results");
                            handleReviewResults(results);
                        } catch (NumberFormatException | NullPointerException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getClass().getSimpleName(), error.getMessage(), error);
                        Snackbar.make(mRootView, "Error connecting to server.", Snackbar.LENGTH_SHORT).show();
                    }
                });
        mVolleyRequestQueue.add(jsObjRequest);
    }

    private void blockUntilMovieIdSet() {
        if (mMovieId == Long.MIN_VALUE)
            synchronized (mMovieId) {
                try {
                    mMovieId.wait();
                } catch (InterruptedException e) {
                }
            }
    }

    private void handleMinutesResults(String rt) {
        mDurationProgress.setVisibility(View.GONE);
        mDurationTextView.setText(Double.valueOf(rt).intValue() + " mins");
        updateMinutesDataInternal(rt);
    }

    private void handleTrailerResults(List<LinkedHashMap<String, String>> results) {
        Set<TrailerListObj> th = new LinkedHashSet<>();
        for (LinkedHashMap<String, String> r : results) {
            String title = r.get("name");
            String youtube_key = r.get("key");
            th.add(new TrailerListObj(youtube_key, title));
        }
        mTrailerList.clear();
        mTrailerList.addAll(th);
        mTrailerListViewAdapter.setData(mTrailerList);
        setFirstTrailer();
        if (!mTrailerList.isEmpty() && mMovieDetailsTrailerView.getVisibility() == View.GONE)
            showMovieDetailsAsyncView(Section.TRAILER);
        updateTrailerDataInternal((Serializable) results);
    }

    private void handleReviewResults(List<LinkedHashMap<String, String>> results) {
        Set<ReviewListObj> rev = new LinkedHashSet<>();
        for (LinkedHashMap<String, String> r : results) {
            String content = r.get("content");
            String author = r.get("author");
            String url = r.get("url");
            rev.add(new ReviewListObj(content, author, url));
        }
        mReviewList.clear();
        mReviewList.addAll(rev);
        mReviewListViewAdapter.setData(mReviewList);
        if (!mReviewList.isEmpty() && mMovieDetailsReviewView.getVisibility() == View.GONE)
            showMovieDetailsAsyncView(Section.REVIEW);
        updateReviewDataInternal((Serializable) results);
    }

    private void updateReviewDataInternal(Serializable results) {
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{mMovieId.toString()};
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_REVIEWS, SerializationUtils.serialize(results));
        getActivity().getContentResolver().update(MovieContract.MovieEntry.buildUriReviews(mMovieId), cv, selection, selectionArgs);
    }

    private void updateTrailerDataInternal(Serializable results) {
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{mMovieId.toString()};
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS, SerializationUtils.serialize(results));
        getActivity().getContentResolver().update(MovieContract.MovieEntry.buildUriTrailers(mMovieId), cv, selection, selectionArgs);
    }

    private void updateMinutesDataInternal(String minutes) {
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{mMovieId.toString()};
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_MINUTES, Double.valueOf(minutes).intValue());
        getActivity().getContentResolver().update(MovieContract.MovieEntry.buildUriMinutes(mMovieId), cv, selection, selectionArgs);
    }

    // TODO: if only one of the layouts (review xor trailer) show up, alter gravity to reduce white space

    /**
     * Synchronized to ensure this method is running at only one given time for a given fragment.
     *
     * @param section - Review or Trailer section
     */
    private synchronized void showMovieDetailsAsyncView(Section section) {
        if (mMovieDetailsAsyncView.getVisibility() == View.GONE) {
            mMovieDetailsAsyncView.setVisibility(View.VISIBLE);
            mMovieDetailsBodyView.setLayoutParams(mMovieDetailsBodyViewDefaultLayout);
            mMovieDetailsTitleViewDefaultLayout = (LinearLayout.LayoutParams) mMovieDetailsTitleView.getLayoutParams();
            mMovieDetailsAsyncViewDefaultLayout = (LinearLayout.LayoutParams) mMovieDetailsAsyncView.getLayoutParams();
            mMovieDetailsReviewViewDefaultLayout = (LinearLayout.LayoutParams) mMovieDetailsReviewView.getLayoutParams();
            mMovieDetailsTrailerViewDefaultLayout = (LinearLayout.LayoutParams) mMovieDetailsTrailerView.getLayoutParams();
        }

        switch (section) {
            case REVIEW:
                mMovieDetailsReviewView.setVisibility(View.VISIBLE);
                if (mMovieDetailsTrailerView.getVisibility() == View.GONE)
                    setAsynFieldToFillWeight(mMovieDetailsReviewViewDefaultLayout, mMovieDetailsReviewView);
                else
                    setAsyncSectionToDefaults();
                break;
            case TRAILER:
                mMovieDetailsTrailerView.setVisibility(View.VISIBLE);
                if (mMovieDetailsReviewView.getVisibility() == View.GONE)
                    setAsynFieldToFillWeight(mMovieDetailsTrailerViewDefaultLayout, mMovieDetailsTrailerView);
                else
                    setAsyncSectionToDefaults();
                break;
        }
    }

    private void setAsynFieldToFillWeight(LinearLayout.LayoutParams lp, LinearLayout l) {
        l.setLayoutParams(new LinearLayout.LayoutParams(lp.width, lp.height, lp.weight * 2f));
        mMovieDetailsAsyncView.setLayoutParams(new LinearLayout.LayoutParams(
                mMovieDetailsAsyncViewDefaultLayout.width,
                mMovieDetailsAsyncViewDefaultLayout.height,
                mMovieDetailsAsyncViewDefaultLayout.weight / 2f));
        mMovieDetailsTitleView.setLayoutParams(new LinearLayout.LayoutParams(
                mMovieDetailsTitleViewDefaultLayout.width,
                mMovieDetailsTitleViewDefaultLayout.height,
                mMovieDetailsTitleViewDefaultLayout.weight / 2f));
    }

    private void setAsyncSectionToDefaults() {
        mMovieDetailsReviewView.setLayoutParams(mMovieDetailsReviewViewDefaultLayout);
        mMovieDetailsTrailerView.setLayoutParams(mMovieDetailsTrailerViewDefaultLayout);
        mMovieDetailsAsyncView.setLayoutParams(mMovieDetailsAsyncViewDefaultLayout);
    }

    @NonNull
    private void getMinutesDataAsync() {
        blockUntilMovieIdSet();
        Uri builtUri = Uri.parse(String.format(sBaseUrl, mMovieId)).buildUpon()
                .appendQueryParameter(sParamApi, sApiKey)
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
                        Log.d("DetailsActivity", "Minutes Response received.");
                        LinkedTreeMap<String, Object> map = Utils.getGson().fromJson(response.toString(), LinkedTreeMap.class);
                        try {
                            String rt = map.get("runtime").toString().trim();
                            handleMinutesResults(rt);
                        } catch (NumberFormatException | NullPointerException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
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
        mVolleyRequestQueue.add(jsObjRequest);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        TYPES type = TYPES.values()[id];
        long mid = args.getLong(sMovieIdKey);
        if (mid > 0L)
            switch (type) {
                case review:
                    return new CursorLoader(getActivity(), MovieContract.MovieEntry.buildUriReviews(mid),
                            null, null, null, null);
                case trailer:
                    return new CursorLoader(getActivity(), MovieContract.MovieEntry.buildUriTrailers(mid),
                            null, null, null, null);
                case minute:
                    return new CursorLoader(getActivity(), MovieContract.MovieEntry.buildUriMinutes(mid),
                            null, null, null, null);
                case details:
                    return new CursorLoader(getActivity(), MovieContract.MovieEntry.buildUriUnionFavorite(mid),
                            null, null, null, null);
                default:
                    throw new RuntimeException("Invalid type: " + type);
            }
        else return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Uri uri = ((CursorLoader) loader).getUri();
        if (!data.moveToFirst()) data = null;
        int match = sUriMatcher.match(uri);
        switch (match) {
//            case MovieProvider.MOVIE:
//                break;
//            case MovieProvider.MOVIE_WITH_ID:
//                break;
            case MovieProvider.MOVIE_MINUTES:
                updateMinutesDataOrAskServer(data);
                break;
            case MovieProvider.MOVIE_REVIEWS:
                updateReviewsDataOrAskServer(data);
                break;
            case MovieProvider.MOVIE_TRAILERS:
                updateTrailerDataOrAskServer(data);
                break;
//            case MovieProvider.MOVIE_RATING:
//                break;
//            case MovieProvider.MOVIE_FAVORITE:
//                break;
//            case MovieProvider.MOVIE_FAVORITE_WITH_ID:
//                break;
            case MovieProvider.MOVIE_WITH_ID_AND_MAYBE_FAVORITE:
                if (data == null || !data.moveToFirst())
                    Snackbar.make(mRootView, "No Data Loaded. Please go back and refresh", Snackbar.LENGTH_LONG).show();
                else if (data.getCount() == 2) {
                    mFavButton.setText(sIsAlreadyFav);
                    mIsAlreadyFav = true;
                    data.moveToLast();
                }
                mIsLoadFinished = handleMovieObjData(data.getBlob(1));
                break;
//            case MovieProvider.MOVIE_POPULAR:
//                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("Unknown/Unimplemented match (%s) / uri (%s): ", match, uri));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private enum Section {
        REVIEW, TRAILER, DETAILS;
    }

    private void setFirstTrailer() {
        if (!mTrailerList.isEmpty()) {
            oFirstTrailer = mTrailerList.get(0);
            mShareActionProvider.setShareIntent(createShareYoutubeIntent());
        }
    }

    private Intent createShareYoutubeIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("test/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(sYoutubeUrl, oFirstTrailer.youtube_key));
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share_youtube);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(MovieGridObj movieObj);
    }
}
