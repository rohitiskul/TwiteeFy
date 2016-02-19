package com.rohit.k.twiteefy;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rohit.k.twiteefy.http.Callback;
import com.rohit.k.twiteefy.http.OAuth2Async;
import com.rohit.k.twiteefy.model.Tweet;

import java.util.ArrayList;

public final class MainActivity extends AppCompatActivity {

    public static final String TOKEN_KEY = "token";
    private static final String DEFAULT_QUERY = "#India";
    private Toolbar toolbar;
    private RecyclerView tweetList;
    private GridLayoutManager layoutManager;
    private TweetListAdapter adapter;
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private TextView scrollToTopBtn;
    private Button loginButton;

    private String accessToken;

    private TweetUpdater tweetUpdater;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tweetUpdater = new TweetUpdater(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.tweets_list_progress);
        scrollToTopBtn = (TextView) findViewById(R.id.new_posts_button);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        loginButton = (Button) findViewById(R.id.login_button_native);
        tweetList = (RecyclerView) findViewById(R.id.tweets_list);
        int columns = getResources().getInteger(R.integer.tweet_list_columns);
        layoutManager = new GridLayoutManager(this, columns);
        adapter = new TweetListAdapter(this);
        tweetList.setLayoutManager(layoutManager);
        tweetList.setAdapter(adapter);


        accessToken = preferences.getString(TOKEN_KEY, null);

        updateLoginUI();

        if (accessToken != null)
            search(DEFAULT_QUERY);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new OAuth2Async(MainActivity.this, new Callback<String>() {
                    @Override
                    public void onSuccess(String token) {
                        accessToken = token;
                        preferences.edit().putString(TOKEN_KEY, accessToken).apply();

                        updateLoginUI();
                        //Default search
                        search(DEFAULT_QUERY);
                    }

                    @Override
                    public void onFailure(String error) {
                        showError("Authorize failed");

                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call search api
                MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                        .title("Search Tweets")
                        .customView(R.layout.layout_dialog_search_hashtag, false)
                        .positiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                View dialogView = dialog.getCustomView();
                                if (dialogView == null) {
                                    dialog.dismiss();
                                    return;
                                }
                                final EditText editText = (EditText) dialogView.findViewById(R.id.dialog_search_edit);
                                dialog.dismiss();
                                search(editText.getText().toString());
                            }
                        })
                        .build();
                dialog.show();
            }
        });

        scrollToTopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToTopBtn.setVisibility(View.GONE);
                layoutManager.smoothScrollToPosition(tweetList, null, 0);
            }
        });

    }

    private void search(final String query) {
        progressBar.setVisibility(View.VISIBLE);
        tweetUpdater.startUpdater(query, new SearchCallback() {
            @Override
            public void success(ArrayList<Tweet> result, boolean isNewQuery) {
                progressBar.setVisibility(View.GONE);
                if (isNewQuery) {
                    scrollToTopBtn.setVisibility(View.GONE);
                    adapter.updateQuery(query, result);
                    layoutManager.smoothScrollToPosition(tweetList, null, 0);
                } else if (result.size() > 0) {
                    scrollToTopBtn.setVisibility(View.VISIBLE);
                    adapter.addNext(result);
                }
                toolbar.setTitle(query);
            }

            @Override
            public void failure(String error) {
                progressBar.setVisibility(View.GONE);
                showError(error);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tweetUpdater.resetUpdater();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int columns = getResources().getInteger(R.integer.tweet_list_columns);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutManager.setSpanCount(columns);
        } else {
            layoutManager.setSpanCount(columns);
        }
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
