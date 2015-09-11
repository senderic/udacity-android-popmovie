package com.ericsender.android_nanodegree.popmovie.Application;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Eric on 9/11/2015.
 */
public class MyStateManager {
    final AtomicBoolean doGridRefresh = new AtomicBoolean();

    MyStateManager() {
    }

    public Object getState(STATE state) {
        switch (state) {
            case REFRESH_GRID:
                return doGridRefresh;
            default:
                throw new UnsupportedOperationException("State note implemented: " + state);
        }
    }
}
