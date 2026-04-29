package com.example.memoriva.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.memoriva.OnThisDayActivity;
import com.example.memoriva.OnThisDayReceiver;
import com.example.memoriva.R;

import java.util.Calendar;

public class NotificationHelper {

    public static final String CHANNEL_ID_MEMORIES = "memoriva_memories";
    public static final String CHANNEL_ID_CAPSULES = "memoriva_capsules";
    public static final int NOTIF_ON_THIS_DAY = 1001;
    public static final int NOTIF_TIME_CAPSULE = 1002;

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Memories channel
            NotificationChannel memoriesChannel = new NotificationChannel(
                    CHANNEL_ID_MEMORIES,
                    "Memories",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            memoriesChannel.setDescription("Notifications about your memories");
            manager.createNotificationChannel(memoriesChannel);

            // Time Capsules channel
            NotificationChannel capsulesChannel = new NotificationChannel(
                    CHANNEL_ID_CAPSULES,
                    "Time Capsules",
                    NotificationManager.IMPORTANCE_HIGH
            );
            capsulesChannel.setDescription("Notifications when time capsules are ready to open");
            manager.createNotificationChannel(capsulesChannel);
        }
    }

    public static void showOnThisDayNotification(Context context, int memoryCount) {
        Intent intent = new Intent(context, OnThisDayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, NOTIF_ON_THIS_DAY, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_MEMORIES)
                .setSmallIcon(R.drawable.ic_calendar)
                .setContentTitle("On This Day \uD83D\uDDD3\uFE0F")
                .setContentText("You have " + memoryCount + " memor" +
                        (memoryCount == 1 ? "y" : "ies") + " from today in past years")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIF_ON_THIS_DAY, builder.build());
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    public static void showTimeCapsuleNotification(Context context, String memoryTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_CAPSULES)
                .setSmallIcon(R.drawable.ic_time_capsule)
                .setContentTitle("Time Capsule Ready! \uD83D\uDCEC")
                .setContentText("Your time capsule for '" + memoryTitle + "' is ready to open")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIF_TIME_CAPSULE, builder.build());
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    public static void scheduleOnThisDayNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, OnThisDayReceiver.class);
        intent.setAction("com.example.memoriva.ON_THIS_DAY");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule daily at 9:00 AM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If 9 AM has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }
}
