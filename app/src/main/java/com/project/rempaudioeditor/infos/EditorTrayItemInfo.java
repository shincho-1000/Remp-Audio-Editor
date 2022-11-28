package com.project.rempaudioeditor.infos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.rempaudioeditor.enums.EditorTrayId;

public class EditorTrayItemInfo {
    private final String text_str;
    private final Integer image_id;
    private final EditorTrayId item_id;

    public EditorTrayItemInfo(@NonNull String text_str,
                              @Nullable Integer image_id,
                              @NonNull EditorTrayId item_id) {
        this.text_str = text_str;
        this.image_id = image_id;
        this.item_id = item_id;
    }

    public String getText() {
        return text_str;
    }

    public Integer getImageId() {
        return image_id;
    }

    public EditorTrayId getId() {
        return item_id;
    }
}
