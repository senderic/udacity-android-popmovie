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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListFragment extends Fragment {

    private ArrayAdapter<String> mMovieAdapter;

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
        return inflater.inflate(R.layout.movie_list_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateMovieList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMovieList() {
        FetchMoviesTask fmt = new FetchMoviesTask();
        fmt.execute("");
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = getClass().getSimpleName();

        String sort = getString(R.string.tmdb_arg_popularity);

        protected String[] doInBackground(String... params) {
            InputStream inputStream = null;
            BufferedReader reader = null;
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
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            } finally {
                closeQuietly(inputStream, reader);
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            Log.i(LOG_TAG, buffer.toString());
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
