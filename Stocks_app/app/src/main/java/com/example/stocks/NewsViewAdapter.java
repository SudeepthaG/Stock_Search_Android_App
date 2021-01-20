package com.example.stocks;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsViewAdapter extends RecyclerView.Adapter<NewsViewAdapter.CardViewHolder> {

    private final List<String[]>  newsCardModelList;
    private View view;

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        String[] newsCardModel;
        View view;
        ImageView imageView;
        TextView sourceTextView;
        TextView dateTextView;
        TextView titleTextView;
        String webUrl;

        public CardViewHolder(@NonNull final View itemView) {
            super(itemView);
            view = itemView;
            imageView = itemView.findViewById(R.id.news_image);
//            imageView.setOnClickListener(null);
            sourceTextView = itemView.findViewById(R.id.news_source);
            dateTextView = itemView.findViewById(R.id.news_time);
            titleTextView = itemView.findViewById(R.id.news_title);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Chrome.launchActivity(v, webUrl);
                }
            });

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final Dialog dialog = new Dialog(view.getContext());
                    dialog.setContentView(R.layout.news_dialogue);

                    final ImageView dialogImage = (ImageView) dialog.findViewById(R.id.dialog_image);
                    dialogImage.setImageDrawable(imageView.getDrawable());

                    final TextView dialogText = (TextView) dialog.findViewById(R.id.dialog_title);
                    dialogText.setText(titleTextView.getText());

                    final ImageView dialogTwitterImage = (ImageView) dialog.findViewById(R.id.dialog_twitter_image);
                    dialogTwitterImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Twitter.launchActivity(v, webUrl);
                        }
                    });

                    final ImageView dialogChromeImage = (ImageView) dialog.findViewById(R.id.dialog_chrome_image);
                    dialogChromeImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Chrome.launchActivity(v, webUrl);

                        }
                    });
                    dialog.show();
                    return true;
                }
            });


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Chrome.launchActivity(v, webUrl);
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final Dialog dialog = new Dialog(view.getContext());
                    dialog.setContentView(R.layout.news_dialogue);

                    final ImageView dialogImage = (ImageView) dialog.findViewById(R.id.dialog_image);
                    dialogImage.setImageDrawable(imageView.getDrawable());

                    final TextView dialogText = (TextView) dialog.findViewById(R.id.dialog_title);
                    dialogText.setText(titleTextView.getText());

                    final ImageView dialogTwitterImage = (ImageView) dialog.findViewById(R.id.dialog_twitter_image);
                    dialogTwitterImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Twitter.launchActivity(v, webUrl);
                        }
                    });

                    final ImageView dialogChromeImage = (ImageView) dialog.findViewById(R.id.dialog_chrome_image);
                    dialogChromeImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Chrome.launchActivity(v, webUrl);

                        }
                    });
                    dialog.show();
                    return true;
                }
            });

        }

    }

    public NewsViewAdapter(List<String[]> newsCardModelList) {
        this.newsCardModelList = newsCardModelList;
    }

    @NonNull
    @Override
    public NewsViewAdapter.CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.head_newscard, parent, false);
        }
        else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.newscard, parent, false);
        }
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewAdapter.CardViewHolder holder, int position) {
        String[] currentNewsCard = this.newsCardModelList.get(position);
        holder.newsCardModel = currentNewsCard;
        holder.sourceTextView.setText(currentNewsCard[0]);
        holder.dateTextView.setText(currentNewsCard[1]);
        holder.titleTextView.setText(currentNewsCard[2]);
        holder.webUrl = currentNewsCard[4];

        Picasso.with(holder.view.getContext()).load(currentNewsCard[3]).fit().into(holder.imageView);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return this.newsCardModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;
    }

}
