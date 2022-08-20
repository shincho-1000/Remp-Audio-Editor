package com.project.rempaudioeditor.arrayadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.infos.SettingsItemView;

import java.util.ArrayList;

public class SettingsItemsAdapter extends RecyclerView.Adapter<SettingsItemsAdapter.ViewHolder> {

    private final Context context; // Brings resources to your code
    private final ArrayList<SettingsItemView> itemsList;
    private final SettingsItemClickListener onClickListener;

    public SettingsItemsAdapter(Context context, ArrayList<SettingsItemView> itemsList, SettingsItemClickListener onClickListener){
        this.context = context;
        this.itemsList = itemsList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // Takes the view to be shown multiple times
        View inflatedView = LayoutInflater.from(context).inflate(R.layout.settings_list_item, parent, false);
        return new ViewHolder(inflatedView, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { // Handles the view taken in onCreateViewHolder
        SettingsItemView item = itemsList.get(position);

        String main_str = item.getMainText();
        String desc_str = item.getDescText();
        holder.textView_main.setText(main_str);
        holder.textView_desc.setText(desc_str);
    }

    @Override
    public int getItemCount() { // Total no. of items
        return itemsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView_main;
        TextView textView_desc;
        SettingsItemClickListener onClickListener;

        public ViewHolder(@NonNull View itemView, SettingsItemClickListener onClickListener) {
            super(itemView);

            textView_main = itemView.findViewById(R.id.settings_list_text_main);
            textView_desc = itemView.findViewById(R.id.settings_list_text_desc);
            this.onClickListener = onClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onSettingsItemClick(getAdapterPosition());
        }
    }

    public interface SettingsItemClickListener {
        void onSettingsItemClick(int position);
    }
}
