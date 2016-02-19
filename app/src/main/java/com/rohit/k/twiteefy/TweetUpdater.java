package com.rohit.k.twiteefy;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.rohit.k.twiteefy.http.Callback;
import com.rohit.k.twiteefy.http.HttpManager;
import com.rohit.k.twiteefy.http.SearchTweets;
import com.rohit.k.twiteefy.model.Tweet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * To update feeds periodically
 * Created by Rohit on 2/14/2016.
 */
public final class TweetUpdater {

    private static final int INTERVAL = 10 * 1000;

    private WeakReference<Activity> activityWeakReference;
    private Handler updaterHandler;
    private Runnable updater;
    private boolean isNewQuery;
    private String query;
    private long sinceId;
    private SearchCallback callback;

    public TweetUpdater(final Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
        updaterHandler = new Handler();
        updater = new Runnable() {
            @Override
            public void run() {
                callSearchAPI();
            }
        };
    }

    public void startUpdater(String query, SearchCallback callback) {
        this.query = query;
        this.callback = callback;
        //Stop if it is already running
        resetUpdater();
        updaterHandler.post(updater);
    }

    private void callSearchAPI() {
        //Always takes the instance with latest states.
        final Activity activity = activityWeakReference.get();
        if (activity == null)
            return;
        final String accessToken = PreferenceManager.getDefaultSharedPreferences(activity).getString(MainActivity.TOKEN_KEY, null);
        if (accessToken == null) {
            callback.failure(HttpManager.TOKEN_ERROR);
            return;
        }
        new SearchTweets(activity, accessToken, query, sinceId, new Callback<ArrayList<Tweet>>() {

            @Override
            public void onSuccess(ArrayList<Tweet> result) {
                if (result.size() > 0) {
                    sinceId = result.get(0).id;
                }
                callback.success(result, isNewQuery);
                //Its only refresh tweets after this point, so query will be same afterwards
                isNewQuery = false;
                updaterHandler.postDelayed(updater, INTERVAL);
            }

            @Override
            public void onFailure(String error) {
                callback.failure(error);
                //Updater will stop in case of error
                resetUpdater();
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void resetUpdater() {
        isNewQuery = true;
        sinceId = 0L;
        updaterHandler.removeCallbacks(updater);
    }
}
