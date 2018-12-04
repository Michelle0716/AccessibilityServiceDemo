package com.michelle.accessibilityservice.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.Locale;

public class AlarmTaskUtil {

    public static void starAlarmTaskByService(Context context, int intervalMinute, Intent intent) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = System.currentTimeMillis() + (intervalMinute * 60 * 1000);

        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        } else {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        }
    }

    public static void starAlarmTaskByService(Context context, long triggerAtMillis, Intent intent) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
    }

    /**
     * RepeatAlarmTask
     *
     * @param context
     * @param intent
     */
    public static void starRepeatAlarmTaskByService(Context context, int hour, int minute, long intervalMillis, Intent intent) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            //系统时间大于7点
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        }

        long triggerAtMillis = calendar.getTimeInMillis();

        PendingIntent operation;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //Android 4.4
            operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            //设置闹钟闹钟将在精确的时间被激发
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            //设置闹钟将在精确的时间段内被激发。setWindow(int type,long windowStartMillis,long windowLengthMillis,PendingIntent operation)
        } else {
            operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
           // 设置一个周期性执行的定时服务
            mAlarmManager.setRepeating(AlarmManager.RTC, triggerAtMillis, intervalMillis, operation);
        }
    }

    public static void stopAlarmTaskByService(Context context, Intent intent) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.cancel(operation);
    }

}
