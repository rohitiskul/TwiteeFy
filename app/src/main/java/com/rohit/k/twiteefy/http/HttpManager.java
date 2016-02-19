package com.rohit.k.twiteefy.http;

import android.util.Base64;
import android.util.Pair;

import com.rohit.k.twiteefy.TwiteeFyApp;
import com.rohit.k.twiteefy.model.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Does all the http communication
 * Created by Rohit on 2/17/2016.
 */
public final class HttpManager {

    public static final String TOKEN_ERROR = "You need to login first";
    public static final String ERROR = "Error communicating with server";
    private static final String HOST = "api.twitter.com";
    private static final String ENDPOINT = "https://" + HOST;
    private static final String VERSION = "1.1";
    private static final String UA = "TwiteeFy v1.0";
    private static final String CHARSET = "UTF-8";

    public static String oauth2Token() {
        try {
            final String credentials = TwiteeFyApp.TWITTER_KEY + ":" + TwiteeFyApp.TWITTER_SECRET;
            String encodedCred = Base64.encodeToString(credentials.getBytes("UTF-8"), Base64.NO_WRAP);

            final URL url = new URL(ENDPOINT + "/oauth2/token");
            final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            //Headers
            conn.setRequestProperty("Host", HOST);
            conn.setRequestProperty("User-Agent", UA);
            conn.setRequestProperty("Authorization", "Basic " + encodedCred);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            //Post params
            final ArrayList<Pair<String, String>> params = new ArrayList<>();
            params.add(new Pair<>("grant_type", "client_credentials"));
            final String encodedParams = encodeParams(params);

            final OutputStream os = conn.getOutputStream();
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            writer.write(encodedParams);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = "", line;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }
                final JSONObject jsonResp = new JSONObject(response);
                return jsonResp.getString("access_token");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return ERROR;
    }

    public static ArrayList<Tweet> searchTweets(String accessToken, String query, int count, long sinceId, long maxId) {
        try {
            query = URLEncoder.encode(query, CHARSET);
            final URL url = new URL(ENDPOINT + //"/1.1/statuses/user_timeline.json?count=100&screen_name=twitterapi");
                    "/" + VERSION + "/search/tweets.json?q=" + query +
                    "&since_id=" + sinceId +
                    "&max_id=" + maxId +
                    "&result_type=recent" +
                    "&count=" + count +
                    "&include_entities=false");
            final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            //Headers
            conn.setRequestProperty("Host", HOST);
            conn.setRequestProperty("User-Agent", UA);
            conn.setRequestProperty("cache-control", "no-cache");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.connect();
            String response = "", line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = bufferedReader.readLine()) != null) {
                response += line;
            }
            final JSONObject jsonResp = new JSONObject(response);
            final JSONArray jsonDataArray = jsonResp.getJSONArray("statuses");
            JSONObject object;
            final ArrayList<Tweet> tweets = new ArrayList<>();
            for (int i = 0; i < jsonDataArray.length(); i++) {
                object = jsonDataArray.getJSONObject(i);
                long id = object.getLong("id");
                final Tweet tweet = new Tweet();
                tweet.id = id;
                tweet.text = object.getString("text");
                tweets.add(tweet);
            }
            return tweets;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String encodeParams(final ArrayList<Pair<String, String>> params) throws UnsupportedEncodingException {
        final StringBuilder builder = new StringBuilder();
        for (Pair<String, String> param : params) {
            builder.append(URLEncoder.encode(param.first, CHARSET));
            builder.append("=");
            builder.append(URLEncoder.encode(param.second, CHARSET));
            builder.append("&");
        }
        return builder.substring(0, builder.length() - 1);
    }

}
