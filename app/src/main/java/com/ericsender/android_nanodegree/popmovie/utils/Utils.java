package com.ericsender.android_nanodegree.popmovie.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.ericsender.android_nanodegree.popmovie.fragments.MovieListFragment;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

    // Serialize / Deserialize -- Based on:
    // http://grepcode.com/file_/repo1.maven.org/maven2/commons-lang/commons-lang/2.4/org/apache/commons/lang/SerializationUtils.java/?v=source
    //-----------------------------------------------------------------------

    // Clone
    //-----------------------------------------------------------------------
    /**
     * <p>Deep clone an <code>Object</code> using serialization.</p>
     *
     * <p>This is many times slower than writing clone methods by hand
     * on all objects in your object graph. However, for complex object
     * graphs, or for those that don't support deep cloning this can
     * be a simple alternative implementation. Of course all the objects
     * must be <code>Serializable</code>.</p>
     *
     * @param object  the <code>Serializable</code> object to clone
     * @return the cloned object
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static Object clone(Serializable object) {
        return deserialize(serialize(object));
    }

    // Serialize
    //-----------------------------------------------------------------------
    /**
     * <p>Serializes an <code>Object</code> to the specified stream.</p>
     *
     * <p>The stream will be closed once the object is written.
     * This avoids the need for a finally clause, and maybe also exception
     * handling, in the application code.</p>
     *
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param obj  the object to serialize to bytes, may be null
     * @param outputStream  the stream to write to, must not be null
     * @throws IllegalArgumentException if <code>outputStream</code> is <code>null</code>
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static void serialize(Object obj, OutputStream outputStream) {
        if (outputStream == null)
            throw new IllegalArgumentException("The OutputStream must not be null");

        try {
            new ObjectOutputStream(outputStream).writeObject(obj);
        } catch (IOException ex) {
            throw new SerializationException(ex);
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {}
        }
    }

    /**
     * <p>Serializes an <code>Object</code> to a byte array for
     * storage/serialization.</p>
     *
     * @param obj  the object to serialize to bytes
     * @return a byte[] with the converted Serializable
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }
    /**
     * <p>Deserializes an <code>Object</code> from the specified stream.</p>
     * <p/>
     * <p>The stream will be closed once the object is written. This
     * avoids the need for a finally clause, and maybe also exception
     * handling, in the application code.</p>
     * <p/>
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param inputStream the serialized object input stream, must not be null
     * @return the deserialized object
     * @throws SerializationException   (runtime) if the serialization fails
     */
    public static Object deserialize(InputStream inputStream) {
        try {
            return (new ObjectInputStream(inputStream)).readObject();
        } catch (ClassNotFoundException | IOException ex) {
            throw new SerializationException(ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * <p>Deserializes a single <code>Object</code> from an array of bytes.</p>
     *
     * @param objectData the serialized object, must not be null
     * @return the deserialized object
     * @throws SerializationException   (runtime) if the serialization fails
     */
    public static Object deserialize(byte[] objectData) {
        return deserialize(new ByteArrayInputStream(objectData));
    }

    public static final String f(String s, Object... args) {
        return String.format(s, args);
    }

    public static List<MovieGridObj> convertJsonMapToMovieList(Object resultsObj) {
        List<LinkedTreeMap<String, Object>> results = (List<LinkedTreeMap<String, Object>>) resultsObj;
        Set<MovieGridObj> movies = new HashSet<>();
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
            // Log.d(getClass().getSimpleName(), "Just received " + movie.title + " from API.");
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

    public static List<MovieGridObj> covertMapToMovieObjList(LinkedTreeMap<String, Serializable> map) {
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

    public static class SerializationException extends RuntimeException {
        public SerializationException(Throwable ex) {
            super(ex);
        }
    }
}
