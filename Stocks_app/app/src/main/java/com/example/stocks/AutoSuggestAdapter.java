package com.example.stocks;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoSuggestAdapter extends ArrayAdapter<String> implements Filterable {
    private List<String> suggestions;

    public AutoSuggestAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        suggestions = new ArrayList<>();
    }

    public void setData(List<String> list) {
        suggestions.clear();
        suggestions.addAll(list);
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return suggestions.get(position);
    }

    public String getObject(int position) {
        return suggestions.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter dataFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && (results.count > 0)) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return dataFilter;
    }
}
