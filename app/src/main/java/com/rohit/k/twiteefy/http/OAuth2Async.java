package com.rohit.k.twiteefy.http;

import android.app.Activity;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * OAuth2 token GET async task
 * Created by Rohit on 2/19/2016.
 */
public final class OAuth2Async extends AsyncTask<Void, Void, String> {

    private WeakReference<Activity> activityWeakReference;
    private Callback<String> callback;

    public OAuth2Async(final Activity activity, Callback<String> callback) {
        activityWeakReference = new WeakReference<>(activity);
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return HttpManager.oauth2Token();
    }

    @Override
    protected void onPostExecute(String token) {
        if (activityWeakReference.get() == null)
            return;
        if (token.equals(HttpManager.ERROR)) {
            callback.onFailure(token);
        } else {
            callback.onSuccess(token);
        }
    }
}
