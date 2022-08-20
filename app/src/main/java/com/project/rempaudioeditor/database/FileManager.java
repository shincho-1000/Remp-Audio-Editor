package com.project.rempaudioeditor.database;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileManager {
    public static void writeStringFileToAppDirectory(Context context, String file_name, String content) {
        try (FileOutputStream fos = context.openFileOutput(file_name, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readStringFileFromAppDirectory(Context context, String file_name) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = context.openFileInput(file_name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void removeStringFileFromAppDirectory(Context context, String file_name) {
        context.deleteFile(file_name);
    }
}
