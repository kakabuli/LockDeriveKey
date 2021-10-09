package com.revolo.lock.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;

import timber.log.Timber;

public class ZoneUtil {
    /**
     * 获取时区
     *
     * @return
     */
    public static String getZone() {
        String stZone = "+00:00";
        TimeZone tz = TimeZone.getDefault();
        String zone = getOffsetString(true, tz.getRawOffset(), tz.getDSTSavings());
        Timber.e("zone:" + zone);
        if (null != zone && !"".equals(zone)) {
            return zone;
        } else {
            return stZone;
        }
    }

    /**
     * 兼容中文版本外语言下获取时区算法
     * @param includeMinuteSeparator
     * @param offsetMillis
     * @param mDSTSavings
     * @return
     */
    public static String getOffsetString(boolean includeMinuteSeparator, int offsetMillis, int mDSTSavings) {
        int offsetMinutes = offsetMillis / 60000;
        char sign = '+';
        if (offsetMinutes < 0) {
            sign = '-';
            offsetMinutes = -offsetMinutes;

        }
        if (mDSTSavings / 60000 > 0)
            offsetMinutes = offsetMinutes - (mDSTSavings / 60000);
        StringBuilder builder = new StringBuilder(6);
        builder.append(sign);
        appendNumber(builder, 2, offsetMinutes / 60);
        if (includeMinuteSeparator) {
            builder.append(':');
        }
        appendNumber(builder, 2, offsetMinutes % 60);
        return builder.toString();

    }

    private static void appendNumber(StringBuilder builder, int count, int value) {
        String string = Integer.toString(value);
        for (int i = 0; i < count - string.length(); i++) {
            builder.append('0');
        }
        builder.append(string);
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    public static long getTime() {
        return System.currentTimeMillis();
    }

    public static long getCreatePwdTime(String timeZone) {
        String time = getDate("", getTime());
        Timber.e("time:" + time);
        long t = getTime("", time);
        Timber.e("timess:" + t);
        long tes = ZoneUtil.getTestTime2(timeZone);
        Timber.e("偏移量times:" + tes);
        return t +tes;
    }

    public static long getZoneTime(long zoneTime, String zone) {
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

    /**
     * 以零时区为参照获取时间戳
     *
     * @param timeZone 默认零时区
     * @param timeStr  时间
     * @param pattern  时间格式
     * @return
     */
    public static long getTime(String timeZone, String timeStr, String pattern) {
        timeZone = "+00:00";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        ParsePosition pos = new ParsePosition(0);
        Date result = formatter.parse(timeStr, pos);
        return result.getTime();
    }

    /**
     * 获取时间戳
     *
     * @param timeZone 时区默认零时区
     * @param timeStr  时间
     *                 默认时间格式
     * @return
     */
    public static long getTime(String timeZone, String timeStr) {
        timeZone = "+00:00";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        ParsePosition pos = new ParsePosition(0);
        Date result = formatter.parse(timeStr, pos);
        return result.getTime();
    }

    /**
     * 获取时间戳
     *
     * @param timeStr 时间
     *                默认零时区
     *                默认时间格式
     * @return
     */
    public static long getTime(String timeStr) {
        String timeZone = "+00:00";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        ParsePosition pos = new ParsePosition(0);
        Date result = formatter.parse(timeStr, pos);
        return result.getTime();
    }

    /**
     * 获取时间戳
     *
     * @param timeZone
     * @param time
     * @return
     */
    public static String getDate(String timeZone, long time) {
        timeZone = "+00:00";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        Date date = new Date();
        date.setTime(time);
        return formatter.format(date);
    }

    /**
     * 获取0时区时间戳
     *
     * @param time
     * @return
     */
    public static String getZeroTimeZoneDate(long time) {
        String timeZone = "+00:00";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
        Date date = new Date();
        date.setTime(time);
        return formatter.format(date);
    }

    /**
     * 获取锁端时区
     *
     * @param zone
     * @return
     */
    public static long getTestTime(String zone) {
        return (getTime("", getDate("", System.currentTimeMillis())) + getTestTime2(zone));
    }

    /**
     * 获取锁端时区
     *
     * @param zone
     * @return
     */
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
        Timber.e("timeZone:" + timeZone + ";time:" + time + ";patt:" + pattern);
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
            int zoneValer = Integer.parseInt(z1) * 4 + (Integer.parseInt(z2) / 15);
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

