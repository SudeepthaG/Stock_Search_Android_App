package com.example.stocks;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AutoSuggestApi {
    private static AutoSuggestApi autoSuggestApi;
    private RequestQueue requestQueue;
    private static Context context;

    public AutoSuggestApi(Context ctx) {
        context = ctx;
        requestQueue = getRequestQueue();
    }

    public static synchronized AutoSuggestApi getInstance(Context context) {
        if (autoSuggestApi == null) {
            autoSuggestApi = new AutoSuggestApi(context);
        }
        return autoSuggestApi;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static void make(Context ctx, String query, Response.Listener<String>
            listener, Response.ErrorListener errorListener) {
        String url = "http://responsive-env.eba-9bzm8k2k.us-east-1.elasticbeanstalk.com/autocomplete/" + query;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                listener, errorListener) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put(Constants.SUBSCRIPTION_KEY_HEADER, Constants.SUBSCRIPTION_KEY_VALUE);
//                return headers;
//            }
        };
        AutoSuggestApi.getInstance(ctx).addToRequestQueue(stringRequest);
    }
}
