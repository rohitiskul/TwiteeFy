package com.rohit.k.twiteefy;

import com.rohit.k.twiteefy.model.Tweet;

import java.util.ArrayList;

/**
 * Callback extension for default twitter callback
 * Created by Rohit on 2/14/2016.
 */
public interface SearchCallback {

    void success(ArrayList<Tweet> result, boolean isNewQuery);

    void failure(String e);

}
