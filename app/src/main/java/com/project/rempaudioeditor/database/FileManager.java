package com.project.rempaudioeditor.database;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileManager {
    public static void writeStringFileToAppDirectory(@NonNull Context context,
                                                     @NonNull String file_name,
                                                     @NonNull String content) {
        try (FileOutputStream output_stream = context.openFileOutput(file_name, Context.MODE_PRIVATE)) {
            output_stream.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readStringFileFromAppDirectory(@NonNull Context context,
                                                        @NonNull String file_name) {
        FileInputStream input_stream = null;
        try {
            input_stream = context.openFileInput(file_name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(input_stream, StandardCharsets.UTF_8);
        StringBuilder contents_builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                contents_builder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents_builder.toString();
    }

    public static void removeStringFileFromAppDirectory(@NonNull Context context,
                                                        @NonNull String file_name) {
        context.deleteFile(file_name);
    }
}
