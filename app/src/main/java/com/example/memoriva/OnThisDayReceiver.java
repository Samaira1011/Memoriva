package com.example.memoriva;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.database.MemorivaDbHelper;
import com.example.memoriva.database.MemoryDao;
import com.example.memoriva.models.Memory;
import com.example.memoriva.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OnThisDayReceiver extends BroadcastReceiver {

    private static final int USER_ID = 1; // placeholder

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get today's MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
        String monthDay = sdf.format(new Date());

        MemorivaDbHelper dbHelper = new MemorivaDbHelper(context);
        MemoryDao memoryDao = new MemoryDao();

        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            List<Memory> memories = memoryDao.getMemoriesOnThisDay(db, USER_ID, monthDay);
            if (!memories.isEmpty()) {
                NotificationHelper.showOnThisDayNotification(context, memories.size());
            }
        } finally {
            dbHelper.close();
        }
    }
}
