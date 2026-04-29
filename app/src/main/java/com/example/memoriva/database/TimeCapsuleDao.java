package com.example.memoriva.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.models.TimeCapsule;

import java.util.ArrayList;
import java.util.List;

public class TimeCapsuleDao {

    // -------------------------------------------------------------------------
    // Insert
    // -------------------------------------------------------------------------

    /**
     * Inserts a new time capsule row and returns the new row ID, or -1 on failure.
     */
    public long insertTimeCapsule(SQLiteDatabase db, TimeCapsule capsule) {
        ContentValues values = buildContentValues(capsule);
        return db.insert("time_capsules", null, values);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the TimeCapsule associated with the given memory ID, or null if not found.
     */
    public TimeCapsule getTimeCapsuleByMemoryId(SQLiteDatabase db, int memoryId) {
        Cursor cursor = db.query(
                "time_capsules", null,
                "memory_id = ?", new String[]{String.valueOf(memoryId)},
                null, null, null, "1"
        );
        try {
            return cursor.moveToFirst() ? cursorToTimeCapsule(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns all time capsules for the given user whose open_date is on or before
     * currentDate and that have not yet been opened.
     *
     * @param currentDate ISO date string (YYYY-MM-DD) representing today's date.
     */
    public List<TimeCapsule> getReadyTimeCapsules(SQLiteDatabase db, int userId,
                                                  String currentDate) {
        Cursor cursor = db.query(
                "time_capsules", null,
                "user_id = ? AND open_date <= ? AND is_opened = 0",
                new String[]{String.valueOf(userId), currentDate},
                null, null, "open_date ASC"
        );
        return cursorToList(cursor);
    }

    // -------------------------------------------------------------------------
    // Update / Delete
    // -------------------------------------------------------------------------

    /**
     * Marks the time capsule as opened and records the opened_at timestamp.
     * Returns the number of rows affected.
     */
    public int markTimeCapsuleOpened(SQLiteDatabase db, int capsuleId) {
        ContentValues values = new ContentValues();
        values.put("is_opened", 1);
        values.put("opened_at", System.currentTimeMillis());
        return db.update(
                "time_capsules", values,
                "capsule_id = ?", new String[]{String.valueOf(capsuleId)}
        );
    }

    /**
     * Deletes the time capsule with the given ID. Returns the number of rows deleted.
     */
    public int deleteTimeCapsule(SQLiteDatabase db, int capsuleId) {
        return db.delete(
                "time_capsules",
                "capsule_id = ?",
                new String[]{String.valueOf(capsuleId)}
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContentValues buildContentValues(TimeCapsule capsule) {
        ContentValues values = new ContentValues();
        values.put("memory_id", capsule.getMemoryId());
        values.put("user_id", capsule.getUserId());
        values.put("open_date", capsule.getOpenDate());
        values.put("message", capsule.getMessage());
        values.put("is_opened", capsule.isOpened() ? 1 : 0);
        values.put("created_at", capsule.getCreatedAt());
        values.put("opened_at", capsule.getOpenedAt());
        return values;
    }

    private List<TimeCapsule> cursorToList(Cursor cursor) {
        List<TimeCapsule> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToTimeCapsule(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private TimeCapsule cursorToTimeCapsule(Cursor cursor) {
        TimeCapsule capsule = new TimeCapsule();
        capsule.setCapsuleId(cursor.getInt(cursor.getColumnIndexOrThrow("capsule_id")));
        capsule.setMemoryId(cursor.getInt(cursor.getColumnIndexOrThrow("memory_id")));
        capsule.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        capsule.setOpenDate(cursor.getString(cursor.getColumnIndexOrThrow("open_date")));
        capsule.setMessage(cursor.getString(cursor.getColumnIndexOrThrow("message")));
        capsule.setOpened(cursor.getInt(cursor.getColumnIndexOrThrow("is_opened")) == 1);
        capsule.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        capsule.setOpenedAt(cursor.getLong(cursor.getColumnIndexOrThrow("opened_at")));
        return capsule;
    }
}
