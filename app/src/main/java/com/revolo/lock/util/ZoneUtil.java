package com.revolo.lock.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import timber.log.Timber;

public class ZoneUtil {
    public static String getZone() {
        String stZone = "+00:00";
        String time = TimeZone.getDefault().getDisplayName(true, TimeZone.SHORT);
        if (time.length() > 8) {
            stZone = time.substring(0, 9).replace("GMT", "");
            Timber.e("dasga:" + stZone);
        }
        return stZone;
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }

    public static long getTime(String timeZone, String timeStr, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        ParsePosition pos = new ParsePosition(0);
        Date result = formatter.parse(timeStr, pos);
        return result.getTime();
    }

    public static long getTime(String timeZone, String timeStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        ParsePosition pos = new ParsePosition(0);
        Date result = formatter.parse(timeStr, pos);
        return result.getTime();
    }

    public static String getDate(String timeZone, long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        Date date = new Date();
        date.setTime(time);
        return formatter.format(date);
    }

    public static String getDate(String timeZone, long time, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        Date date = new Date();
        date.setTime(time);
        return formatter.format(date);
    }

    public static byte getZoneByte(String zone) {
        String z1 = zone.substring(1, 3);
        String z2 = zone.substring(4, 6);
        try {
            int zoneValer = Integer.parseInt(z1) * 4 + (Integer.parseInt(z2)/15);
            if (zone.indexOf("-") > -1) {
                zoneValer = zoneValer * -1;
            }
            return (byte) zoneValer;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}

