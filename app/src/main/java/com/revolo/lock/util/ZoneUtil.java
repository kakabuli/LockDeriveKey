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
    public static long getZoneTime(long zoneTime,String zone){
        String z1 = zone.substring(1, 3);
        String z2 = zone.substring(4, 6);
        int zoneValer = 0;
        try {
            zoneValer = Integer.parseInt(z1) * 4 + (Integer.parseInt(z2) / 15);
            if (zone.indexOf("-") > -1) {
                zoneValer = zoneValer * -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            zoneValer = 0;
        }

        long time = zoneTime - (zoneValer * 15 * 60 * 1000);

        Timber.e("dagd:" + time);
        Timber.e("dagd2:" + zoneTime);

        return time;
    }

    public static long getTime(String timeZone, String timeStr, String pattern) {
        timeZone = "+00:00";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        ParsePosition pos = new ParsePosition(0);
        Date result = formatter.parse(timeStr, pos);
        return result.getTime();
    }

    public static long getTime(String timeZone, String timeStr) {
        timeZone = "+00:00";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        ParsePosition pos = new ParsePosition(0);
        Date result = formatter.parse(timeStr, pos);
        return result.getTime();
    }

    public static String getDate(String timeZone, long time) {
        timeZone = "+00:00";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        Date date = new Date();
        date.setTime(time);
        return formatter.format(date);
    }

    public static long getTestTime(String zone) {
        return (getTime("", getDate("", System.currentTimeMillis())) +getTestTime2(zone));
    }

    public static long getTestTime2(String zone) {
        String z1 = zone.substring(1, 3);
        String z2 = zone.substring(4, 6);
        int zoneValer = 0;
        try {
            zoneValer = Integer.parseInt(z1) * 4 + (Integer.parseInt(z2) / 15);
            if (zone.indexOf("-") > -1) {
                zoneValer = zoneValer * -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            zoneValer = 0;
        }

        return zoneValer * 15 * 60 * 1000;
    }

    public static String getDate(String timeZone, long time, String pattern) {
        timeZone = "+00:00";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        Date date = new Date();
        date.setTime(time);
        return formatter.format(date);
    }
    public static String getTestDate(String timeZone, long time, String pattern) {
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

