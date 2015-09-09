package com.ericsender.android_nanodegree.popmovie.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.activities.DetailsActivity;
import com.ericsender.android_nanodegree.popmovie.adapters.GridViewAdapter;
import com.ericsender.android_nanodegree.popmovie.data.MovieContract;
import com.ericsender.android_nanodegree.popmovie.data.MovieDbHelper;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.utils.NaturalDeserializer;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        else //if (sort.equals(getString(R.string.highest_rated_val))) {
            return getString(R.string.tmdb_arg_highestrating);
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

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    @Override
    public void onResume() {
        String foo = getCurrentSortPref();
        Log.d(getClass().getSimpleName(), "onResume with Sort =  " + foo);
        // If a change to the sort order is seen, resort the gridview and redistplay
        if (!foo.equals(mCurrSortOrder)) { // This will also be true on inital loading.
            mCurrSortOrder = foo;
            Log.d(getClass().getSimpleName(), "Sorting on: " + mCurrSortOrder);
            // sortMovieList();
            updateMovieListVolley();
            setTitle();
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
                Log.d(getClass().getSimpleName(), "Refreshing!");
                updateMovieListVolley();
                //updateMovieList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMovieList() {
        FetchMoviesTask fmt = new FetchMoviesTask();
        fmt.execute("");
    }

    private void updateMovieListVolley() {
        int rows = 0;
        String sort = getApiSortPref();
        // TODO add conditions for wanting live data (refresh/empty db)
        // if (seekInternalDataFirst) {
        Cursor cursor = getActivity().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        rows = cursor.getCount();

        // TODO: Could the query handle loading live data isntead of the Fragment?
        if (rows == 0) {
            Log.d(LOG_TAG, "getting live data");
            getLiveData(sort);
        } else {
            Log.d(LOG_TAG, "getting database data");
            getInternalData(cursor, sort);
        }
    }

    private void insertMovieListIntoDatabase(final String sort) {
        MovieDbHelper dbHelper = new MovieDbHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Map<Long, Long> rowIds = new HashMap<>();
        ContentValues[] inserts = new ContentValues[mMovieList.size()];
        int i = 0;
        try {
            for (MovieGridObj obj : mMovieList) {
                long movie_id = obj.id;
                byte[] blob = Utils.serialize(obj);
                ContentValues cv = new ContentValues();
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie_id);
                cv.put(MovieContract.MovieEntry.COLUMN_JSON, blob);
                inserts[i++] = cv;
            }
            getActivity().getContentResolver().bulkInsert(MovieContract.MovieEntry.buildMovieUri(), inserts);
            Log.d(LOG_TAG, String.format("Just inserted movies %s", mMovieList));
        } finally {
            db.close();
        }
    }

    private void getInternalData(Cursor cursor, String sort) {
        List<LinkedHashMap<String, Serializable>> lMaps = new ArrayList<>();
        while (cursor.moveToNext()) {
            Long movie_id = cursor.getLong(0);
            byte[] bMovieObj = cursor.getBlob(1);
            LinkedHashMap<String, Serializable> movieObj = (LinkedHashMap<String, Serializable>)
                    Utils.deserialize(bMovieObj);
            bMovieObj = null; // Force feed to the GC.
            lMaps.add(movieObj);
        }
        mMovieList = Utils.convertJsonMapToMovieList(lMaps);
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
        insertMovieListIntoDatabase(sort);
    }

    /*
    TODO: This Async may be able to be deleted. Replaced with Volley above.
     */
    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = getClass().getSimpleName();

        String sort = getString(R.string.tmdb_arg_popularity);

        protected String[] doInBackground(String... params) {
            InputStream inputStream = null;
            BufferedReader reader = null;
            LinkedTreeMap map;
            Uri builtUri = Uri.parse(getString(R.string.tmdb_api_base_discover_url)).buildUpon()
                    .appendQueryParameter(getString(R.string.tmdb_param_sortby), sort)
                    .appendQueryParameter(getString(R.string.tmdb_param_api), getString(R.string.private_tmdb_api))
                    .build();


            try {
                URL url = new URL(builtUri.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                Log.e(LOG_TAG, "inputstream null!");
                return null;
            }

            try {
                reader = new BufferedReader(new InputStreamReader(inputStream));

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Object.class, new NaturalDeserializer());
                Gson gson = gsonBuilder.create();
                map = gson.fromJson(reader, LinkedTreeMap.class);
            } finally {
                Utils.closeQuietly(inputStream, reader);
            }

            if (map == null) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            Log.d(LOG_TAG, new GsonBuilder().setPrettyPrinting().create().toJson(map));
            return new String[0];
        }
    }

}

