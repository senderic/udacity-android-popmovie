package com.ericsender.android_nanodegree.popmovie.com.ericsender.android_nanodegree.popmovie.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.test.AndroidTestCase;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.com.ericsender.android_nanodegree.popmovie.utils.PollingCheck;
import com.ericsender.android_nanodegree.popmovie.data.MovieContract;
import com.ericsender.android_nanodegree.popmovie.data.MovieDbHelper;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by Eric K. Sender on 9/2/2015.
 * Adapted from Udacity Sunshine App
 */
public class TestUtilities extends AndroidTestCase {

    static void validateRecordsToDatabase(String error, Cursor valueCursor, Map<Long, ContentValues> expectedValues) {
        while (valueCursor.moveToNext()) {
            long _id = valueCursor.getLong(0);
            Long movie_id = valueCursor.getLong(1);
            byte[] bMovieObj = valueCursor.getBlob(2);
            LinkedHashMap<String, Serializable> movieObj = (LinkedHashMap<String, Serializable>)
                    Utils.deserialize(bMovieObj);

            ContentValues cv = expectedValues.get(_id);
            Long expectedId = (Long) cv.get(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            byte[] expectedBMovieObject = (byte[]) cv.get(MovieContract.MovieEntry.COLUMN_JSON);
            LinkedHashMap<String, Serializable> expectedMovieObj = (LinkedHashMap<String, Serializable>) Utils.deserialize(expectedBMovieObject);

            assertEquals("movied id's don't match", expectedId, movie_id);
            // assumes I didn't screw up the equals()/hashCode() functions!
            assertEquals("movie objects don't match", expectedMovieObj, movieObj);
            // paranoid sanity check:
            assertTrue("binary movie objects don't match", Arrays.equals(expectedBMovieObject, bMovieObj));
        }
    }

    public static void validateFavoritesCursor(Cursor favCursor, Map<Long, ContentValues> expectedValues, Set<Long> insertedMoviedIds) {
        assertTrue("Empty cursor returned. ", favCursor.moveToFirst());
        validateFavoriteCurrentRecord(favCursor, expectedValues, insertedMoviedIds);
        favCursor.close();
    }

    private static void validateFavoriteCurrentRecord(Cursor favCursor, Map<Long, ContentValues> expectedValues, Set<Long> insertedMoviedIds) {
        while (favCursor.moveToNext()) {
            Long movie_id = favCursor.getLong(0);

            assertTrue("Movied ID must be in inserted set", insertedMoviedIds.contains(movie_id));

            byte[] blob = favCursor.getBlob(1);

            ContentValues expect = expectedValues.get(movie_id);

            assertEquals("movied ids don't match", expect.getAsLong(MovieContract.MovieEntry.COLUMN_MOVIE_ID), movie_id);
            assertTrue("Binaries don't match", Arrays.equals(expect.getAsByteArray(MovieContract.MovieEntry.COLUMN_JSON), blob));
        }
    }

    static void validateMovieCursor(Cursor valueCursor, Map<Long, ContentValues> expectedValues) {
        assertTrue("Empty cursor returned. ", valueCursor.moveToFirst());
        validateMovieCurrentRecord(valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateMovieCurrentRecord(Cursor valueCursor, Map<Long, ContentValues> expectedValue) {
        while (valueCursor.moveToNext()) {
            long _id = valueCursor.getLong(0);
            Long movie_id = valueCursor.getLong(1);
            byte[] blob = valueCursor.getBlob(2);

            ContentValues expect = expectedValue.get(movie_id);

            assertEquals("movied ids don't match", expect.getAsLong(MovieContract.MovieEntry.COLUMN_MOVIE_ID), movie_id);
            assertTrue("Binaries don't match", Arrays.equals(expect.getAsByteArray(MovieContract.MovieEntry.COLUMN_JSON), blob));
        }
    }


    /*
        Created a Map of content values ready for database insertion.
     */
    static Map<Long, ContentValues> createPopularMovieValues(Context c) {
        // Create a new map of values, where column names are the keys
        Map<Long, ContentValues> testValues = new HashMap<>();
        LinkedTreeMap<String, Serializable> map = getPopularDataAsMap(c);
        long dbRowId = 0;
        for (Map<String, Serializable> m : (List<Map<String, Serializable>>) map.get("results")) {
            ContentValues cv = createMovieContentValue(dbRowId++, m);
            testValues.put(cv.getAsLong("movie_id"), cv);
        }

        return testValues;
    }

    public static LinkedTreeMap<String, Serializable> getPopularDataAsMap(Context c) {
        InputStream in = c.getResources().openRawResource(R.raw.popular);
        String json = Utils.readStreamToString(in);
        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Utils.getGson().fromJson(json, LinkedTreeMap.class);
    }

    @NonNull
    public static ContentValues createMovieContentValue(long dbRowId, Map<String, Serializable> m) {
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry._ID, dbRowId);
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, ((Double) m.get("id")).longValue());
        cv.put(MovieContract.MovieEntry.COLUMN_JSON, Utils.serialize(m));
        return cv;
    }

    @NonNull
    public static ContentValues createFavoriteContentValue(long movie_id) {
        ContentValues cv = new ContentValues();
        //cv.put(MovieContract.FavoriteEntry._ID, dbRowId);
        cv.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID, movie_id);
        return cv;
    }

    /*
        Students: You can uncomment this function once you have finished creating the
        FavoriteEntry part of the MovieContract as well as the MovieDbHelper.
     */
    public static Map<Long, Long> insertMovieRow(Context context, Map<Long, ContentValues> cvs) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Map<Long, Long> rowIds = new HashMap<>();
        try {
            for (Map.Entry<Long, ContentValues> cv : cvs.entrySet()) {
                long locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, cv.getValue());

                // Verify we got a row back.
                assertTrue("Error: Failure to insert value", locationRowId != -1L);
                rowIds.put(locationRowId, cv.getKey());
            }
        } finally {
            db.close();
        }
        return rowIds;
    }

    private static final Random rand = new Random();

    public static Long generateRandomFavoritesAndInsert(SQLiteDatabase db, ContentValues cv) {
        if (rand.nextBoolean()) { // if is favorite
            long movie_id = (Long) cv.get(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            ContentValues fcv = createFavoriteContentValue(movie_id);
            db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, fcv);
            return movie_id;
        } else
            return null;
    }

    public static boolean verifyFavoritesAreInMoviesTable(int dbFavOrder, Cursor cursor, SQLiteDatabase db) {
        assertTrue("Something returned", cursor.moveToFirst());
        assertEquals("Number of inserts = rows returned", dbFavOrder, cursor.getCount());

        while (cursor.moveToNext()) {
            long _id = cursor.getLong(0);
            Long movie_id = cursor.getLong(1);

            Cursor c = db.query(
                    MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                    null, // all columns
                    String.format("%s=?", MovieContract.MovieEntry.COLUMN_MOVIE_ID), // Columns for the "where" clause
                    new String[]{movie_id.toString()}, // Values for the "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null // sort order
            );
            assertTrue("Movie_id did not match anything in the Movies table", c.moveToFirst());
            c.close();
        }
        return true;
    }

    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}