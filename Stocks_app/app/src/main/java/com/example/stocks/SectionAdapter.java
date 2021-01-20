package com.example.stocks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {
    private ArrayList<Section> sectionList;
    private Context context;
    private ItemAdapter itemAdapter;
    public SectionAdapter(Context context, ArrayList<Section> sections) {
        sectionList = sections;
        this.context = context;
    }
    @NonNull
    @Override
    public SectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_section, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Section section = sectionList.get(position);
        holder.context = context;
        holder.bind(section);



        //both these lines could be added in the bind function inside the class for ViewHolder
//        holder.itemRecyclerView.setHasFixedSize(true);//just added
//        holder.itemRecyclerView.setNestedScrollingEnabled(false); //just added

    }
    @Override
    public int getItemCount() {
        return sectionList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView sectionName;
        private RecyclerView itemRecyclerView;
        private Context context;
        private TextView Worth;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionName = itemView.findViewById(R.id.section_item_text_view);
            itemRecyclerView = itemView.findViewById(R.id.item_recycler_view);
            Worth = itemView.findViewById(R.id.remWorth);
        }
        public void bind(Section section) {
            sectionName.setText(section.getSectionTitle());
            // RecyclerView for items
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            itemRecyclerView.setLayoutManager(linearLayoutManager);

            ArrayList<watchShare> items = section.getAllItemsInSection();

            if (section.getSectionTitle().equals("FAVORITES")){
                enableSwipeToDeleteAndUndo(itemRecyclerView);
                itemView.findViewById(R.id.netWorth).setVisibility(View.GONE);
                itemAdapter = new ItemAdapter(this.context, items, "favsOrder"); //could be the ItemRecyclerViewAdapter

            }
            else if (section.getSectionTitle().equals("PORTFOLIO")){
                itemView.findViewById(R.id.netWorth).setVisibility(View.VISIBLE);
                ArrayList<watchShare> tempItems = new ArrayList<>(items.subList(0,items.size()-1));
                itemAdapter = new ItemAdapter(this.context, tempItems, "sharesOrder"); //could be the ItemRecyclerViewAdapter
                String temp = items.get(items.size()-1).getLast().toString();
                Worth.setText(temp);

            }



            itemRecyclerView.setAdapter(itemAdapter);
            itemRecyclerView.setHasFixedSize(true);//just added
            itemRecyclerView.setNestedScrollingEnabled(false); //just added
//            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(itemRecyclerView.getContext(),
//                    linearLayoutManager.getOrientation());
//            itemRecyclerView.addItemDecoration(dividerItemDecoration);
            RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider));
            itemRecyclerView.addItemDecoration(dividerItemDecoration);


            // for drag and drop
            ItemTouchHelper.Callback callback =
                    new ItemMoveCallback(itemAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(itemRecyclerView);
        }
    }
    public void moveItem(int toSectionPosition, int fromSectionPosition) {
        ArrayList<watchShare> toItemsInSection = sectionList.get(toSectionPosition).getAllItemsInSection();
        ArrayList<watchShare> fromItemsInSection = sectionList.get(fromSectionPosition).getAllItemsInSection();
        if (fromItemsInSection.size() > 3) {
            toItemsInSection.add(fromItemsInSection.get(3));
            fromItemsInSection.remove(3);
            // update adapter for items in a section
            itemAdapter.notifyDataSetChanged();
            // update adapter for sections
            this.notifyDataSetChanged();
        }
    }

    private void enableSwipeToDeleteAndUndo(RecyclerView itemRecyclerView) {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this.context) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final watchShare item = itemAdapter.getData().get(position);

                itemAdapter.removeItem(position);

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(itemRecyclerView);
    }
}
