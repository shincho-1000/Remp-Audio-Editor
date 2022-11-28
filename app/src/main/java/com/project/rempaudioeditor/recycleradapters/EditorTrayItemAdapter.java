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
    private final Context context; // Brings resources to your code
    private final ArrayList<EditorTrayItemInfo> itemsList;
    private final EditorTrayItemClickListener onClickListener;

    public EditorTrayItemAdapter(Context context,
                                 ArrayList<EditorTrayItemInfo> itemsList,
                                 EditorTrayItemClickListener onClickListener) {
        this.context = context;
        this.itemsList = itemsList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public EditorTrayItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(context).inflate(R.layout.editor_tray_item, parent, false);
        return new EditorTrayItemAdapter.ViewHolder(inflatedView, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull EditorTrayItemAdapter.ViewHolder holder, int position) {
        EditorTrayItemInfo item = itemsList.get(position);

        String main_str = item.getText();
        Integer image_id = item.getImageId();
        holder.text.setText(main_str);

        if (image_id != null)
            holder.image.setImageResource(image_id);
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
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
