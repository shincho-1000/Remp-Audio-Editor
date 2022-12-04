package com.project.rempaudioeditor.recycleradapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rempaudioeditor.R;
import com.project.rempaudioeditor.infos.EditorTrayItemInfo;

import java.util.ArrayList;

public class EditorTrayItemAdapter extends RecyclerView.Adapter<EditorTrayItemAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<EditorTrayItemInfo> items_list;
    private final EditorTrayItemClickListener on_click_listener;

    public EditorTrayItemAdapter(Context context,
                                 ArrayList<EditorTrayItemInfo> items_list,
                                 EditorTrayItemClickListener on_click_listener) {
        this.context = context;
        this.items_list = items_list;
        this.on_click_listener = on_click_listener;
    }

    @NonNull
    @Override
    public EditorTrayItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(context).inflate(R.layout.editor_tray_item, parent, false);
        return new EditorTrayItemAdapter.ViewHolder(inflatedView, on_click_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull EditorTrayItemAdapter.ViewHolder holder, int position) {
        EditorTrayItemInfo item = items_list.get(position);

        String main_text = item.getText();
        Integer image_id = item.getImageId();
        holder.text.setText(main_text);

        if (image_id != null)
            holder.image.setImageResource(image_id);
    }

    @Override
    public int getItemCount() {
        return items_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text;
        ImageView image;

        EditorTrayItemAdapter.EditorTrayItemClickListener onClickListener;

        public ViewHolder(@NonNull View itemView,
                          EditorTrayItemAdapter.EditorTrayItemClickListener onClickListener) {
            super(itemView);

            text = itemView.findViewById(R.id.editor_tray_text);
            image = itemView.findViewById(R.id.editor_tray_image);
            this.onClickListener = onClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onEditorTrayItemClick(getAdapterPosition());
        }
    }

    public interface EditorTrayItemClickListener {
        void onEditorTrayItemClick(int position);
    }
}
