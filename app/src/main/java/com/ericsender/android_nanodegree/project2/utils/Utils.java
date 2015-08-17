package com.ericsender.android_nanodegree.project2.utils;

import android.content.Context;
import android.content.res.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by g56147 on 8/11/2015.
 */
public class Utils {
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(Object.class, new NaturalDeserializer()).create();

    /*
        * http://stackoverflow.com/a/18387977/1582712
        */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static final Gson getGson() {
        return gson;
    }
}
