package com.rohit.k.twiteefy;

import android.app.Application;

/**
 * TwiteeFy app
 * Created by Rohit Kulkarni on 10/02/16.
 */
public final class TwiteeFyApp extends Application {

    private static TwiteeFyApp instance;

    public TwiteeFyApp() {
        instance = this;
    }

    public static TwiteeFyApp instance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
