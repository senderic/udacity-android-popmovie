package com.ericsender.android_nanodegree.popmovie.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

/**
 * Created by Eric K. Sender on 9/29/2015.
 * Adapter from http://stackoverflow.com/a/9704820/1582712
 */
public class ShowAnimation extends Animation {
    float targetWeight;
    View view;
    // boolean doRun = true;

    public ShowAnimation(View view, float targetWeight, long durationMillis) {
        this.view = view;
        this.targetWeight = targetWeight;
        //if (doRun = targetWeight <= 0f) view.animate().translationY(0f);
        //else
        this.setDuration(durationMillis);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        //if (doRun)
        ((LinearLayout.LayoutParams) view.getLayoutParams()).weight = targetWeight * interpolatedTime;
        view.requestLayout();

    }

    @Override
    public void initialize(int width, int height, int parentWidth,
                           int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}