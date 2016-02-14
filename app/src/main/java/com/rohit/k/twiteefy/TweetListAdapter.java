package com.rohit.k.twiteefy;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.List;

/**
 * Tweet list recycler view adapter
 * Created by Rohit Kulkarni on 10/02/16.
 */
public final class TweetListAdapter extends RecyclerView.Adapter<TweetListAdapter.TweetItemVH> {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Tweet> tweetItems;
    private String query;

    public TweetListAdapter(final Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void updateQuery(final String query, List<Tweet> tweets) {
        this.query = query.toLowerCase();
        this.tweetItems = new ArrayList<>(tweets);
        notifyDataSetChanged();
    }

    public void addNext(List<Tweet> tweets) {
        if (tweetItems == null) {
            tweetItems = new ArrayList<>();
            tweetItems.addAll(tweets);
        } else {
            tweetItems.addAll(0, tweets);
        }
        notifyItemRangeInserted(0, tweets.size());
    }

    @Override
    public TweetItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_tweet_item, parent, false);
        return new TweetItemVH(view);
    }

    @Override
    public void onBindViewHolder(TweetItemVH holder, int position) {
        Tweet tweet = tweetItems.get(position);
        SpannableString text = boldHashtags(tweet.text);
        holder.title.setText(text);
    }

    private SpannableString boldHashtags(CharSequence text) {
        final SpannableString spStr = new SpannableString(text);
        boolean hashStarted = false;
        int start = 0, i;
        for (i = 0; i < text.length(); i++) {
            if (hashStarted) {
                if (text.charAt(i) == ' ') {
                    hashStarted = false;
                    applySpan(spStr, start, i);
                    start = 0;
                }
            } else {
                if (text.charAt(i) == '#') {
                    start = i;
                    hashStarted = true;
                }
            }
        }
        if (hashStarted) {
            applySpan(spStr, start, i);
        }
        return spStr;
    }

    private void applySpan(final SpannableString spStr, int start, int end) {
        spStr.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        String tag = spStr.subSequence(start, end).toString().toLowerCase();
////        if (tag.contains(query)) {
////            int color = ContextCompat.getColor(context, R.color.colorAccent);
////            spStr.setSpan(new ForegroundColorSpan(color), start, start + query.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////        }
    }

    @Override
    public int getItemCount() {
        return tweetItems == null ? 0 : tweetItems.size();
    }

    public static final class TweetItemVH extends RecyclerView.ViewHolder {

        private TextView title;

        public TweetItemVH(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.tweet_title);
        }
    }
}
