package com.project.rempaudioeditor.converters;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.project.rempaudioeditor.views.WaveForm;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class AudioConverters {
    public static ArrayList<Integer> makeFftFrom(byte[] fft) {
        ArrayList<Integer> newFft = new ArrayList<>();
        int distance = 3; // new distance between two consecutive bars in the original array
        int currentRepetition; // the loop has to be repeated
        boolean ifAdd = true; // to half the total no of bars
        boolean addAfter = true; // to add the bar after the previous bar or before it

        for (currentRepetition = 0; currentRepetition < distance; currentRepetition++) {
            for (int i = currentRepetition; i < fft.length; i += 4) {
                if (ifAdd) {
                    if (addAfter) {
                        newFft.add((int) fft[i]);
                        addAfter = false;
                    } else {
                        newFft.add(newFft.size()-1, (int) fft[i]);
                        addAfter = true;
                    }
                    ifAdd = false;
                }
                else {
                    ifAdd = true;
                }
            }
        }

        return newFft;
    }

    public static WaveForm createWaveForm(Context context, Uri uriFile) {
        MediaMetadataRetriever audio_data_retriever = new MediaMetadataRetriever();
        audio_data_retriever.setDataSource(context, uriFile);
        int audio_duration_in_milisec = Integer.parseInt(audio_data_retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        Cursor uriCursor = context.getContentResolver().query(uriFile, null, null, null, null);
        int file_size_index = uriCursor.getColumnIndex(OpenableColumns.SIZE);
        uriCursor.moveToFirst();
        byte[] bytes = new byte[uriCursor.getInt(file_size_index)];
        uriCursor.close();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uriFile);
            BufferedInputStream buf = new BufferedInputStream(inputStream);
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        WaveForm waveForm = new WaveForm(context);
        waveForm.setBars(bytes, audio_duration_in_milisec);

        return waveForm;
    }
}
