package com.rohit.k.twiteefy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Search;

public final class MainActivity extends AppCompatActivity {

    private static final String DEFAULT_QUERY = "#India";

    private static final String TOKEN_KEY = "token";
    private static final String SECRET_KEY = "token_secret";

    private Toolbar toolbar;
    private RecyclerView tweetList;
    private GridLayoutManager layoutManager;
    private TweetListAdapter adapter;
    private FloatingActionButton fab;
    private TwitterLoginButton loginButton;

    private String accessToken;
    private String accessTokenSecret;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        tweetList = (RecyclerView) findViewById(R.id.tweets_list);
        layoutManager = new GridLayoutManager(this, 1);
        adapter = new TweetListAdapter(this);
        tweetList.setLayoutManager(layoutManager);
        tweetList.setAdapter(adapter);


        accessToken = preferences.getString(TOKEN_KEY, null);
        accessTokenSecret = preferences.getString(SECRET_KEY, null);

        updateLoginUI();

        if (accessToken != null)
            search(DEFAULT_QUERY);


        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                TwitterSession session = Twitter.getSessionManager().getActiveSession();
                if (session == null) {
                    showError("Authorize failed");
                    return;
                }
                TwitterAuthToken authToken = session.getAuthToken();
                if (authToken == null) {
                    showError("Authorize failed");
                    return;
                }
                accessToken = authToken.token;
                accessTokenSecret = authToken.secret;

                preferences.edit().putString(TOKEN_KEY, accessToken).apply();
                preferences.edit().putString(SECRET_KEY, accessTokenSecret).apply();

                updateLoginUI();
                //Default search
                search(DEFAULT_QUERY);
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                if (exception.getMessage() != null)
                    showError(exception.getMessage());
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call search api
            }
        });

    }

    private void search(final String query) {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        twitterApiClient.getSearchService().tweets(query, null, "en", null, "recent", 20,
                null, 0L, Long.MAX_VALUE, true, new Callback<Search>() {
                    @Override
                    public void success(Result<Search> result) {
                        adapter.updateQuery(query);
                        adapter.setTweets(result.data.tweets);
                        toolbar.setTitle(query);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        e.getMessage();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (loginButton != null)
            loginButton.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Displays UI according to the state of the twitter login status
     */
    private void updateLoginUI() {
        if (accessToken == null) {
            loginButton.setVisibility(View.VISIBLE);
            tweetList.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        } else {
            loginButton.setVisibility(View.GONE);
            tweetList.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Displays error message in a snack bar
     *
     * @param error Error message to show on UI
     */
    private void showError(String error) {
        Snackbar.make(fab, error, Snackbar.LENGTH_SHORT).show();
    }
}
