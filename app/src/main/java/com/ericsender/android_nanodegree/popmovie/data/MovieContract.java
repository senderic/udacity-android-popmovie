package com.ericsender.android_nanodegree.popmovie.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Eric K. Sender on 9/1/2015.
 */
public class MovieContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.ericsender.android_nanodegree.popmovie";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FAVORITE = "favorite";
    public static final String PATH_POPULAR = "popular";
    public static final String PATH_RATING = "rating";
    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ISFAV_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = PATH_MOVIE;
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_BLOB = "details_serializedParseableJson";
        public static final String COLUMN_MOVIE_TRAILERS = "trailers_serializedParseableJson";
        public static final String COLUMN_MOVIE_REVIEWS = "reviews_serializedParseableJson";
        public static final String COLUMN_MOVIE_MINUTES = "minutes";

        public static Uri buildUri(Long movie_id) {
            return CONTENT_URI.buildUpon().appendPath(movie_id.toString()).build();
        }

        public static Uri buildUriUnionFavorite(Long movie_id) {
            return CONTENT_URI.buildUpon().appendPath("isFav").appendPath(movie_id.toString()).build();
        }

        public static Uri buildUriReviews(Long movie_id) {
            return CONTENT_URI.buildUpon().appendPath("review").appendPath(movie_id.toString()).build();
        }

        public static Uri buildUriTrailers(Long movie_id) {
            return CONTENT_URI.buildUpon().appendPath("trailer").appendPath(movie_id.toString()).build();
        }

        public static Uri buildUriMinutes(Long movie_id) {
            return CONTENT_URI.buildUpon().appendPath("minute").appendPath(movie_id.toString()).build();
        }

        public static Uri buildUri() {
            return CONTENT_URI;
        }
    }

    public static final class PopularEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POPULAR).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POPULAR;

        public static final String TABLE_NAME = PATH_POPULAR;
        public static final String COLUMN_MOVIE_ID = MovieEntry.COLUMN_MOVIE_ID;

        public static Uri buildUri() {
            return CONTENT_URI;
        }
    }

    public static final class FavoriteEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        public static final String TABLE_NAME = PATH_FAVORITE;
        public static final String COLUMN_MOVIE_ID = MovieEntry.COLUMN_MOVIE_ID;

        public static Uri buildUri() {
            return CONTENT_URI; // This will return all the favorites.
        }

        public static Uri buildUri(Long movie_id) {
            return CONTENT_URI.buildUpon().appendPath(movie_id.toString()).build();
        }
    }

    public static final class RatingEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RATING).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATING;

        public static final String TABLE_NAME = PATH_RATING;
        public static final String COLUMN_MOVIE_ID = MovieEntry.COLUMN_MOVIE_ID;

        public static Uri buildUri() {
            return CONTENT_URI;
        }

        public static String getMovieListFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}