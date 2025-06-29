package com.example.javastripeapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String formatCurrentDateTime(Long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }
}
