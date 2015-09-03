package com.ericsender.android_nanodegree.popmovie.com.ericsender.android_nanodegree.popmovie.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;
import android.util.Log;

import com.ericsender.android_nanodegree.popmovie.R;
import com.ericsender.android_nanodegree.popmovie.com.ericsender.android_nanodegree.popmovie.utils.PollingCheck;
import com.ericsender.android_nanodegree.popmovie.data.MovieContract;
import com.ericsender.android_nanodegree.popmovie.data.MovieDbHelper;
import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eric K. Sender on 9/2/2015.
 * Adapted from Udacity Sunshine App
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_LOCATION = "99705";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Students: Use this to create some default weather values for your database tests.
     */
    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(MovieContract.MovieEntry.COLUMN_JSON, locationRowId);
        weatherValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, TEST_DATE);

        return weatherValues;
    }

    /*
        Students: You can uncomment this helper function once you have finished creating the
        FavoriteEntry part of the MovieContract.
     */
    static List<ContentValues> createMovieValues(Context c) {
        // Create a new map of values, where column names are the keys
        List<ContentValues> testValues = new ArrayList<>();
        InputStream in = c.getResources().openRawResource(R.raw.popular);
        String json = Utils.readStreamToString(in);
        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LinkedTreeMap<String, Serializable> map = Utils.getGson().fromJson(json, LinkedTreeMap.class);

        for (Map<String, Serializable> m : (List<Map<String, Serializable>>) map.get("results")) {
            ContentValues cv = new ContentValues();
            cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, ((Double) m.get("id")).longValue());
            cv.put(MovieContract.MovieEntry.COLUMN_JSON, Utils.serialize(m));
            testValues.add(cv);
        }

        return testValues;
    }

    /*
        Students: You can uncomment this function once you have finished creating the
        FavoriteEntry part of the MovieContract as well as the MovieDbHelper.
     */
    static long insertNorthPoleLocationValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = null;// TestUtilities.createMovieValues(context);

        long locationRowId;
        locationRowId = db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
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