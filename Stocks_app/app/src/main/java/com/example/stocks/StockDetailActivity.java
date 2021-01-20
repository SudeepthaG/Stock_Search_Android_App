package com.example.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skyhope.showmoretextview.ShowMoreTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StockDetailActivity extends AppCompatActivity{
    private String queryString;
    private RequestQueue requestQueue;
    private float totalAmount = (float) 20000.0;
    private MenuItem favClick;
    private boolean favFlag = false;
    private boolean showFavs = false;
    View progressLayout;
    ScrollView scrollView;
    TextView stkTkr;
    TextView stkName;
    TextView stkValue;
    String[] prices;
    String compname;
    private float lastPrice;
    Context context = this;
    TextView moredata;
    TextView lessdata;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        progressLayout = findViewById(R.id.progressScreen);
        scrollView = findViewById(R.id.scrollView2);
        progressLayout.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        Intent intent = getIntent();
        this.queryString = (intent.getStringExtra("com.example.stocks.QUERY_STRING")).trim().split(" ")[0];
        //this.compname=(intent.getStringExtra("com.example.stocks.QUERY_STRING")).trim().split(" ")[2];
        init();

        Button trade  = (Button) findViewById(R.id.tradebutton);
        trade.setOnClickListener(v -> {
            Log.i("","entered trade");
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.tradedialog);

                EditText inputtext=(EditText)dialog.findViewById(R.id.inputtext);
                TextView tradeheading= (TextView) dialog.findViewById(R.id.tradeheading);
                String headtext="Trade "+compname+" shares";
                tradeheading.setText(headtext);
                TextView sharescalc= (TextView) dialog.findViewById(R.id.sharescalc);
                String sharescalcstr =  "0 x $" + lastPrice +"/share = $0.00";
                sharescalc.setText(sharescalcstr);
                TextView moneyleft = (TextView) dialog.findViewById(R.id.moneyleft);
                float remainingAmt = getAmountFile();
                String moneylefttext="$"+remainingAmt+" available to buy "+queryString;
                moneyleft.setText(moneylefttext);
                //        inputtext.addTextChangedListener(new EditTextWatcher());
                inputtext.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {}

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                        if(s.length() > 0){
                            if(!s.equals('-')){
                                int value = Integer.parseInt(s.toString());
                                TextView sharescalc= (TextView) dialog.findViewById(R.id.sharescalc);
                                String sharescalcstr = s + " x $" + lastPrice +"/share = $"+(lastPrice*value);
                                sharescalc.setText(sharescalcstr);                            }

                        }else{
                            String sharescalcstr =  "0 x $" + lastPrice +"/share = $0.00";
                            sharescalc.setText(sharescalcstr);
                        }

                    }
                });
            Button buyButton = (Button) dialog.findViewById(R.id.buybtn);
            // if button is clicked, close the custom dialog
            buyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputtext.getText().toString().length() == 0){
                        Toast.makeText(StockDetailActivity.this,"Please enter valid amount",Toast.LENGTH_LONG).show();
                    }
                    else if(inputtext.getText().toString().equals("0")){
                        Toast.makeText(StockDetailActivity.this,"Cannot buy less than 0 shares",Toast.LENGTH_LONG).show();
                    }
                    else{
                        String val=inputtext.getText().toString();
                        int value=Integer.parseInt(val);
                        float moneyneeded = lastPrice * value;
                        float moneyleft=getAmountFile();
                        if(moneyneeded>moneyleft){
                            Toast.makeText(StockDetailActivity.this,"Not enough money to buy",Toast.LENGTH_LONG).show();
                        }

                        else{
                            float newmoneyleft = moneyleft - moneyneeded;
                            initializeAmountFile(newmoneyleft);
                            int existingval = getSharedPref(queryString);
                            updateSharedPreference(queryString, existingval + value);
                            dialog.dismiss();
                            final Dialog congBuy = new Dialog(context);
                            congBuy.setContentView(R.layout.congratulationsdialog);
                            TextView congratsText = (TextView) congBuy.findViewById(R.id.congratstext);
                            String sh = "";
                            if(value > 1){
                                sh = " shares";
                            }
                            else{
                                sh = " share";
                            }
                            String text = "You have successfully bought " + val + sh + " of " + queryString;
                            congratsText.setText(text);
                            Button doneBtn = (Button) congBuy.findViewById(R.id.donebutton);
                            doneBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    congBuy.dismiss();
//                                    TextView sharesxmlb=findViewById(R.id.sharesxml);
//                                    int existingVal = getSharedPref(queryString);
//                                    Log.i("existing new val", String.valueOf(existingVal));
//                                    String txt= "Shares Owned: "+(existingVal) +"";
//                                    sharesxmlb.setText(txt);
                                    invokeSharedPreference();

                                }
                            });
                            congBuy.show();
                        }

                    }
                }
            });
            //Sell Button
            Button sellButton = (Button) dialog.findViewById(R.id.sellbtn);
            // if button is clicked, close the custom dialog
            sellButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputtext.getText().toString().length() == 0){
                        Toast.makeText(StockDetailActivity.this,"Please enter valid amount",Toast.LENGTH_LONG).show();
                    }
                    else if(inputtext.getText().toString().equals("0")){
                        Toast.makeText(StockDetailActivity.this,"Cannot sell less than 0 shares",Toast.LENGTH_LONG).show();
                    }
                    else{
                        String val=inputtext.getText().toString();
                        int value=Integer.parseInt(val);
                        float moneyneeded = lastPrice * value;
                        float moneyleft=getAmountFile();
                        if(value > getSharedPref(queryString)){
                            Toast.makeText(StockDetailActivity.this,"Not enough shares to sell",Toast.LENGTH_LONG).show();
                        }

                        else{
                            float newmoneyleft = moneyleft + moneyneeded;
                            initializeAmountFile(newmoneyleft);
                            int existingval = getSharedPref(queryString);
                            int rem = existingval - value;
                            if(rem == 0){
                                removeSharedPreferences(queryString);
                            }
                            else{
                                updateSharedPreference(queryString, existingval - value);
                            }

                            dialog.dismiss();
                            final Dialog congSell = new Dialog(context);
                            congSell.setContentView(R.layout.congratulationsdialog);
                            TextView congratsText = (TextView) congSell.findViewById(R.id.congratstext);
                            String sh = "";
                            if(value > 1){
                                sh = " shares";
                            }
                            else{
                                sh = " share";
                            }
                            String text = "You have successfully sold " + val + sh + " of " + queryString;
                            congratsText.setText(text);
                            Button doneBtn = (Button) congSell.findViewById(R.id.donebutton);
                            doneBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    congSell.dismiss();
//                                    TextView sharesxmlc=findViewById(R.id.sharesxml);
//                                    int existingVal = getSharedPref(queryString);
//                                    Log.i("existing new val", String.valueOf(existingVal));
//                                    String txt= "Shares Owned: "+ existingVal;
//                                    sharesxmlc.setText(txt);
                                    invokeSharedPreference();
                                }
                            });
                            congSell.show();
                        }

                    }
                }
            });

            dialog.show();
        });


        Button showmorebtn=(Button) findViewById(R.id.showmorebtn);
        Button showlessbtn=(Button) findViewById(R.id.showlessbtn);
        showmorebtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                lessdata.setVisibility(View.VISIBLE);
                showlessbtn.setVisibility(View.VISIBLE);
                moredata.setVisibility(View.GONE);
                showmorebtn.setVisibility(View.GONE);

            }
        });

        showlessbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                lessdata.setVisibility(View.GONE);
                showlessbtn.setVisibility(View.GONE);
                moredata.setVisibility(View.VISIBLE);
                showmorebtn.setVisibility(View.VISIBLE);

            }
        });

    }

    private void init(){
        this.requestQueue = Volley.newRequestQueue(this);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back_grey_24dp);
        myToolbar.setNavigationOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));

        getStockDetails();
        getStockPrices();
        getCharts();
        getNews();
