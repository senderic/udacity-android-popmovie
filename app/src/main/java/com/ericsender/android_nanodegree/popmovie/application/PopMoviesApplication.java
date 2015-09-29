package com.ericsender.android_nanodegree.popmovie.application;

import android.app.Application;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Eric on 9/11/2015.
 */
public class PopMoviesApplication extends Application {
    public final State STATE = State.getInstance();

    // TODO: can the constructor be private...?

    public static class State {
        private final AtomicBoolean isRefreshGrid = new AtomicBoolean();
        private final AtomicBoolean isTwoPane = new AtomicBoolean();

        public void setTwoPane(boolean b) {
            isTwoPane.set(b);
        }

        public boolean getTwoPane() {
            return isTwoPane.get();
        }

        public boolean getIsRefreshGrid() {
            return isRefreshGrid.get();
        }

        public void setIsRefreshGrid(boolean b) {
            isRefreshGrid.set(b);
        }

        private static class SingletonHolder {
            private static final State INSTANCE = new State();
        }

        public static State getInstance() {
            return SingletonHolder.INSTANCE;
        }
    }

}