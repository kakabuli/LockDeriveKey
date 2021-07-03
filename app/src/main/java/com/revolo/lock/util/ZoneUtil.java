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
        byte bzone = 0x00;
        if ("00".equals(zone)) {
            bzone = 0x00;
        } else if ("01".equals(zone)) {
            bzone = 0x01;
        } else if ("02".equals(zone)) {
            bzone = 0x02;
        } else if ("03".equals(zone)) {
            bzone = 0x03;
        } else if ("04".equals(zone)) {
            bzone = 0x04;
        } else if ("05".equals(zone)) {
            bzone = 0x05;
        } else if ("06".equals(zone)) {
            bzone = 0x06;
        } else if ("07".equals(zone)) {
            bzone = 0x07;
        } else if ("08".equals(zone)) {
            bzone = 0x08;
        } else if ("09".equals(zone)) {
            bzone = 0x09;
        } else if ("10".equals(zone)) {
            bzone = 0x10;
        } else if ("11".equals(zone)) {
            bzone = 0x11;
        } else if ("12".equals(zone)) {
            bzone = 0x12;
        }
        return bzone;
    }

}

