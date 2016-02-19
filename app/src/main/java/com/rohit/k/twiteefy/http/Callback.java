package com.rohit.k.twiteefy.http;

/**
 * Callback which is called after execution of any async task
 * Created by Rohit on 2/19/2016.
 */
public interface Callback<T> {
    void onSuccess(T data);

    void onFailure(String error);
}
