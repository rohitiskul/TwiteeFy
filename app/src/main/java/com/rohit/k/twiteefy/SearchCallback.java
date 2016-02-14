package com.rohit.k.twiteefy;

import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;

/**
 * Callback extension for default twitter callback
 * Created by Rohit on 2/14/2016.
 */
public interface SearchCallback {

    void success(Result<Search> result, boolean isNewQuery);

    void failure(TwitterException e);

}
