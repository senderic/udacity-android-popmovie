package com.ericsender.android_nanodegree.popmovie.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ericsender.android_nanodegree.popmovie.Application.PopMoviesApplication;
import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.activities.DetailsActivity;
import com.ericsender.android_nanodegree.popmovie.adapters.GridViewAdapter;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ericsender.android_nanodegree.popmovie.Application.STATE.REFRESH_GRID;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListFragment extends Fragment {

    public static final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private ArrayAdapter<MovieGridObj> mMovieAdapter;
    private List<MovieGridObj> mMovieList = new ArrayList<>();
    private GridViewAdapter mGridViewAdapter;
    private GridView mMovieGridView;
    private String mCurrSortOrder;

    private String getCurrentSortPref() {
        return PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_sort_order_key),
                        getString(R.string.most_popular_val));
    }

    private String getApiSortPref() {
        String sort = getCurrentSortPref();

        if (sort.equals(getString(R.string.most_popular_val)))
            return getString(R.string.tmdb_arg_popularity);
        else if (sort.equals(getString(R.string.highest_rated_val)))
            return getString(R.string.tmdb_arg_highestrating);
        else //if (sort.equals(getString(R.string.favorite_val)))
            return getString(R.string.tmdb_arg_favorite);
        // else throw new RuntimeException("Sort order value is not known: " + sort);
    }

    private void setTitle() {
        // getActivity().setTitle(getString(R.string.title_activity_main) + " - " + getCurrentSortPref());
    }

    public MovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getContentResolver().delete(MovieContract.PopularEntry.buildPopularUri(), null, null);
        getActivity().getContentResolver().delete(MovieContract.RatingEntry.buildRatingUri(), null, null);

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    @Override
    public void onResume() {
        String foo = getCurrentSortPref();
        Log.d(getClass().getSimpleName(), "onResume with Sort =  " + foo);
        // If a change to the sort order is seen, resort the gridview and redistplay
        PopMoviesApplication Me = ((PopMoviesApplication) getActivity().getApplication());
        AtomicBoolean refreshGrid = (AtomicBoolean) Me.getStateManager().getState(REFRESH_GRID);
        Log.d(LOG_TAG, "Forced RefreshGrid status is: " + refreshGrid);
        if (refreshGrid.get() == true || !foo.equals(mCurrSortOrder)) { // This will also be true on inital loading.
            mCurrSortOrder = foo;
            Log.d(getClass().getSimpleName(), "Sorting on: " + mCurrSortOrder);
            // sortMovieList();
            updateMovieListVolley(false);
            setTitle();
            refreshGrid.set(false);
        }
        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.movie_list_fragment, container, false);
        mMovieGridView = (GridView) rootView.findViewById(R.id.movie_grid);
        mGridViewAdapter = new GridViewAdapter(getActivity(), R.layout.movie_cell, mMovieList, mMovieGridView);
        mMovieGridView.setAdapter(mGridViewAdapter);
        // mMovieAdapter = new ArrayAdapter<String>(getActivity(), R.layout.grid_movie_posters,
        createGridItemClickCallbacks();

        return rootView;
    }

    private void createGridItemClickCallbacks() {
        //Grid view click event
        mMovieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                Parcelable item = null;
                try {
                    item = (MovieGridObj) parent.getItemAtPosition(position);
                } catch (IndexOutOfBoundsException e) {
                }
                if (item != null) {
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);

                    // Interesting data to pass across are the thumbnail size/location, the
                    // resourceId of the source bitmap, the picture description, and the
                    // orientation (to avoid returning back to an obsolete configuration if
                    // the device rotates again in the meantime)

                    int[] screenLocation = new int[2];
                    imageView.getLocationOnScreen(screenLocation);

                    //Pass the image title and url to DetailsActivity
                    intent.putExtra("left", screenLocation[0]).
                            putExtra("top", screenLocation[1]).
                            putExtra("movieObj", item);

                    //Start details activity
                    startActivity(intent);
                } else
                    Toast.makeText(getActivity(), "No Movie Selected!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.d(LOG_TAG, "Refreshing!");
                updateMovieListVolley(true);
                //updateMovieList();
                return true;
//            case R.id.action_sort:
//                Log.d(LOG_TAG, "Sort Spinner");
//                handleSortSpinner();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//      TODO: implement as an optional bonus.
//    private void handleSortSpinner() {
//        final Spinner spinner = (Spinner) getActivity().findViewById(R.id.sort_spinner);
//        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
//                R.array.pref_sort_order_entries, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(LOG_TAG, Utils.f("onItemSelected parent (%s), view (%s), position (%s), id (%s)", parent, view, position, id));
//                // parent.removeView(spinner);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//    }

    private Uri determineUri(String sort) {
        if (StringUtils.containsIgnoreCase(sort, "popular"))
            return MovieContract.PopularEntry.buildPopularUri();
        else if (StringUtils.containsIgnoreCase(sort, "vote"))
            return MovieContract.RatingEntry.buildRatingUri();
        else if (StringUtils.containsIgnoreCase(sort, "fav"))
            return MovieContract.FavoriteEntry.buildFavoriteUri();
        else
            throw new UnsupportedOperationException("Sort not identified: " + sort);
    }

    private void updateMovieListVolley(boolean hardRefresh) {
        int rows = 0;
        final String sort = getApiSortPref();
        boolean isFav = getString(R.string.tmdb_arg_favorite).equals(sort);
        Cursor cursor = null;
        try {
            if (hardRefresh == false) {  // Get data internally
                Uri uri = determineUri(sort);

                cursor = getActivity().getContentResolver().query(
                        uri,
                        null,
                        null,
                        null,
                        null
                );
                rows = cursor.getCount();
                Log.d(LOG_TAG, "Queried this number of rows: " + rows);
                if (isFav && rows == 0) {
                    Toast.makeText(getActivity(), "No Favorites in Database. Please select a different sort!", Toast.LENGTH_SHORT).show();
                    mMovieList.clear();
                    registeringData(sort);
                    return;
                }
            }
            // TODO: Could the query handle loading live data isntead of the Fragment?
            if (rows == 0) {
                if (isFav)
                    Toast.makeText(getActivity(), "Cannot Refresh When Sorting Preference is Favorites. Please choose '"
                            + getString(R.string.most_popular_title) + "' or '"
                            + getString(R.string.highest_rated_title)
                            + "'", Toast.LENGTH_SHORT).show();
                else {
                    Log.d(LOG_TAG, "getting live data");
                    getLiveData(sort);
                }
            } else if (cursor != null) {
                Log.d(LOG_TAG, Utils.f("getting database data (rows returned = %d)", rows));
                getInternalData(cursor, sort);
            } else
                throw new UnsupportedOperationException(Utils.f("Cursor is null but %d rows were expected", rows));
        } finally {
            Utils.closeQuietly(cursor);
        }
    }

    private void insertMovieListIntoDatabase(final String sort) {
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
                movieCv.put(MovieContract.MovieEntry.COLUMN_JSON, blob);
                idCv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie_id);
                cvs[i] = movieCv;
                movie_ids[i++] = idCv;
            }
            getActivity().getContentResolver().bulkInsert(MovieContract.MovieEntry.buildMovieUri(), cvs);
            // Deleted whatever is in rating/poppular
            if (StringUtils.containsIgnoreCase(sort, "rate") ||
                    StringUtils.containsIgnoreCase(sort, "popular")) {
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
        List<MovieGridObj> lMaps = new ArrayList<>();
        while (cursor.moveToNext()) {
            Long movie_id = cursor.getLong(0);
            byte[] bMovieObj = cursor.getBlob(1);
            MovieGridObj movieObj = (MovieGridObj) SerializationUtils.deserialize(bMovieObj);
            lMaps.add(movieObj);
        }
        mMovieList.clear();
        mMovieList = lMaps;
        registeringData(sort);
    }

    private void getLiveData(final String sort) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
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
        final Toast t = Toast.makeText(getActivity(), "Loading Data...", Toast.LENGTH_LONG);
        final StopWatch sw = new StopWatch();
        t.show();
        sw.start();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(getClass().getSimpleName(), "Response received.");
                        LinkedTreeMap<String, Serializable> map = Utils.getGson().fromJson(response.toString(), LinkedTreeMap.class);
                        handleMap(map, sort);
                        insertMovieListIntoDatabase(sort);
                        t.setText("Loading Finished in: " + sw);
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getClass().getSimpleName(), error.getMessage(), error);
                        t.setText(String.format("Error connecting to server (%s) in: %s", error.getMessage(), sw));
                        //Toast.makeText(getActivity(), "Error connecting to server.", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(jsObjRequest);
    }

    private void handleMap(LinkedTreeMap<String, Serializable> map, String sort) {
        mMovieList = Utils.covertMapToMovieObjList(map);
        Log.d(getClass().getSimpleName(), "Received a set of movies. Registering them.");
        registeringData(sort);
    }

    private void registeringData(String sort) {
        mGridViewAdapter.setGridData(mMovieList);
    }
}

