package com.ericsender.android_nanodegree.popmovie.com.ericsender.android_nanodegree.popmovie.data;

/**
 * Created by Eric K. Sender on 9/2/2015.
 * Adapted from Udacity Sunshine App
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.ericsender.android_nanodegree.popmovie.data.MovieContract;
import com.ericsender.android_nanodegree.popmovie.data.MovieDbHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }


    public void setUp() {
        deleteTheDatabase();
    }


    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.FavoriteEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.RatingEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.PopularEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(
                this.mContext).getWritableDatabase();
        assertTrue("Database should be open.", db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without all required tables.",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_JSON);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_ID);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                movieColumnHashSet.isEmpty());
        db.close();
    }

    public void testMovieTable() {
        insertMovies();
    }

    public void testFavorites() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Map<Long, ContentValues> orderedInserts = insertMovies();
        try {
            for (int i = 0; i < 2; i++) {
                dbHelper.emptyFavorites(db);
                Cursor c = db.query(MovieContract.FavoriteEntry.TABLE_NAME, null, null, null, null, null, null);
                assertFalse("Favorites table needs to be empty", c.moveToFirst());
                c.close();
                long dbFavOrder = 0;
                while (dbFavOrder == 0) // just in case we get all random falses.
                    for (Map.Entry<Long, ContentValues> e : orderedInserts.entrySet())
                        dbFavOrder = TestUtilities.generateRandomFavorites(db, dbFavOrder, e.getValue());

                Cursor cursor = db.query(
                        MovieContract.FavoriteEntry.TABLE_NAME,  // Table to Query
                        null, // all columns
                        null, // Columns for the "where" clause
                        null, // Values for the "where" clause
                        null, // columns to group by
                        null, // columns to filter by row groups
                        null // sort order
                );

                assertTrue("Favorites not in Movies Table",
                        TestUtilities.verifyFavoritesAreInMoviesTable(dbFavOrder, cursor, db));
            }
        } finally {
            db.close();
        }
    }


    public Map<Long, ContentValues> insertMovies() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createPopularMovieValues if you wish)

        List<ContentValues> testValues = TestUtilities.createPopularMovieValues(mContext);
        Map<Long, ContentValues> insertOrderedTestValues = new HashMap<>();
        // Third Step: Insert ContentValues into database and get a row ID back
        long movieRowId = -1L;
        int insertCount = 0;
        for (ContentValues cv : testValues) {
            movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, cv);

            // Verify we got a row back.
            assertFalse("Insert failed!", -1L == movieRowId);
            insertOrderedTestValues.put(movieRowId, cv);
            assertEquals("InsertCount match RowId", insertCount++, movieRowId);
        }

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue("Error: No Records returned from movie query", cursor.moveToFirst());

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateRecordsToDatabase function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateRecordsToDatabase("Error: Location Query Validation Failed",
                cursor, insertOrderedTestValues);

        // Move the cursor to demonstrate that there is only one record in the database
//        assertFalse("Error: More than one record returned from location query",
//                cursor.moveToNext());

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return insertOrderedTestValues;
    }
}

