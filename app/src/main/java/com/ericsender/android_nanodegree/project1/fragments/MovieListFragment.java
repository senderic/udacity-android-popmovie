package com.ericsender.android_nanodegree.project1.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.ericsender.android_nanodegree.project1.R;
import com.ericsender.android_nanodegree.project1.activities.DetailsActivity;
import com.ericsender.android_nanodegree.project1.adapters.GridViewAdapter;
import com.ericsender.android_nanodegree.project1.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.project1.utils.NaturalDeserializer;
import com.ericsender.android_nanodegree.project1.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListFragment extends Fragment {

    public static final String MOVIE_LIST_KEY = "movieList";
    public static final String CURR_SORT_ORDER_KEY = "currSortOrder";
    private ArrayAdapter<MovieGridObj> mMovieAdapter;
    private ArrayList<MovieGridObj> mMovieList = new ArrayList<>();
    private GridViewAdapter mGridViewAdapter;
    private GridView mMovieGridView;
    private final Comparator<MovieGridObj> sortAlgo = new Comparator<MovieGridObj>() {
        @Override
        public int compare(MovieGridObj lhs, MovieGridObj rhs) {
            // Not sorting now, using API calls for each new sort. Returning 1 essentially does nothing
            return 1; //return performSort(lhs, rhs);
        }

        private int performSort(MovieGridObj lhs, MovieGridObj rhs) {
            String sort = getCurrentSortPref();

            sort = sort == null ? "" : sort; // defensive-ish code

            // Log.d(getClass().getSimpleName(), f("Sorting %s and %s based on %s", lhs.title, rhs.title, sort));

            if (sort.equalsIgnoreCase(getString(R.string.most_popular_val)))
                return lhs.popularity.compareTo(rhs.popularity);
            else if (sort.equalsIgnoreCase(getString(R.string.highest_rated_val)))
                return lhs.vote_average.compareTo(rhs.vote_average);
            else throw new RuntimeException("Sort setting not valid: " + sort);
        }
    };
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

    public static final String f(String s, Object... args) {
        return String.format(s, args);
    }

    public MovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Log.d(getClass().getSimpleName(), "SavedInstanceState - Restore");
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
            mCurrSortOrder = savedInstanceState.getString(CURR_SORT_ORDER_KEY);
        }
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
        } else Log.d(getClass().getSimpleName(), "No update needed");
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIE_LIST_KEY, mMovieList);
        outState.putString(CURR_SORT_ORDER_KEY, mCurrSortOrder);
    }

    private void sortMovieList() {
        Collections.sort(mMovieList, sortAlgo);
        mGridViewAdapter.setGridData(mMovieList);
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
                MovieGridObj item = null;
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
        String sort = getApiSortPref();

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

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(getClass().getSimpleName(), "Response received.");
                        LinkedTreeMap<String, Object> map = Utils.getGson().fromJson(response.toString(), LinkedTreeMap.class);
                        mMovieList = covertMapToMovieObjList(map);
                        Log.d(getClass().getSimpleName(), "Received a set of movies. Registering them.");
                        mGridViewAdapter.setGridData(mMovieList);
                    }

                    private ArrayList<MovieGridObj> covertMapToMovieObjList(LinkedTreeMap<String, Object> map) {
                        ArrayList<MovieGridObj> movies = null;
                        Double page, total_pages, total_results;
                        for (Map.Entry<String, Object> entry : map.entrySet())
                            switch (entry.getKey()) {
                                case "page":
                                    page = (Double) entry.getValue();
                                    break;
                                case "results":
                                    movies = handleResults((ArrayList) entry.getValue());
                                    break;
                                case "total_pages":
                                    total_pages = (Double) entry.getValue();
                                    break;
                                case "total_results":
                                    total_results = (Double) entry.getValue();
                                    break;
                                default:
                                    Log.d(getClass().getSimpleName(), "Key/Val did not match predefined set: " + entry.getKey());
                            }
//                        for (int c = 0; c < movies.size(); c++)
//                            Log.d(getClass().getSimpleName(), f("%d>> List for %s", c + 1, movies.get(c)));
                        return movies;
                    }

                    private ArrayList<MovieGridObj> handleResults(Object resultsObj) {
                        ArrayList<LinkedTreeMap<String, Object>> results = (ArrayList<LinkedTreeMap<String, Object>>) resultsObj;
                        Set<MovieGridObj> movies = new TreeSet<>(sortAlgo);
                        for (LinkedTreeMap<String, Object> m : results) {
                            MovieGridObj movie = new MovieGridObj();
                            for (Map.Entry<String, Object> e : m.entrySet())
                                switch (e.getKey()) {
                                    case "adult":
                                        movie.adult = (Boolean) e.getValue();
                                        break;
                                    case "backdrop_path":
                                        movie.backdrop_path = (String) e.getValue();
                                        break;
                                    case "genre_ids":
                                        movie.genre_ids = (ArrayList<Double>) e.getValue();
                                        break;
                                    case "id":
                                        movie.id = (Double) e.getValue();
                                        break;
                                    case "original_language":
                                        movie.original_language = (String) e.getValue();
                                        break;
                                    case "original_title":
                                        movie.original_title = (String) e.getValue();
                                        break;
                                    case "overview":
                                        movie.overview = (String) e.getValue();
                                        break;
                                    case "release_date":
                                        movie.release_date = (String) e.getValue();
                                        break;
                                    case "poster_path":
                                        movie.poster_path = (String) e.getValue();
                                        break;
                                    case "popularity":
                                        movie.popularity = (Double) e.getValue();
                                        break;
                                    case "title":
                                        movie.title = (String) e.getValue();
                                        break;
                                    case "video":
                                        movie.video = (Boolean) e.getValue();
                                        break;
                                    case "vote_average":
                                        movie.vote_average = (Double) e.getValue();
                                        break;
                                    case "vote_count":
                                        movie.vote_count = ((Double) e.getValue()).intValue();
                                        break;
                                }
                            // Log.d(getClass().getSimpleName(), "Just received " + movie.title + " from API.");
                            movies.add(movie);
                        }
                        return new ArrayList<>(movies);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getClass().getSimpleName(), error.getMessage(), error);
                        Toast.makeText(getActivity(), "Error connecting to server.", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsObjRequest);
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
                closeQuietly(inputStream, reader);
            }

            if (map == null) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            Log.d(LOG_TAG, new GsonBuilder().setPrettyPrinting().create().toJson(map));
            return new String[0];
        }
    }

    public static void closeQuietly(Closeable... cs) {
        for (Closeable c : cs)
            if (c != null) try {
                c.close();
            } catch (IOException e) {
            }
    }
}

