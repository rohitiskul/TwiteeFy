package com.rohit.k.twiteefy;

import android.os.Handler;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;

/**
 * To update feeds periodically
 * Created by Rohit on 2/14/2016.
 */
public final class TweetUpdater {

    private static final int INTERVAL = 10 * 1000;

    private Handler updaterHandler;
    private Runnable updater;
    private TwitterApiClient twitterApiClient;
    private boolean isNewQuery;
    private String query;
    private long sinceId;
    private SearchCallback callback;


    public TweetUpdater() {
        twitterApiClient = TwitterCore.getInstance().getApiClient();
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
        twitterApiClient.getSearchService().tweets(query, null, "en", null, "recent", 20,
                null, sinceId, Long.MAX_VALUE, true, new Callback<Search>() {
                    @Override
                    public void success(Result<Search> result) {
                        if (result != null && result.data != null && result.data.tweets != null && result.data.tweets.size() > 0) {
                            Tweet tweet = result.data.tweets.get(0);
                            sinceId = tweet.id;
                        }
                        callback.success(result, isNewQuery);
                        //Its only refresh tweets after this point, so query will be same afterwards
                        isNewQuery = false;
                        updaterHandler.postDelayed(updater, INTERVAL);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        callback.failure(e);
                        //Updater will stop in case of error
                        resetUpdater();
                    }
                });
    }

    public void resetUpdater() {
        isNewQuery = true;
        sinceId = 0L;
        updaterHandler.removeCallbacks(updater);
    }
}
