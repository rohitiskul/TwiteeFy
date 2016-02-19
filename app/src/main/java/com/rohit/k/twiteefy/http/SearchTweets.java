package com.rohit.k.twiteefy.http;

import android.app.Activity;
import android.os.AsyncTask;

import com.rohit.k.twiteefy.model.Tweet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * To search tweets using twitter rest api
 * Created by Rohit on 2/19/2016.
 */
public final class SearchTweets extends AsyncTask<Void, Void, ArrayList<Tweet>> {

    private final WeakReference<Activity> activityWeakReference;
    private final Callback<ArrayList<Tweet>> callback;
    private final String accessToken;
    private final String query;
    private final long sinceId;

    public SearchTweets(final Activity activity, final String accessToken, final String query, final long sinceId, final Callback<ArrayList<Tweet>> callback) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.accessToken = accessToken;
        this.query = query;
        this.sinceId = sinceId;
        this.callback = callback;
    }

    @Override
    protected ArrayList<Tweet> doInBackground(Void... voids) {
        return HttpManager.searchTweets(accessToken, query, 20, sinceId, Long.MAX_VALUE);
    }

    @Override
    protected void onPostExecute(ArrayList<Tweet> data) {
        if (activityWeakReference.get() == null)
            return;
        if (data == null) {
            callback.onFailure(HttpManager.ERROR);
        } else {
            callback.onSuccess(data);
        }
    }
}
