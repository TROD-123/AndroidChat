package com.thirdarm.chat.utils;

import android.content.Context;
import android.util.TimeUtils;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by TROD on 20180304.
 */

public class Utils {

    private static Toast mToast;

    /**
     * Shows a toast message
     *
     * @param context The context
     * @param message The message to show in the toast
     */
    public static void showToast(Context context, String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /**
     * Converts time, represented in millis, into a readable date/time form
     *
     * @param millis Time, represented in millis
     * @return String representing a short, readable form of time
     */
    public static String convertMillisToReadableDateTime(long millis) {
        // Convert time in millis to calendar object
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(millis);
        calendar.setTime(date);
        int currentYear = calendar.get(Calendar.YEAR);

        // Get another calendar instance, used to note the first day of the current year
        Calendar calendarYear = Calendar.getInstance();
        calendarYear.set(currentYear, 0, 1);

        // Get time difference from now, as well as time representations of ...
        long timeDiff = System.currentTimeMillis() - millis;
        long millisInDay = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        long millisInWeek = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
        long millisInYear = TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS);

        // Get time difference from start of year
        long timeDiffInCurrentYear = System.currentTimeMillis() - calendarYear.getTimeInMillis();

        // https://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
        SimpleDateFormat readableDateFormat;

        // Show short date depending on time difference
        if (timeDiff < millisInDay) {
            // Show time if today
            readableDateFormat = new SimpleDateFormat("kk:mm");
        } else if (timeDiff < millisInWeek) {
            // Show day of week if within this week but not today
            readableDateFormat = new SimpleDateFormat("EEE kk:mm");
        } else if (timeDiffInCurrentYear < millisInYear) {
            // Show full date, but not year, if longer than a week ago but within the same year
            readableDateFormat = new SimpleDateFormat("MMM d, kk:mm");
        } else {
            // Show full date if longer than a week ago
            readableDateFormat = new SimpleDateFormat("MMM d, ''yy, kk:mm");
        }
        return readableDateFormat.format(date);
    }
}
