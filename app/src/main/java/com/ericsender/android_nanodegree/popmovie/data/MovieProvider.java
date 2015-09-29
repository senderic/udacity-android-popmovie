package com.ericsender.android_nanodegree.popmovie.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.security.InvalidParameterException;

public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String LOG_TAG = "MovieProvider";
    private MovieDbHelper mOpenHelper;

    public static final int MOVIE = 100;
    public static final int MOVIE_WITH_ID = 101;
    public static final int MOVIE_WITH_ID_AND_MAYBE_FAVORITE = 102;
    public static final int MOVIE_FAVORITE = 200;
    public static final int MOVIE_FAVORITE_WITH_ID = 201;
    public static final int MOVIE_RATING = 300;
    public static final int MOVIE_POPULAR = 400;

    private static final SQLiteQueryBuilder sMoviesAll;
    private static final SQLiteQueryBuilder sMovieById;
    private static final SQLiteQueryBuilder sFavoriteMovies;
    private static final SQLiteQueryBuilder sHighRatedMovies;
    private static final SQLiteQueryBuilder sPopularMovies;

    private static final SQLiteQueryBuilder sFavoriteMovie;

    static {
        // TODO: do I need to implement this?
        sMoviesAll = new SQLiteQueryBuilder();
        sMovieById = new SQLiteQueryBuilder();
        sFavoriteMovies = new SQLiteQueryBuilder();
        sFavoriteMovie = new SQLiteQueryBuilder();
        // TODO: implement below two once I know sFavoriteMovies is working
        sHighRatedMovies = new SQLiteQueryBuilder();
        sPopularMovies = new SQLiteQueryBuilder();

        sMoviesAll.setTables(MovieContract.MovieEntry.TABLE_NAME);

        sFavoriteMovie.setTables(MovieContract.FavoriteEntry.TABLE_NAME);

        sFavoriteMovies.setTables(MovieContract.MovieEntry.TABLE_NAME
                + " INNER JOIN " +
                MovieContract.FavoriteEntry.TABLE_NAME
                + " ON "
                + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
                + " = "
                + MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID);

        sMovieById.setTables(MovieContract.MovieEntry.TABLE_NAME);
    }

    // movie._id = ?
    private static final String sMovieSelection = MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
    private static final String sSelectAllFavorites = MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID;

    private Cursor getAllMovies() {
        return sMoviesAll.query(mOpenHelper.getReadableDatabase(), null, null, null, null, null, null);
    }

    private Cursor getPopularMovies() {
        return mOpenHelper.getReadableDatabase().rawQuery("SELECT "
                + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " AS _id, "
                + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_BLOB
                + " FROM " + MovieContract.MovieEntry.TABLE_NAME
                + " WHERE " + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
                + " IN (SELECT "
                + MovieContract.PopularEntry.TABLE_NAME + "." + MovieContract.PopularEntry.COLUMN_MOVIE_ID
                + " FROM " + MovieContract.PopularEntry.TABLE_NAME
                + " WHERE 1 ORDER BY "
                + MovieContract.PopularEntry.TABLE_NAME + "." + MovieContract.PopularEntry._ID + " desc)", null);

    }

    private Cursor getMovieWithIdAndMaybeFavorite(String movie_id) {
        return mOpenHelper.getReadableDatabase().rawQuery("SELECT "
                        + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_BLOB
                        + " FROM " + MovieContract.MovieEntry.TABLE_NAME
                        + " WHERE " + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?"
                        + " UNION SELECT "
                        + MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID
                        + " FROM " + MovieContract.FavoriteEntry.TABLE_NAME
                        + " WHERE " + MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + "=?",
                new String[]{movie_id, movie_id});
    }

    private Cursor getRatedMovies() {
        return mOpenHelper.getReadableDatabase().rawQuery("SELECT "
                + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " AS _id, "
                + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_BLOB
                + " FROM " + MovieContract.MovieEntry.TABLE_NAME
                + " WHERE " + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
                + " IN (SELECT "
                + MovieContract.RatingEntry.TABLE_NAME + "." + MovieContract.RatingEntry.COLUMN_MOVIE_ID
                + " FROM " + MovieContract.RatingEntry.TABLE_NAME
                + " WHERE 1 ORDER BY "
                + MovieContract.RatingEntry.TABLE_NAME + "." + MovieContract.RatingEntry._ID + " desc)", null);
    }

    private Cursor getFavoriteMovieId(String movie_id) {
        return sFavoriteMovie.query(mOpenHelper.getReadableDatabase(),
                new String[]{MovieContract.FavoriteEntry._ID},
                MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + "=?",
                new String[]{movie_id},
                null, null, null);
    }

    private Cursor getMovieById(String movie_id) {
        return sMovieById.query(mOpenHelper.getReadableDatabase(),
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_BLOB},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                new String[]{movie_id},
                null, null, null);
    }

    private Cursor getFavoriteMovies() { // Uri uri, String[] projection, String[] selectionArgs) {
        return mOpenHelper.getReadableDatabase().rawQuery("SELECT "
                + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " AS _id, "
                + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_BLOB
                + " FROM " + MovieContract.MovieEntry.TABLE_NAME
                + " WHERE " + MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
                + " IN (SELECT "
                + MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID
                + " FROM " + MovieContract.FavoriteEntry.TABLE_NAME
                + " WHERE 1 ORDER BY "
                + MovieContract.FavoriteEntry.TABLE_NAME + "." + MovieContract.FavoriteEntry._ID + " desc)", null);

        // TODO: can this be made to work with subqueries / " IN ( ... ) " expressions?
//        return sFavoriteMovies.query(mOpenHelper.getReadableDatabase(),
//                projection,
//                sMovieSelection,
//                selectionArgs,
//                null,
//                null,
//                MovieContract.FavoriteEntry._ID + " desc");
    }

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        // matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        // Get all movies
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        // Get a movie by id
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        // Get movie and also a row if its a favorite // TODO using "*" instead of "#" to handle negatives
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/isFav/#", MOVIE_WITH_ID_AND_MAYBE_FAVORITE);
        // Get all movies marked favorite (should not be limited)
        matcher.addURI(authority, MovieContract.PATH_FAVORITE, MOVIE_FAVORITE);
        // Get a favorite movie
        matcher.addURI(authority, MovieContract.PATH_FAVORITE + "/#", MOVIE_FAVORITE_WITH_ID);
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
            case MOVIE:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_FAVORITE:
                rowsDeleted = db.delete(MovieContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_POPULAR:
                rowsDeleted = db.delete(MovieContract.PopularEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_RATING:
                rowsDeleted = db.delete(MovieContract.RatingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("Unknown/Unimplemented match :: uri: %d / %s", match, uri));
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
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_FAVORITE:
                return MovieContract.FavoriteEntry.CONTENT_TYPE;
            case MOVIE_RATING:
                return MovieContract.RatingEntry.CONTENT_TYPE;
            case MOVIE_POPULAR:
                return MovieContract.PopularEntry.CONTENT_TYPE;
            default:
                throw new InvalidParameterException("Unknown uri/match: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        long _id;
        Uri returnUri;
        switch (match) {
            case MOVIE:
                _id = insertMovie(values);
                if (_id == -1)
                    throw new RuntimeException("Failed insert of values: " + values);
                returnUri = MovieContract.MovieEntry.buildMovieUri();
                break;
            case MOVIE_WITH_ID:
                _id = insertMovie(values);
                if (_id == -1)
                    throw new RuntimeException("Failed insert of values: " + values);
                returnUri = MovieContract.MovieEntry.buildMovieUri(values.getAsLong(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                break;
            case MOVIE_RATING:
                _id = insertRated(values);
                if (_id == -1)
                    throw new android.database.SQLException("Failed to insert row into into movies " + uri);
                returnUri = MovieContract.RatingEntry.buildRatingUri();
                break;
            case MOVIE_POPULAR:
                _id = insertPopular(values);
                if (_id == -1)
                    throw new android.database.SQLException("Failed to insert row into into movies " + uri);
                returnUri = MovieContract.PopularEntry.buildPopularUri();
                break;
            case MOVIE_FAVORITE:
                _id = insertFavorite(values);
                if (_id == -1)
                    throw new android.database.SQLException("Failed to insert row into into movies " + uri);
                returnUri = MovieContract.FavoriteEntry.buildFavoriteUri();
                break;
            default:
                throw new UnsupportedOperationException("Unknown/Unimplemented uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int retVal = super.bulkInsert(uri, values);
            if (retVal > 0) db.setTransactionSuccessful();
            return retVal;
        } finally {
            db.endTransaction();
        }
    }

    private long insertRated(ContentValues values) {
        try {
            return mOpenHelper.getWritableDatabase().insertOrThrow(MovieContract.RatingEntry.TABLE_NAME, null, values);
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            return -2L;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1L;
        }
    }

    private long insertPopular(ContentValues values) {
        try {
            return mOpenHelper.getWritableDatabase().insertOrThrow(MovieContract.PopularEntry.TABLE_NAME, null, values);
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            Log.e(LOG_TAG, String.format("Did not insert %d because of constraint (already exists)", values.getAsLong(MovieContract.PopularEntry.COLUMN_MOVIE_ID)));
            return -2L;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1L;
        }
    }

    private long insertFavorite(ContentValues values) {
        try {
            return mOpenHelper.getWritableDatabase().insertOrThrow(MovieContract.FavoriteEntry.TABLE_NAME, null, values);
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            return -2L;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1L;
        }
    }

    private long insertMovie(ContentValues values) {
        try {
            return mOpenHelper.getWritableDatabase().insertOrThrow(MovieContract.MovieEntry.TABLE_NAME, null, values);
        } catch (SQLiteConstraintException e) {
            return -2L;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1L;
        }
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
            case MOVIE:
                retCursor = getAllMovies();
                break;
            case MOVIE_WITH_ID:
                retCursor = getMovieById(uri.getLastPathSegment());
                break;
            case MOVIE_RATING:
                retCursor = getRatedMovies();
                break;
            case MOVIE_FAVORITE:
                retCursor = getFavoriteMovies();
                break;
            case MOVIE_FAVORITE_WITH_ID:
                retCursor = getFavoriteMovieId(uri.getLastPathSegment());
                break;
            case MOVIE_WITH_ID_AND_MAYBE_FAVORITE:
                retCursor = getMovieWithIdAndMaybeFavorite(uri.getLastPathSegment());
                break;
            case MOVIE_POPULAR:
                retCursor = getPopularMovies();
                break;
            default:
                throw new UnsupportedOperationException("Unknown/Unimplemented uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
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
