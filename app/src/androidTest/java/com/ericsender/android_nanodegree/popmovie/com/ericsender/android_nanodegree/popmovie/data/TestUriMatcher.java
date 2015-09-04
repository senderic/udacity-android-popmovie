/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ericsender.android_nanodegree.popmovie.com.ericsender.android_nanodegree.popmovie.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.ericsender.android_nanodegree.popmovie.data.MovieContract;
import com.ericsender.android_nanodegree.popmovie.data.MovieProvider;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {

    // content://com.ericsender.android_nanodegree.popmovie/movie/0
    private static final Uri TEST_MOVIE_ITEM = MovieContract.MovieEntry.buildMovieUri(0L);
    // content://com.ericsender.android_nanodegree.popmovie/movie
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.buildMovieUri();
    // content://com.ericsender.android_nanodegree.popmovie/favorite
    private static final Uri TEST_FAVORITE_DIR = MovieContract.FavoriteEntry.buildFavoriteUri();


    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The Movie URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_ITEM), MovieProvider.MOVIE_WITH_ID);
        assertEquals("Error: The Movie URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);
        assertEquals("Error: The Favorite URI was matched incorrectly.",
                testMatcher.match(TEST_FAVORITE_DIR), MovieProvider.MOVIE_FAVORITE);
    }
}
