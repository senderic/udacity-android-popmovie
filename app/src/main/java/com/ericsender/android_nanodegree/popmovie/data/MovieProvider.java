package com.ericsender.android_nanodegree.popmovie.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Movie;
import android.net.Uri;

import com.ericsender.android_nanodegree.popmovie.parcelable.MovieGridObj;
import com.ericsender.android_nanodegree.popmovie.utils.Utils;

import java.security.InvalidParameterException;

public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101;
    static final int MOVIE_FAVORITE = 200;
    static final int MOVIE_RATING = 300;
    static final int MOVIE_POPULAR = 400;

    private static final SQLiteQueryBuilder sMovieById;
    private static final SQLiteQueryBuilder sFavoriteMovies;
    private static final SQLiteQueryBuilder sHighRatedMovies;
    private static final SQLiteQueryBuilder sPopularMovies;

    static {
        // TODO: do I need to implement this?
        sMovieById = new SQLiteQueryBuilder();
        sFavoriteMovies = new SQLiteQueryBuilder();
        // TODO: implement below two once I know sFavoriteMovies is working
        sHighRatedMovies = new SQLiteQueryBuilder();
        sPopularMovies = new SQLiteQueryBuilder();

        sFavoriteMovies.setTables(MovieContract.MovieEntry.TABLE_NAME
                + " INNER JOIN " +
                MovieContract.FavoriteEntry.TABLE_NAME
                + " ON "
                + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
                + " = "
                + MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID);
    }

    // movie._id = ?
    private static final String sMovieSelection = MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
    private static final String sSelectAllFavorites = MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID;

    private Cursor getFavoriteMovies() { // Uri uri, String[] projection, String[] selectionArgs) {
        return mOpenHelper.getReadableDatabase().rawQuery("SELECT "
                + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_JSON
                + " FROM " + MovieContract.MovieEntry.TABLE_NAME
                + " WHERE " + MovieContract.MovieEntry.COLUMN_MOVIE_ID
                + " IN (SELECT "
                + MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID
                + " FROM " + MovieContract.FavoriteEntry.TABLE_NAME
                + " WHERE TRUE ORDER BY " + MovieContract.FavoriteEntry._ID + " desc)", null);

        // TODO: can this be made to work with subqueries / " IN ( ... ) " expressions?
//        return sFavoriteMovies.query(mOpenHelper.getReadableDatabase(),
//                projection,
//                sMovieSelection,
//                selectionArgs,
//                null,
//                null,
//                MovieContract.FavoriteEntry._ID + " desc");
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        // matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        // Get a movie by id
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        // Get all movies marked favorite (should not be limited)
        matcher.addURI(authority, MovieContract.PATH_FAVORITE, MOVIE_FAVORITE);
        // Get all movies marked as highest rated (should be limited to 20)
        matcher.addURI(authority, MovieContract.PATH_RATING, MOVIE_RATING);
        // Get all movies marked as most popular (should be limited to 20)
        matcher.addURI(authority, MovieContract.PATH_POPULAR, MOVIE_POPULAR);

        return matcher;
    }

    public MovieProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (selection == null) selection = "1";
        switch (match) {
            case MOVIE_FAVORITE:
                rowsDeleted = db.delete(MovieContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown/Unimplemented uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_FAVORITE:
                return MovieContract.FavoriteEntry.CONTENT_TYPE;
            case MOVIE_RATING:
                return MovieContract.RatingEntry.CONTENT_TYPE;
            case MOVIE_POPULAR:
                return MovieContract.PopularEntry.CONTENT_TYPE;
        }
        throw new InvalidParameterException("Unknown uri/match: " + uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        db.beginTransaction();
        try {
            switch (match) {
                case MOVIE_FAVORITE:
                    long _id = insertMovie(values);
                    if (_id > 0) {
                        ContentValues v = new ContentValues(1);
                        MovieGridObj o = (MovieGridObj) Utils.deserialize(values.getAsByteArray(MovieContract.MovieEntry.COLUMN_JSON));
                        // TODO: handle if o is null or o.id is null?
                        v.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID, o.id.longValue());
                        long _fid = insertFavorite(values);
                        if (_fid > 0)
                            returnUri = MovieContract.FavoriteEntry.buildFavoriteUri(_fid);
                        else
                            throw new android.database.SQLException("Failed to insert row into favorites " + uri);
                        db.setTransactionSuccessful();
                    } else
                        throw new android.database.SQLException("Failed to insert row into into movies " + uri);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown/Unimplemented uri " + uri);
            }
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    private long insertFavorite(ContentValues values) {
        return mOpenHelper.getWritableDatabase().insert(MovieContract.FavoriteEntry.TABLE_NAME, null, values);
    }

    private long insertMovie(ContentValues values) {
        return mOpenHelper.getWritableDatabase().insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIE_FAVORITE:
                retCursor = getFavoriteMovies();
                break;
            default:
                throw new UnsupportedOperationException("Unknown/Unimplemented uri: " + uri);
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}