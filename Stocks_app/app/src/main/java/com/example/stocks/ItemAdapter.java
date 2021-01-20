package com.example.stocks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    ArrayList<watchShare> items;
    private Context context;
    private String orderType;


    public ItemAdapter(Context context, ArrayList<watchShare> items, String orderType) {
        this.items = items;
        this.context = context;
        this.orderType = orderType;
    }


    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);// item_custom_row_layout
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        watchShare currentItem = items.get(position);
//        String itemName = (String) items.get(position);
        holder.bind(currentItem);
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    public ArrayList<watchShare> getData() {
        return items;
    }

    public void removeItem(int position) {
        watchShare currentItem = items.get(position);


        String ticker = currentItem.getTicker();

        SharedPreferences myPrefs = this.context.getSharedPreferences("MyPrefs", this.context.MODE_PRIVATE);
        myPrefs.edit().remove(ticker).apply();
        items.remove(position);
        updateOrder(items);
        notifyItemRemoved(position);


    }

    public void updateOrder(ArrayList<watchShare> items){
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("Order", this.context.MODE_PRIVATE);

        ArrayList<String> arrPackage = new ArrayList<>();
        for(int i=0; i<items.size();i++){
            watchShare currentItem = items.get(i);
            String ticker = currentItem.getTicker();
            arrPackage.add(ticker);
        }
        Gson gson = new Gson();
        String json = gson.toJson(arrPackage);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(orderType,json );
        editor.apply();
    }


    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        updateOrder(items);

    }

    @Override
    public void onRowSelected(ViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(ViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.parseColor("#F2F2F4"));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //        private TextView itemName;
        View rowView;
        TextView ticker;
        TextView subTitle;
        TextView last;
        TextView change;
        ImageView img;
        Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowView = itemView;
            ticker = itemView.findViewById(R.id.list_item_text_view);
            subTitle = itemView.findViewById(R.id.textView2);
            last = itemView.findViewById(R.id.textView3);
            change = itemView.findViewById(R.id.textView6);
            img= itemView.findViewById(R.id.imageView);
            button = itemView.findViewById(R.id.button);
        }

        public void bind(watchShare item) {
            ticker.setText(item.getTicker());
            subTitle.setText(item.getSubTitle());
            last.setText(item.getLast().toString()); //also set the decimal points
            change.setText(item.getChange().toString());//also set the decimal points
//            change.setTextColor(Color.parseColor(item.getChangeColor()));
//            Log.d("changeColor", item.getChangeColor().getClass().getSimpleName());
//
//            if (item.getChangeColor() == "#2a7f00"){
//                img.setImageResource(R.drawable.ic_twotone_trending_up_24);
//            }
//            else if(item.getChangeColor() == "#000"){
//                img.setVisibility(View.INVISIBLE);
//            }

            if (item.getChange() > 0) {
                change.setText(item.getChange().toString());
                change.setTextColor(Color.parseColor("#319C5E"));
                img.setImageResource(R.drawable.ic_twotone_trending_up_24);
            }
            else if(item.getChange() < 0){
                String content = String.valueOf(-1 *item.getChange());
                change.setText(content);
                change.setTextColor((Color.parseColor("#9B4049")));
            }else{
                change.setText(item.getChange().toString());
                img.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, StockDetailActivity.class);
                    intent.putExtra("com.example.stocks.QUERY_STRING", item.getTicker());
                    context.startActivity(intent);
                }
            });

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, StockDetailActivity.class);
                    intent.putExtra("com.example.stocks.QUERY_STRING", item.getTicker());
                    context.startActivity(intent);
                }
            });
            //set the image and the color for the change on the basis of the changeColor
        }
    }
}
