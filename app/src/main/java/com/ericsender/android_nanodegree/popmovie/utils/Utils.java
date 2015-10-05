package com.ericsender.android_nanodegree.popmovie.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.data.MovieContract;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by g56147 on 8/11/2015.
 */
public class Utils {
    private static final String LOG_TAG = Utils.class.getSimpleName();
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(Object.class, new NaturalDeserializer()).create();

    /*
        * http://stackoverflow.com/a/18387977/1582712
        */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static final Gson getGson() {
        return gson;
    }

    public static final String readStreamToString(InputStream is) {
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bis.readLine()) != null) sb.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static List<MovieGridObj> convertJsonMapToMovieList(Object resultsObj) {
        List<Map<String, Object>> results = (List<Map<String, Object>>) resultsObj;
        Set<MovieGridObj> movies = new HashSet<>();
        for (Map<String, Object> m : results) {
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
                        movie.id = ((Double) e.getValue()).longValue();
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
            movies.add(movie);
        }
        return new ArrayList<>(movies);
    }

    public static void closeQuietly(Closeable... cs) {
        for (Closeable c : cs)
            if (c != null) try {
                c.close();
            } catch (IOException e) {
            }
    }

    public static List<MovieGridObj> covertMapToMovieObjList(Map<String, Serializable> map) {
        List<MovieGridObj> movies = null;
        Double page, total_pages, total_results;
        for (Map.Entry<String, Serializable> entry : map.entrySet())
            switch (entry.getKey()) {
                case "page":
                    page = (Double) entry.getValue();
                    break;
                case "results":
                    movies = convertJsonMapToMovieList((ArrayList) entry.getValue());
                    break;
                case "total_pages":
                    total_pages = (Double) entry.getValue();
                    break;
                case "total_results":
                    total_results = (Double) entry.getValue();
                    break;
                default:
                    Log.d(LOG_TAG, "Key/Val did not match predefined set: " + entry.getKey());
            }
        return movies;
    }

    public static void eraseDatabase(Activity activity) {
        activity.getContentResolver().delete(MovieContract.MovieEntry.buildUri(), null, null);
        activity.getContentResolver().delete(MovieContract.FavoriteEntry.buildUri(), null, null);
        activity.getContentResolver().delete(MovieContract.RatingEntry.buildUri(), null, null);
        activity.getContentResolver().delete(MovieContract.PopularEntry.buildUri(), null, null);
        Log.d(LOG_TAG, "Erased databased!");
    }

    /**
     * Get the method name for a depth in call stack. <br />
     * Utility function <br />
     * <a href="http://stackoverflow.com/a/442789/1582712">Source</a>
     *
     * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
     * @return method name
     */
    public static final String getMethodName(final int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[depth].getMethodName();
    }

    public static void log() {
        log(null);
    }

    public static void log(String append) {
        boolean isAppend = StringUtils.isNotEmpty(append);
        Log.d("UTILS", String.format("%s%s%s", getMethodName(4 + 1), isAppend ? " - " : "", isAppend ? append : ""));
    }

    public static void hideViewSafe(View v) {
        if (v != null) v.setVisibility(View.GONE);
    }

    public static void makeMenuItemInvisible(Menu menu, int... ids) {
        for (int id : ids) {
            MenuItem mi = menu.findItem(id);
            if (mi != null) mi.setVisible(false);
        }
    }
}
