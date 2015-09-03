package com.ericsender.android_nanodegree.popmovie.com.ericsender.android_nanodegree.popmovie;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by Eric K. Sender on 9/2/2015.
 * Adapted from Udacity Sunshine App
 */
public class FullTestSuite extends TestSuite {
    public static Test suite() {
        return new TestSuiteBuilder(FullTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }

    public FullTestSuite() {
        super();
    }
}
