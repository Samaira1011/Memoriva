package com.example.memoriva.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.models.Memory;

import java.util.ArrayList;
import java.util.List;

public class MemoryDao {

    // -------------------------------------------------------------------------
    // Insert
    // -------------------------------------------------------------------------

    /**
     * Inserts a new memory row and returns the new row ID, or -1 on failure.
     */
    public long insertMemory(SQLiteDatabase db, Memory memory) {
        ContentValues values = buildContentValues(memory);
        return db.insert("memories", null, values);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the Memory with the given primary key, or null if not found.
     */
    public Memory getMemoryById(SQLiteDatabase db, int memoryId) {
        Cursor cursor = db.query(
                "memories", null,
                "memory_id = ?", new String[]{String.valueOf(memoryId)},
                null, null, null, "1"
        );
        try {
            return cursor.moveToFirst() ? cursorToMemory(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns all memories belonging to the given user, ordered by date descending.
     */
    public List<Memory> getMemoriesByUserId(SQLiteDatabase db, int userId) {
        Cursor cursor = db.query(
                "memories", null,
                "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "date DESC"
        );
        return cursorToList(cursor);
    }

    /**
     * Returns all memories for a user on a specific date (ISO format YYYY-MM-DD).
     */
    public List<Memory> getMemoriesByDate(SQLiteDatabase db, int userId, String date) {
        Cursor cursor = db.query(
                "memories", null,
                "user_id = ? AND date = ?",
                new String[]{String.valueOf(userId), date},
                null, null, "created_at DESC"
        );
        return cursorToList(cursor);
    }

    /**
     * Returns all memories linked to a specific trip via memory_trip_mapping.
     */
    public List<Memory> getMemoriesByTripId(SQLiteDatabase db, int tripId) {
        String sql =
                "SELECT m.* FROM memories m " +
                "INNER JOIN memory_trip_mapping mtm ON m.memory_id = mtm.memory_id " +
                "WHERE mtm.trip_id = ? " +
                "ORDER BY m.date ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(tripId)});
        return cursorToList(cursor);
    }

    /**
     * Returns all memories for a user that have an associated place (place_id IS NOT NULL).
     */
    public List<Memory> getMemoriesWithLocation(SQLiteDatabase db, int userId) {
        Cursor cursor = db.query(
                "memories", null,
                "user_id = ? AND place_id IS NOT NULL",
                new String[]{String.valueOf(userId)},
                null, null, "date DESC"
        );
        return cursorToList(cursor);
    }

    /**
     * Returns a single random memory for the given user, or null if none exist.
     */
    public Memory getRandomMemory(SQLiteDatabase db, int userId) {
        Cursor cursor = db.query(
                "memories", null,
                "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "RANDOM()", "1"
        );
        try {
            return cursor.moveToFirst() ? cursorToMemory(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns memories whose date matches the given month-day pattern (MM-dd) across all years.
     * monthDay must be in "MM-dd" format, e.g. "07-04".
     */
    public List<Memory> getMemoriesOnThisDay(SQLiteDatabase db, int userId, String monthDay) {
        // SQLite date column is stored as YYYY-MM-DD; substr(date, 6, 5) gives MM-DD
        String sql =
                "SELECT * FROM memories " +
                "WHERE user_id = ? AND substr(date, 6, 5) = ? " +
                "ORDER BY date DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId), monthDay});
        return cursorToList(cursor);
    }

    // -------------------------------------------------------------------------
    // Update / Delete
    // -------------------------------------------------------------------------

    /**
     * Updates all mutable fields for the given memory. Returns the number of rows affected.
     */
    public int updateMemory(SQLiteDatabase db, Memory memory) {
        ContentValues values = buildContentValues(memory);
        return db.update(
                "memories", values,
                "memory_id = ?", new String[]{String.valueOf(memory.getMemoryId())}
        );
    }

    /**
     * Deletes the memory with the given ID. Returns the number of rows deleted.
     */
    public int deleteMemory(SQLiteDatabase db, int memoryId) {
        return db.delete("memories", "memory_id = ?", new String[]{String.valueOf(memoryId)});
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContentValues buildContentValues(Memory memory) {
        ContentValues values = new ContentValues();
        values.put("user_id", memory.getUserId());
        values.put("title", memory.getTitle());
        values.put("date", memory.getDate());
        if (memory.getPlaceId() > 0) {
            values.put("place_id", memory.getPlaceId());
        } else {
            values.putNull("place_id");
        }
        values.put("notes", memory.getNotes());
        values.put("photo_paths", memory.getPhotoPaths());
        values.put("is_shared", memory.isShared() ? 1 : 0);
        values.put("is_locked", memory.isLocked() ? 1 : 0);
        values.put("created_at", memory.getCreatedAt());
        values.put("updated_at", memory.getUpdatedAt());
        return values;
    }

    private List<Memory> cursorToList(Cursor cursor) {
        List<Memory> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToMemory(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private Memory cursorToMemory(Cursor cursor) {
        Memory memory = new Memory();
        memory.setMemoryId(cursor.getInt(cursor.getColumnIndexOrThrow("memory_id")));
        memory.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        memory.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        memory.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
        int placeIdIdx = cursor.getColumnIndexOrThrow("place_id");
        memory.setPlaceId(cursor.isNull(placeIdIdx) ? 0 : cursor.getInt(placeIdIdx));
        memory.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        memory.setPhotoPaths(cursor.getString(cursor.getColumnIndexOrThrow("photo_paths")));
        memory.setShared(cursor.getInt(cursor.getColumnIndexOrThrow("is_shared")) == 1);
        memory.setLocked(cursor.getInt(cursor.getColumnIndexOrThrow("is_locked")) == 1);
        memory.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        memory.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")));
        return memory;
    }
}
