package com.example.stocks;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class Twitter {
    private static class Constants {
        public static final String HASHTAG_QUERY_PARAM = "hashtags=CSCI571News";
        public static final String TWITTER_URL = "https://twitter.com/intent/tweet";
        public static final String TEXT_QUERY_PARAM = "text=";
        public static final String DEFAULT_TEXT = "Check out this Link: ";
    }

    public static void launchActivity(View view, String url) {
        String twitterInterUrl = Constants.TWITTER_URL + "?" + Constants.HASHTAG_QUERY_PARAM +
                "&" + Constants.TEXT_QUERY_PARAM + Constants.DEFAULT_TEXT + url;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterInterUrl));
        view.getContext().startActivity(intent);
    }
}
