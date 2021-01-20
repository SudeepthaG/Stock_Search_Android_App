package com.example.stocks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skyhope.showmoretextview.ShowMoreTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private AutoSuggestAdapter autoSuggestAdapter;
    public String queryString;
    private Handler handler;
    private RequestQueue requestQueue;

    //  3  functions to refresh the page after every 15 seconds and pause this handler if the activity is changed
    Handler timehandler = new Handler();
    Runnable runnable;
    int delay = 15*1000; //Delay for 15 seconds.  One second = 1000 milliseconds.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Stocks);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        this.requestQueue = Volley.newRequestQueue(this);
        makeFavsVolleyCall();
    }

    private void makeFavsVolleyCall() {

        SharedPreferences myPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Map<String, ?> allEntries = myPrefs.getAll();
        ArrayList<String> allKeys = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            allKeys.add(entry.getKey());
        }
//        String allKeysStr = TextUtils.join(",", allKeys);
        SharedPreferences myShares = getSharedPreferences("myShares", MODE_PRIVATE);
        Map<String, ?> allShareEntries = myShares.getAll();
        ArrayList<String> allShareKeys = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allShareEntries.entrySet()) {
            allShareKeys.add(entry.getKey());
        }

        SharedPreferences cash = getSharedPreferences("FinalAmt", MODE_PRIVATE);
        final float[] myCash = {cash.getFloat("remainingAmt", 0.0f)};

        // get the tickers from the favsOrder file in order shared preferences
        SharedPreferences myOrder = getSharedPreferences("Order", MODE_PRIVATE);
        ArrayList<String> favsOrder = new ArrayList<>();

        Gson gson = new Gson();
        String favsJson = myOrder.getString("favsOrder", "");
        if (favsJson.isEmpty()) {
//            Toast.makeText(MainActivity.this,"There is something error",Toast.LENGTH_LONG).show();
        } else {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            List<String> arrPackageData = gson.fromJson(favsJson, type);
            favsOrder.addAll(arrPackageData);
        }
        ArrayList<String> sharesOrder = new ArrayList<>();
        String sharesJson = myOrder.getString("sharesOrder", "");
        if (sharesJson.isEmpty()) {
//            Toast.makeText(MainActivity.this,"There is something error",Toast.LENGTH_LONG).show();
        } else {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            List<String> arrPackageData = gson.fromJson(sharesJson, type);
            sharesOrder.addAll(arrPackageData);
        }



        Set<String> hash_Set = new HashSet<String>();


        hash_Set.addAll(favsOrder);
        hash_Set.addAll(sharesOrder);

//        hash_Set.addAll(allKeys);
//        hash_Set.addAll(allShareKeys);

        String keys = String.join(",", hash_Set);

//        ArrayList<String> allSharesKeys = new ArrayList<>();
//        allSharesKeys.add("TSLA");
//        allSharesKeys.add("MSFT");
        String stringUrl = "http://nodejsserver-env.eba-r2ztfbmm.us-east-1.elasticbeanstalk.com/watchlist/" + keys;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, stringUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject json = response;

                            ArrayList<watchShare> watches = new ArrayList<>();//the list for favorites
                            ArrayList<watchShare> shares = new ArrayList<>(); //the list for portfolio

                            for (int i=0; i<sharesOrder.size(); i++){
                                String ticker = sharesOrder.get(i);
                                JSONObject res = json.getJSONObject(ticker);
                                watchShare watchData = new watchShare();

                                watchData.setTicker(ticker);
                                int tempShares = myShares.getInt(ticker, 0);
                                if(tempShares > 1){
                                    watchData.setSubTitle( tempShares+ ".0 shares");
                                }
                                else{
                                    watchData.setSubTitle( tempShares+ ".0 share");
                                }
                                watchData.setLast((float) res.getDouble("last"));
                                watchData.setChange((float) res.getDouble("change"));
                                watchData.setChangeColor(res.getString("color"));
//                        watchData.setInPortfolio(false); not required for now
//                        watchData.setInFavs(true);
                                shares.add(watchData);

                                float val = (float) (myShares.getInt(ticker, 0) * res.getDouble("last"));
                                myCash[0] += val;
                            }

                            watchShare dummyWatch = new watchShare();
                            dummyWatch.setLast(myCash[0]);
                            shares.add(dummyWatch);

