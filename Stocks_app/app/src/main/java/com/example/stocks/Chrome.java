package com.example.stocks;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class Chrome {
    public static void launchActivity(View view, String url) {
//        String twitterInterUrl = Twitter.Constants.TWITTER_URL + "?" + Twitter.Constants.HASHTAG_QUERY_PARAM +
//                "&" + Twitter.Constants.TEXT_QUERY_PARAM + Twitter.Constants.DEFAULT_TEXT + url;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
    }
}
