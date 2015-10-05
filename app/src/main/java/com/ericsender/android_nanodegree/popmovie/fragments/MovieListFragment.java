package com.ericsender.android_nanodegree.popmovie.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.adapters.GridViewAdapter;
import com.ericsender.android_nanodegree.popmovie.application.PopMoviesApplication;
import com.ericsender.android_nanodegree.popmovie.data.MovieContract;
import com.ericsender.android_nanodegree.popmovie.data.MovieDbHelper;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.google.gson.internal.LinkedTreeMap;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private ArrayAdapter<MovieGridObj> mMovieAdapter;
    private List<MovieGridObj> mMovieList = new ArrayList<>();
    private GridViewAdapter mGridViewAdapter;
    private GridView mMovieGridView;
    private String mCurrSortOrder;
    private MovieListFragment mThis;
    private PopMoviesApplication.State appState;
    private int mPosition;
    private RequestQueue mVollRequestQueue;

    private String getApiSortPref() {
        String sort = appState.getCurrSortState();
        if (sort == null)
            appState.setCurrSortState(sort = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .getString(getString(R.string.pref_sort_order_key),
                            getString(R.string.most_popular_val)));

        if (sort.equals(getString(R.string.most_popular_val)))
            return getString(R.string.tmdb_arg_popularity);
        else if (sort.equals(getString(R.string.highest_rated_val)))
            return getString(R.string.tmdb_arg_highestrating);
        else //if (sort.equals(getString(R.string.favorite_val)))
            return getString(R.string.tmdb_arg_favorite);
        // else throw new RuntimeException("Sort order value is not known: " + sort);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Utils.log(getClass().getSimpleName());
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.GRIDVIEW_LIST_KEY), (ArrayList<? extends Parcelable>) mMovieList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_sort);
        Spinner s = (Spinner) MenuItemCompat.getActionView(item);
        SpinnerAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.pref_sort_order_spinner_entries, android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        final String[] values = getResources().getStringArray(R.array.pref_sort_order_spinner_values);

                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            Utils.log(String.format("position %d and id %d", position, id));
                                            if (position > 0) {
                                                appState.setCurrSortState(mCurrSortOrder = values[position]);
                                                handleSort();
                                            }
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {
                                            Utils.log();
                                        }
                                    }
        );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.log(getClass().getSimpleName());
        super.onCreate(savedInstanceState);
        mVollRequestQueue = Volley.newRequestQueue(getActivity());
        appState = ((PopMoviesApplication) getActivity().getApplication()).STATE;
        if (savedInstanceState != null)
            mMovieList = (List<MovieGridObj>) savedInstanceState.get(getString(R.string.GRIDVIEW_LIST_KEY));
        else {
            getActivity().getContentResolver().delete(MovieContract.PopularEntry.buildUri(), null, null);
            getActivity().getContentResolver().delete(MovieContract.RatingEntry.buildUri(), null, null);
        }
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    @Override
    public void onResume() {
        Utils.log(getClass().getSimpleName());
        String foo = getApiSortPref();
        Log.d(getClass().getSimpleName(), "onResume with Sort =  " + foo);
        // If a change to the sort order is seen, resort the gridview and redistplay
        boolean refreshGrid = appState.getIsRefreshGrid();
        Log.d(LOG_TAG, "Forced RefreshGrid status is: " + refreshGrid);
        if (refreshGrid || !foo.equals(mCurrSortOrder)) { // This will also be true on initial loading.
            mCurrSortOrder = foo;
            handleSort();
        }
        super.onResume();
    }

    private void handleSort() {
        Log.d(getClass().getSimpleName(), "Sorting on: " + mCurrSortOrder);
        Bundle b = new Bundle();
        b.putString("sort", mCurrSortOrder);
        getLoaderManager().restartLoader(0, b, this);
        appState.setIsRefreshGrid(false);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Utils.log(getClass().getSimpleName());
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        mMovieGridView = (GridView) rootView.findViewById(R.id.movie_grid);
        mGridViewAdapter = new GridViewAdapter(getActivity(), null, 0);
        mMovieGridView.setAdapter(mGridViewAdapter);
        createGridItemClickCallbacks();
        mThis = this;
        return rootView;
    }

    private void createGridItemClickCallbacks() {
        Utils.log(getClass().getSimpleName());
        //Grid view click event
        mMovieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    byte[] b = cursor.getBlob(1);
                    ((MovieDetailsFragment.Callback) getActivity())
                            .onItemSelected((MovieGridObj) SerializationUtils.deserialize(b));
                } else
                    Toast.makeText(getActivity(), "No Movie Selected!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Utils.log(getClass().getSimpleName());
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.d(LOG_TAG, "Refreshing!");
                Bundle b = new Bundle();
                b.putString("sort", getApiSortPref());
                b.putBoolean("refresh", true);
                getLoaderManager().restartLoader(0, b, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri determineUri(String sort) {
        Utils.log(getClass().getSimpleName());
        if (StringUtils.containsIgnoreCase(sort, "popular"))
            return MovieContract.PopularEntry.buildUri();
        else if (StringUtils.containsIgnoreCase(sort, "vote") || StringUtils.containsIgnoreCase(sort, "rate"))
            return MovieContract.RatingEntry.buildUri();
        else if (StringUtils.containsIgnoreCase(sort, "fav"))
            return MovieContract.FavoriteEntry.buildUri();
        else
            throw new UnsupportedOperationException("Sort not identified: " + sort);
    }

    private void insertMovieListIntoDatabase(final String sort) {
        Utils.log(getClass().getSimpleName());
        MovieDbHelper dbHelper = new MovieDbHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues[] movie_ids = new ContentValues[mMovieList.size()];
        ContentValues[] cvs = new ContentValues[mMovieList.size()];
        int i = 0;
        try {
            for (MovieGridObj obj : mMovieList) {
                long movie_id = obj.id;
                byte[] blob = SerializationUtils.serialize(obj);
                ContentValues movieCv = new ContentValues();
                ContentValues idCv = new ContentValues();
                movieCv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie_id);
                movieCv.put(MovieContract.MovieEntry.COLUMN_MOVIE_BLOB, blob);
                idCv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie_id);
                cvs[i] = movieCv;
                movie_ids[i++] = idCv;
            }
            getActivity().getContentResolver().bulkInsert(MovieContract.MovieEntry.buildUri(), cvs);
            // Deleted whatever is in rating/poppular
            if (getString(R.string.tmdb_arg_highestrating).equals(sort) ||
                    getString(R.string.tmdb_arg_popularity).equals(sort)) {
                Uri uri = determineUri(sort);
                getActivity().getContentResolver().delete(uri, null, null);
                getActivity().getContentResolver().bulkInsert(uri, movie_ids);
            }
            Log.d(LOG_TAG, String.format("Just inserted movies %s", Arrays.toString(cvs)));
        } finally {
            db.close();
        }
    }

    private void getInternalData(Cursor cursor, String sort) {
        Utils.log(getClass().getSimpleName());
        List<MovieGridObj> lMaps = new ArrayList<>();
        while (cursor.moveToNext()) {
            byte[] bMovieObj = cursor.getBlob(1);
            MovieGridObj movieObj = SerializationUtils.deserialize(bMovieObj);
            lMaps.add(movieObj);
        }
        mMovieList.clear();
        mMovieList = lMaps;
        mGridViewAdapter.setGridData(mMovieList);
    }

    private void getLiveDataAndCallLoader(final String sort) {
        Utils.log(getClass().getSimpleName());
        if (isFav(sort))
            Snackbar.make(getView(), "Cannot Refresh When Sorting Preference is Favorites. Please choose '"
                    + getString(R.string.most_popular_title) + "' or '"
                    + getString(R.string.highest_rated_title)
                    + "'", Snackbar.LENGTH_SHORT).show();
        Uri builtUri = Uri.parse(getString(R.string.tmdb_api_base_discover_url)).buildUpon()
                .appendQueryParameter(getString(R.string.tmdb_param_sortby), sort)
                .appendQueryParameter(getString(R.string.tmdb_param_api), getString(R.string.private_tmdb_api))
                .build();
        String url = "";
        try {
            url = new URL(builtUri.toString()).toString();
        } catch (MalformedURLException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            // Toast.makeText(getActivity(), "Malformed URL " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(getClass().getSimpleName(), "updateMovieListVolley() - url = " + url);// .substring(0, url.length() - 16));
        final StopWatch sw = new StopWatch();
        sw.start();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(getClass().getSimpleName(), "Response received.");
                        Map<String, Serializable> map = Utils.getGson().fromJson(response.toString(), LinkedTreeMap.class);
                        handleMap(map, sort);
                        insertMovieListIntoDatabase(sort);
                        Bundle b = new Bundle();
                        b.putString("sort", sort);
                        getLoaderManager().restartLoader(0, b, mThis);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getClass().getSimpleName(), error.getMessage(), error);
                        Snackbar.make(mMovieGridView, String.format("Error connecting to server (%s) in: %s", error.getMessage(), sw), Snackbar.LENGTH_SHORT).show();
                    }
                });
        mVollRequestQueue.add(jsObjRequest);
    }

    private boolean isFav(String sort) {
        return getString(R.string.tmdb_arg_favorite).equals(sort);
    }

    private void handleMap(Map<String, Serializable> map, String sort) {
        Utils.log(getClass().getSimpleName());
        mMovieList = Utils.covertMapToMovieObjList(map);
        Log.d(getClass().getSimpleName(), "Received a set of movies. Registering them.");
        mGridViewAdapter.setGridData(mMovieList);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Utils.log(getClass().getSimpleName());
        final String sort = args.getString("sort");
        Uri uri = determineUri(sort);
        Boolean isRefresh = args.getBoolean("refresh");
        if (isRefresh) {
            getLiveDataAndCallLoader(sort);
            return null;
        } else
            return new CursorLoader(getActivity(), uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Utils.log(getClass().getSimpleName());
        String sort = getApiSortPref();

        if (!data.moveToFirst()) {
            if (!isFav(sort))
                getLiveDataAndCallLoader(sort);

            else
                Snackbar.make(getView(), "Cannot Refresh When Sorting Preference is Favorites. Please choose '"
                        + getString(R.string.most_popular_title) + "' or '"
                        + getString(R.string.highest_rated_title)
                        + "'", Snackbar.LENGTH_SHORT).show();
        } else {
            mGridViewAdapter.swapCursor(data);
            mPosition = data.getPosition();
            if (mPosition != GridView.INVALID_POSITION)
                mMovieGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGridViewAdapter.swapCursor(null);
    }
}