//                            shares.add(myCash[0]);

                            for (int i=0;i<favsOrder.size(); i++){
                                String ticker = favsOrder.get(i);
                                JSONObject res = json.getJSONObject(ticker);
                                watchShare watchData = new watchShare();

                                if (sharesOrder.contains(ticker)){
                                    watchData.setTicker(ticker);
                                    int tempShares = myShares.getInt(ticker, 0);
                                    if(tempShares > 1){
                                        watchData.setSubTitle( tempShares+ ".0 shares");
                                    }
                                    else{
                                        watchData.setSubTitle( tempShares+ ".0 share");
                                    }
                                    watchData.setLast((float) res.getDouble("last"));
                                    watchData.setChange((float) res.getDouble("change"));
                                    watchData.setChangeColor(res.getString("color"));
//                        watchData.setInPortfolio(false); not required for now
//                        watchData.setInFavs(true);
                                }
                                else{
                                    watchData.setTicker(ticker);
                                    watchData.setSubTitle(myPrefs.getString(ticker, null));
                                    watchData.setLast((float) res.getDouble("last"));
                                    watchData.setChange((float) res.getDouble("change"));
                                    watchData.setChangeColor(res.getString("color"));
                                }

                                watches.add(watchData);



                            }

                            setProgressInvisible();
                            getCurrentDate();
                            setRecyclerView(shares, watches);
                            setHyperLink();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,
                                    "the error is due to ticker being in caps",  Toast.LENGTH_LONG).show();
                            Log.e("VolleyException", "Error "+e.toString());

                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("Volley", "Error at Make Fav calls "+error.toString());


                    }
                });
        requestQueue.add(jsonObjectRequest);

    }


    @Override
    protected void onResume() {
        //start handler as activity become visible

        timehandler.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something
                makeFavsVolleyCall();
                Log.d("Refreshing 15 sec: ", new SimpleDateFormat(" HH:mm:ss z").format(new Date()));
                timehandler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause() {
        timehandler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    private void setProgressInvisible() {
        View v = findViewById(R.id.progressScreen);
        v.setVisibility(View.INVISIBLE);
    }

    private void setHyperLink() {
        TextView t2 = (TextView) findViewById(R.id.textView5);

        String val = "<html><a href=\"https://www.tiingo.com/\">Powered by Tiingo</a></html>";
        Spannable spannedText = (Spannable)
                Html.fromHtml(val);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable processedText = removeUnderlines(spannedText);
        t2.setText(processedText);
    }

    private Spannable removeUnderlines(Spannable p_Text) {
        URLSpan[] spans = p_Text.getSpans(0, p_Text.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = p_Text.getSpanStart(span);
            int end = p_Text.getSpanEnd(span);
            p_Text.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            p_Text.setSpan(span, start, end, 0);
        }
        return p_Text;
    }

    private void setRecyclerView(ArrayList<watchShare> shares, ArrayList<watchShare> watches) {


        RecyclerView recyclerView = findViewById(R.id.sectioned_recycler_view);
        recyclerView.setHasFixedSize(true); //just added
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        final ArrayList<Section> sections = new ArrayList<>();
        //add sections


        sections.add(new Section("PORTFOLIO", shares));


        sections.add(new Section("FAVORITES", watches));

        final SectionAdapter adapter = new SectionAdapter(this, sections);//may be this is called SectionRecyclerViewAdapter
        recyclerView.setAdapter(adapter);


    }


//
    private void getCurrentDate() {
        TextView date_TV=(TextView) findViewById(R.id.textView4);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMMM dd, yyyy");
        Date date = new Date();
        date_TV.setText(simpleDateFormat.format(date));
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) searchViewItem.getActionView();
        final SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        this.autoSuggestAdapter = new AutoSuggestAdapter(this, android.R.layout.simple_dropdown_item_1line);
        searchAutoComplete.setAdapter(this.autoSuggestAdapter);
        searchAutoComplete.setThreshold(3);

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                queryString = (String) adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText("" + queryString.split(" - ")[1]);
            }
        });

        searchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(1000);
                handler.sendEmptyMessageDelayed(1000,
                        2000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }


        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (queryString != null && queryString.length() > 0) {
                    Intent intent = new Intent(MainActivity.this, StockDetailActivity.class);
                    intent.putExtra("com.example.stocks.QUERY_STRING", queryString);
                    startActivity(intent);
                }
                if (autoSuggestAdapter != null) {
                    autoSuggestAdapter.setData(new ArrayList<String>());
                    autoSuggestAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1000) {
                    if (!TextUtils.isEmpty(searchAutoComplete.getText())) {
                        callAutoSuggestApi(searchAutoComplete.getText().toString());
                    }
                }
                return false;
            }
        });
        return true;
    }

    private void callAutoSuggestApi(String text) {
        AutoSuggestApi.make(this, text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        stringList.add(row.getString("tickerName") + " - " + row.getString("name"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //IMPORTANT: set data here and notify
                autoSuggestAdapter.setData(stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error getting data from Tiingo API");
                error.printStackTrace();

            }
        });
    }

    }
