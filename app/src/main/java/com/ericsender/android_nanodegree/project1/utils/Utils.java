package com.ericsender.android_nanodegree.project1.utils;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by g56147 on 8/11/2015.
 */
public class Utils {
    /*
    * http://stackoverflow.com/a/18387977/1582712
    */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
