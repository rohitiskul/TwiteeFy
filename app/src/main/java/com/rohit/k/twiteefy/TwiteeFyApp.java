package com.rohit.k.twiteefy;

import android.app.Application;

/**
 * TwiteeFy app
 * Created by Rohit Kulkarni on 10/02/16.
 */
public final class TwiteeFyApp extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    public static final String TWITTER_KEY = "8ciGQCes5NOizKbCj42wiVLd9";
    public static final String TWITTER_SECRET = "OAERYAzMdfFnEOgWRaTQqE2VTwiDjI7OvkFlvYYlm1QTtdRiUZ";
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
