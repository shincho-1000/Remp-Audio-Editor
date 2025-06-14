package com.project.rempaudioeditor.utils;

import android.content.Context;

import androidx.annotation.NonNull;

public class UnitConverter {
    public static double convertDpToPx(@NonNull Context context, double dp){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale);
    }

    public static String format(long milisec_to_convert){
        double sec_to_convert = milisec_to_convert/1000f;
        long hours = (long) ((sec_to_convert % 86400) / 3600);
        long minutes = (long) ((sec_to_convert % 3600) / 60);
        long seconds = Math.round(sec_to_convert % 60);

        String formattedDuration = "";

        if (hours < 10)
            formattedDuration += "0";

        formattedDuration += hours + ":";

        if (minutes < 10)
            formattedDuration += "0";

        formattedDuration += minutes + ":";

        if (seconds < 10)
            formattedDuration += "0";

        formattedDuration += seconds;


        return formattedDuration;
    }

    public static long toMilisec(@NonNull String formatted_time){
        String[] time_splits = formatted_time.trim().split(":");

        long hr = 0;
        long min = 0;
        long sec = 0;

        if (time_splits.length == 3) {
            hr = Long.parseLong(time_splits[0].trim());
            min = Long.parseLong(time_splits[1].trim());
            sec = Long.parseLong(time_splits[2].trim());
        } else if (time_splits.length == 2) {
            min = Long.parseLong(time_splits[0].trim());
            sec = Long.parseLong(time_splits[1].trim());
        } else if (time_splits.length == 1) {
            if (!time_splits[0].trim().isEmpty()) {
                sec = Long.parseLong(time_splits[0].trim());
            }
        }

        return hr*60*60*1000 + min*60*1000 + sec*1000;
    }
}