//        progressLayout.setVisibility(View.GONE);
//        scrollView.setVisibility(View.VISIBLE);
    }

    private void invokeSharedPreference(){
        TextView sharesOwned = (TextView) findViewById(R.id.sharesxml);
        TextView marketVal = (TextView)findViewById(R.id.marketvalxml);
        //Initialize amount
//        initializeAmountFile(20000.00);

        SharedPreferences sharesPref = getSharedPreferences("myShares", MODE_PRIVATE);
        SharedPreferences.Editor shareEditor = sharesPref.edit();
//        shareEditor.putInt("dummyZero", 0 );
//        shareEditor.apply();
        Map<String, Integer> shareEntries= (Map<String, Integer>) sharesPref.getAll();
        if(shareEntries.containsKey(queryString)){
            String shares_owned = "Shares owned: " + shareEntries.get(queryString);
            sharesOwned.setText(shares_owned);
            String lastVal = "Market Value: " + (lastPrice * shareEntries.get(queryString));
            marketVal.setText(lastVal);
        }
        else{
            String shares_owned = "You have 0 shares of " + queryString +".";
            sharesOwned.setText(shares_owned);
            String askTrade = "Start trading!";
            marketVal.setText(askTrade);
        }

    }


    private void updateSharedPreference(String ticker, int val){
        SharedPreferences sharesPref = getSharedPreferences("myShares", MODE_PRIVATE);
        SharedPreferences.Editor shareEditor = sharesPref.edit();
        Log.i("updateSharedPreference", String.valueOf(val));
        shareEditor.putInt(ticker,val);
        shareEditor.apply();

        SharedPreferences myOrder = getSharedPreferences("Order", MODE_PRIVATE);
        ArrayList<String> sharesOrder = new ArrayList<>();
        Gson gson = new Gson();
        String sharesJson = myOrder.getString("sharesOrder", "");
        if (sharesJson.isEmpty()) {
            Toast.makeText(StockDetailActivity.this,"There is something error",Toast.LENGTH_LONG).show();
        } else {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            List<String> arrPackageData = gson.fromJson(sharesJson, type);
            sharesOrder.addAll(arrPackageData);
        }
        if (!sharesOrder.contains(ticker)){
            sharesOrder.add(ticker);
        }

        String json = gson.toJson(sharesOrder);
        SharedPreferences.Editor editor = myOrder.edit();
        editor.putString("sharesOrder",json );
        editor.apply();

    }

    private void removeSharedPreferences(String ticker){
        SharedPreferences sharesPref = getSharedPreferences("myShares", MODE_PRIVATE);
        sharesPref.edit().remove(ticker).apply();

        SharedPreferences myOrder = getSharedPreferences("Order", MODE_PRIVATE);
        ArrayList<String> sharesOrder = new ArrayList<>();
        Gson gson = new Gson();
        String sharesJson = myOrder.getString("sharesOrder", "");
        if (sharesJson.isEmpty()) {
            Toast.makeText(StockDetailActivity.this,"There is something error",Toast.LENGTH_LONG).show();
        } else {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            List<String> arrPackageData = gson.fromJson(sharesJson, type);
            sharesOrder.addAll(arrPackageData);
        }
        sharesOrder.remove(ticker);

        String json = gson.toJson(sharesOrder);
        SharedPreferences.Editor editor = myOrder.edit();
        editor.putString("sharesOrder",json );
        editor.apply();
    }

    private int getSharedPref(String ticker){
        SharedPreferences sharesPref = getSharedPreferences("myShares", MODE_PRIVATE);
        SharedPreferences.Editor shareEditor = sharesPref.edit();
//        shareEditor.putInt("dummyZero", 0 );
//        shareEditor.apply();
        Map<String, Integer> shareEntries= (Map<String, Integer>) sharesPref.getAll();
        if(shareEntries.containsKey(ticker)){
            Log.i("getSharedPref",String.valueOf(shareEntries.get(ticker)));
            return shareEntries.get(ticker);
        }
        else{
            Log.i("getSharedPref", "0");
            return 0;
        }
    }


    private void getStockDetails(){
        String url = "http://responsive-env.eba-9bzm8k2k.us-east-1.elasticbeanstalk.com/details/" + this.queryString;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        stkTkr = (TextView) findViewById(R.id.StkName);
                        stkName = (TextView) findViewById(R.id.textView4);
                        ShowMoreTextView textView = findViewById(R.id.text_view_show_more);
                        moredata=findViewById(R.id.moredata);
                        lessdata=findViewById(R.id.lessdata);
                        try{
                            stkTkr.setText(response.getString("ticker"));
                            stkName.setText(response.getString("name"));
                            compname = response.getString("name");
                            textView.setText(response.getString("description"));
                            moredata.setText(response.getString("description"));
                            lessdata.setText(response.getString("description"));
                            textView.setShowingLine(2);
                            textView.addShowMoreText("Show more...");
                            textView.addShowLessText("Show less");
                            textView.setShowMoreColor(Color.parseColor("#C1C1C1"));
                            textView.setShowLessTextColor(Color.parseColor("#C1C1C1"));
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("Volley", "Error "+error.toString());

                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void getStockPrices(){
        String url = "http://responsive-env.eba-9bzm8k2k.us-east-1.elasticbeanstalk.com/details/description/" + this.queryString;
        Context context = StockDetailActivity.this;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        stkValue = (TextView) findViewById(R.id.stkValue);
                        TextView stkChangePrice = (TextView) findViewById(R.id.textView5);
                        GridView gridView = (GridView) findViewById(R.id.gridView1);
                        try{

                            BigDecimal Output= new BigDecimal(response.getDouble("lastPrice")
                                    - response.getDouble("prevClose")).setScale(2, RoundingMode.HALF_EVEN);
                            stkValue.setText("$" + String.valueOf(response.getDouble("lastPrice")));
                            lastPrice = (float)response.getDouble("lastPrice");
                            float change = Output.floatValue();
                            String changeVal = "";
                            if(change >= 0){
                                changeVal = "$" + String.valueOf(change);
                            }
                            else{
                                changeVal = "-$" + String.valueOf(-1*change);
                            }
                            stkChangePrice.setText(changeVal);
                            if(Output.compareTo(new BigDecimal(0)) >= 0){
                                stkChangePrice.setTextColor(Color.rgb(37,168,1));
                            }
                            else{
                                stkChangePrice.setTextColor(Color.RED);
                            }

                            String[] strList = new String[7];
                            strList[0] = "Current Price: "+ ((response.getString("lastPrice")) != "null"?
                                    String.valueOf(response.getDouble("lastPrice")) : "0.0");
                            strList[1] = "Low: " + ((response.getString("lowPrice")) != "null"?
                                    String.valueOf(response.getDouble("lowPrice")) : "0.0");
                            strList[2] = "Bid Price: " + ((response.getString("bidPrice")) != "null"?
                                    String.valueOf(response.getDouble("bidPrice")) : "0.0");
                            strList[3] = "Open Price: " + ((response.getString("openPrice")) != "null"?
                                    String.valueOf(response.getDouble("openPrice")) : "0.0");
                            strList[4] = "Mid: " + ((response.getString("midPrice")) != "null"?
                                    String.valueOf(response.getDouble("midPrice")) : "0.0");
                            strList[5] = "High: " + ((response.getString("highPrice")) != "null"?
                                    String.valueOf(response.getDouble("highPrice")) : "0.0");
                            if((response.getString("volume"))!= "null"){
                                int vol = response.getInt("volume");
                                DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
                                String numberAsString = decimalFormat.format(vol);
                                strList[6] = "Volume: "+ numberAsString;
                            }
                            else{
                                strList[6] = "Volume: 0.0";
                            }
                            gridView.setAdapter(new TextViewAdapter(context, strList));


                            favClick.setVisible(true);
                            invokeSharedPreference();
                            progressLayout.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });
        requestQueue.add(jsonObjectRequest);
    }


    private void getCharts(){
        WebView webview = (WebView) findViewById(R.id.webView);
        webview.getSettings().setBuiltInZoomControls(false);
        webview.getSettings().setSupportZoom(false);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString() != null && request.getUrl().toString().endsWith(".html")) {
                    view.loadUrl(request.getUrl().toString());
                    return true;
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW,request.getUrl()));
                    return true;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }

        });
        webview.loadUrl("file:///android_asset/HighCharts.html?ticker=" + queryString);
    }

    private void getNews(){
        String url = "http://responsive-env.eba-9bzm8k2k.us-east-1.elasticbeanstalk.com/details/news/" + this.queryString;
        Context context = StockDetailActivity.this;

        JsonArrayRequest jsonArrayReq = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>(){

            @Override
            public void onResponse(JSONArray response) {
                View view = new View(context);
                RecyclerView recyclerView = (RecyclerView)findViewById(R.id.news_recycler_view);
                try{
                    int n = response.length();
                    List<String[]> articles = new ArrayList<String[]>();
                    for(int i = 0; i < n; i++){
                        String[] article = new String[5];
                        JSONObject current = response.getJSONObject(i);
                        JSONObject src = current.getJSONObject("source");
                        article[0] = src.getString("name");
                        article[1] = numberOfDays(current.getString("publishedAt"));
                        article[2] = current.getString("title");
                        article[3] = current.getString("urlToImage");
                        article[4] = current.getString("url");
                        articles.add(article);
                    }

                    LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(layoutManager);
                    NewsViewAdapter adapter = new NewsViewAdapter(articles);
                    recyclerView.setAdapter(adapter);

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                Log.e("Volley", "Error "+error.toString());

            }
        });

//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        View view = new View(context);
//                        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.news_recycler_view);
//                        try{
//                            JSONArray results = response.getJSONArray("articles");
//                            int n = results.length();
//                            List<String[]> articles = new ArrayList<String[]>();
//                            for(int i = 0; i < n; i++){
//                                String[] article = new String[5];
//                                JSONObject current = results.getJSONObject(i);
//                                JSONObject src = current.getJSONObject("source");
//                                article[0] = src.getString("name");
//                                article[1] = numberOfDays(current.getString("publishedAt")) + " days ago";
//                                article[2] = current.getString("title");
//                                article[3] = current.getString("urlToImage");
//                                article[4] = current.getString("url");
//                                articles.add(article);
//                            }
//
//                            LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
//                            recyclerView.setHasFixedSize(true);
//                            recyclerView.setLayoutManager(layoutManager);
//                            NewsViewAdapter adapter = new NewsViewAdapter(articles);
//                            recyclerView.setAdapter(adapter);
////                            progressLayout.setVisibility(View.GONE);
////                            scrollView.setVisibility(View.VISIBLE);
////                            favClick.setVisible(true);
//                            invokeSharedPreference();
//                        }catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO: Handle error
//
//                    }
//                });
        requestQueue.add(jsonArrayReq);
    }

    private String numberOfDays(String strDate) throws Exception{
        Instant instantBefore = Instant.parse(strDate);
        ZoneId z = ZoneId.of("America/Los_Angeles");
        ZonedDateTime dateTimeBefore = instantBefore.atZone(z);
        Instant instantAfter = Instant.now(); // Replace with any date after
        ZonedDateTime dateTimeAfter = instantAfter.atZone(z);
        int days = (int) ChronoUnit.DAYS.between(dateTimeBefore, dateTimeAfter);
        if(days > 0){
            if(days == 1){
                return String.valueOf(days) + " day ago";
            }
            return String.valueOf(days) + " days ago";
        } else {
            int hrs = (int) ChronoUnit.HOURS.between(dateTimeBefore, dateTimeAfter);
            return String.valueOf(hrs) + " hours ago";
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fav_menu, menu);
        favClick = menu.findItem(R.id.favBtn);
        getFavStatus();
        return true;
    }

    private void getFavStatus() {
        SharedPreferences myPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Map<String, ?> allEntries = myPrefs.getAll();
        outerLoop:
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (queryString.equalsIgnoreCase(entry.getKey())){
                favClick.setIcon(R.drawable.ic_baseline_star_24);
                favFlag = true;
                break outerLoop;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.favBtn){
            SharedPreferences myPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

            SharedPreferences myOrder = getSharedPreferences("Order", MODE_PRIVATE);
            SharedPreferences.Editor orderEditor = myOrder.edit();
            ArrayList<String> favsOrder = new ArrayList<>();

            Gson gson = new Gson();
            String favsJson = myOrder.getString("favsOrder", "");
            if (favsJson.isEmpty()) {
                Toast.makeText(StockDetailActivity.this,"There is something error",Toast.LENGTH_LONG).show();
            } else {
                Type type = new TypeToken<List<String>>() {
                }.getType();
                List<String> arrPackageData = gson.fromJson(favsJson, type);
                favsOrder.addAll(arrPackageData);
            }
//            Map<String, ?> allEntries = myPrefs.getAll();
//            boolean flag = false;
//            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//                if (entry.getKey().toString().contains(queryTicker)) {
//                    flag = true;
//                }
//            }
            if (favFlag) {
                myPrefs.edit().remove(queryString).apply();
                favsOrder.remove(queryString);
                String json = gson.toJson(favsOrder);
                orderEditor.putString("favsOrder",json );
                orderEditor.apply();
                Toast.makeText(StockDetailActivity.this,
                        '"'+ queryString + '"' + " was removed from favorites",
                        Toast.LENGTH_LONG).show();
                favFlag = false;
            }else{

                SharedPreferences.Editor editor = myPrefs.edit();
//                editor.putFloat(queryString, (float) 5.0);
                editor.putString(queryString, compname);
                editor.apply();

                favsOrder.add(queryString);
                String json = gson.toJson(favsOrder);
                orderEditor.putString("favsOrder",json );
                orderEditor.apply();
                Toast.makeText(StockDetailActivity.this,
                        '"'+ queryString + '"' + " was added to favorites",
                        Toast.LENGTH_LONG).show();
                favFlag = true;
            }
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);
//        favClick = menu.findItem(R.id.favBtn);
        if (showFavs){
            favClick.setVisible(true);

        }else{
            showFavs = true;
        }
        if (favFlag){
            favClick.setIcon(R.drawable.ic_baseline_star_24);
//            favClick.setVisible(true);
        }else{
            favClick.setIcon(R.drawable.ic_baseline_star_border_24);
//            favClick.setVisible(true);
        }


        return true;
    }

    private void initializeAmountFile(double newamt){
        SharedPreferences amountPref = getSharedPreferences("FinalAmt", MODE_PRIVATE);
        SharedPreferences.Editor amountEditor = amountPref.edit();
        amountEditor.putFloat("remainingAmt",(float)newamt);
        amountEditor.apply();
    }

    private float getAmountFile(){
        SharedPreferences amountPref = getSharedPreferences("FinalAmt", MODE_PRIVATE);
        SharedPreferences.Editor amountEditor = amountPref.edit();
        float amt = amountPref.getFloat("remainingAmt" , (float) 20000.0);
        return amt;
    }
}