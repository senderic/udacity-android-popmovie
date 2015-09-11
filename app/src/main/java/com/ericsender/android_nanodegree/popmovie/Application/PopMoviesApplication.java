package com.ericsender.android_nanodegree.popmovie.Application;

import android.app.Application;

/**
 * Created by Eric on 9/11/2015.
 */
public class PopMoviesApplication extends Application {
    private final MyStateManager myStateManager = new MyStateManager();

    public MyStateManager getStateManager() {
        return myStateManager;
    }
}