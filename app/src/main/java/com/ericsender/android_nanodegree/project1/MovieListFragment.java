package com.ericsender.android_nanodegree.project1;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ericsender.android_nanodegree.project1.adapters.GridViewAdapter;
import com.ericsender.android_nanodegree.project1.adapters.MovieObj;
import com.ericsender.android_nanodegree.project1.utils.NaturalDeserializer;
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
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListFragment extends Fragment {

    private ArrayAdapter<MovieObj> mMovieAdapter;
    private List<MovieObj> mMovieList = new ArrayList<>();
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Object.class, new NaturalDeserializer()).create();
    private GridViewAdapter mGridViewAdapter;
    private GridView mMovieGrid;

    public MovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.movie_list_fragment, container, false);
        mMovieGrid = (GridView) rootView.findViewById(R.id.movie_grid);
        mGridViewAdapter = new GridViewAdapter(getActivity(), R.layout.movie_cell, mMovieList, mMovieGrid);
        mMovieGrid.setAdapter(mGridViewAdapter);
        // mMovieAdapter = new ArrayAdapter<String>(getActivity(), R.layout.grid_movie_posters,

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        updateMovieListVolley();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
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
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        Uri builtUri = Uri.parse(getString(R.string.tmdb_api_base_url)).buildUpon()
                .appendQueryParameter(getString(R.string.tmdb_param_sortby), getString(R.string.tmdb_arg_popularity))
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

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        LinkedTreeMap<String, Object> map = gson.fromJson(response.toString(), LinkedTreeMap.class);
                        List<MovieObj> movies = covertMapToMovieObjList(map);
                        if (!movies.isEmpty()) {
                            mGridViewAdapter.clear();
                            mMovieList = movies;
                            mGridViewAdapter.setGridData(mMovieList);
                        }
                    }

                    private List<MovieObj> covertMapToMovieObjList(LinkedTreeMap<String, Object> map) {
                        List<MovieObj> movies = null;
                        for (Map.Entry<String, Object> entry : map.entrySet())
                            switch (entry.getKey()) {
                                case "page":
                                    Double page = (Double) entry.getValue();
                                    break;
                                case "results":
                                    movies = handleResults((ArrayList) entry.getValue());
                                    break;
                                case "total_pages":
                                    Double total_pages = (Double) entry.getValue();
                                    break;
                                case "total_results":
                                    Double total_results = (Double) entry.getValue();
                                    break;
                                default:
                                    Log.d(getClass().getSimpleName(), "Key/Val did not match predefined set: " + entry.getKey());
                            }
                        return movies;
                    }

                    private List<MovieObj> handleResults(List<LinkedTreeMap<String, Object>> results) {
                        List<MovieObj> movies = new ArrayList<>();
                        for (LinkedTreeMap<String, Object> m : results) {
                            MovieObj movie = new MovieObj();
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
                                        movie.vote_count = (Double) e.getValue();
                                        break;
                                }
                            movies.add(movie);
                        }
                        return movies;
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(getClass().getSimpleName(), error.getMessage(), error);
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
            Uri builtUri = Uri.parse(getString(R.string.tmdb_api_base_url)).buildUpon()
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

