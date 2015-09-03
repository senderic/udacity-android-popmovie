package com.ericsender.android_nanodegree.popmovie.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Eric K. Sender on 9/1/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_JSON + " BLOB NOT NULL, " +
                " UNIQUE (" + MovieContract.MovieEntry._ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_POPULAR_TABLE = "CREATE TABLE " + MovieContract.PopularEntry.TABLE_NAME + " (" +
                MovieContract.PopularEntry._ID + " INTEGER PRIMARY KEY," +
                MovieContract.PopularEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL, " +
                " FOREIGN KEY (" + MovieContract.PopularEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieContract.MovieEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + "), " +
                " UNIQUE (" + MovieContract.PopularEntry.COLUMN_MOVIE_ID + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_RATING_TABLE = "CREATE TABLE " + MovieContract.RatingEntry.TABLE_NAME + " (" +
                MovieContract.RatingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.RatingEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + MovieContract.RatingEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieContract.RatingEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + "), " +
                " UNIQUE (" + MovieContract.RatingEntry.COLUMN_MOVIE_ID + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + MovieContract.FavoriteEntry.TABLE_NAME + " (" +
                MovieContract.FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + MovieContract.RatingEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieContract.FavoriteEntry.TABLE_NAME + " (" + MovieContract.MovieEntry._ID + "), " +
                " UNIQUE (" + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + ") ON CONFLICT IGNORE);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_POPULAR_TABLE);
        db.execSQL(SQL_CREATE_RATING_TABLE);
        db.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.PopularEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.FavoriteEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.RatingEntry.TABLE_NAME);
        onCreate(db);
    }
}
