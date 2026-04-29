package com.example.memoriva.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.models.DreamDestination;

import java.util.ArrayList;
import java.util.List;

public class DreamDestinationDao {

    // -------------------------------------------------------------------------
    // Insert
    // -------------------------------------------------------------------------

    /**
     * Inserts a new dream destination row and returns the new row ID, or -1 on failure.
     */
    public long insertDreamDestination(SQLiteDatabase db, DreamDestination dest) {
        ContentValues values = buildContentValues(dest);
        return db.insert("dream_destinations", null, values);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the DreamDestination with the given primary key, or null if not found.
     */
    public DreamDestination getDreamDestinationById(SQLiteDatabase db, int dreamId) {
        Cursor cursor = db.query(
                "dream_destinations", null,
                "dream_id = ?", new String[]{String.valueOf(dreamId)},
                null, null, null, "1"
        );
        try {
            return cursor.moveToFirst() ? cursorToDreamDestination(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns all dream destinations for the given user, ordered by sort_order then created_at.
     */
    public List<DreamDestination> getDreamDestinationsByUserId(SQLiteDatabase db, int userId) {
        Cursor cursor = db.query(
                "dream_destinations", null,
                "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "sort_order ASC, created_at DESC"
        );
        return cursorToList(cursor);
    }

    /**
     * Returns dream destinations for the given user filtered by status (PLANNED or WISHLIST).
     */
    public List<DreamDestination> getDreamDestinationsByStatus(SQLiteDatabase db, int userId,
                                                               String status) {
        Cursor cursor = db.query(
                "dream_destinations", null,
                "user_id = ? AND status = ?",
                new String[]{String.valueOf(userId), status},
                null, null, "sort_order ASC, created_at DESC"
        );
        return cursorToList(cursor);
    }

    // -------------------------------------------------------------------------
    // Update / Delete
    // -------------------------------------------------------------------------

    /**
     * Updates all mutable fields for the given dream destination.
     * Returns the number of rows affected.
     */
    public int updateDreamDestination(SQLiteDatabase db, DreamDestination dest) {
        ContentValues values = buildContentValues(dest);
        return db.update(
                "dream_destinations", values,
                "dream_id = ?", new String[]{String.valueOf(dest.getDreamId())}
        );
    }

    /**
     * Deletes the dream destination with the given ID. Returns the number of rows deleted.
     */
    public int deleteDreamDestination(SQLiteDatabase db, int dreamId) {
        return db.delete(
                "dream_destinations",
                "dream_id = ?",
                new String[]{String.valueOf(dreamId)}
        );
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    /**
     * Returns true if the user already has a dream destination with the given place name
     * (case-insensitive comparison).
     */
    public boolean isDuplicateDestination(SQLiteDatabase db, int userId, String placeName) {
        Cursor cursor = db.query(
                "dream_destinations",
                new String[]{"dream_id"},
                "user_id = ? AND LOWER(place_name) = LOWER(?)",
                new String[]{String.valueOf(userId), placeName},
                null, null, null, "1"
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContentValues buildContentValues(DreamDestination dest) {
        ContentValues values = new ContentValues();
        values.put("user_id", dest.getUserId());
        values.put("place_name", dest.getPlaceName());
        values.put("status", dest.getStatus());
        values.put("expected_date", dest.getExpectedDate());
        values.put("cover_image_path", dest.getCoverImagePath());
        values.put("notes", dest.getNotes());
        values.put("budget_estimate", dest.getBudgetEstimate());
        values.put("sort_order", dest.getSortOrder());
        values.put("created_at", dest.getCreatedAt());
        return values;
    }

    private List<DreamDestination> cursorToList(Cursor cursor) {
        List<DreamDestination> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToDreamDestination(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private DreamDestination cursorToDreamDestination(Cursor cursor) {
        DreamDestination dest = new DreamDestination();
        dest.setDreamId(cursor.getInt(cursor.getColumnIndexOrThrow("dream_id")));
        dest.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        dest.setPlaceName(cursor.getString(cursor.getColumnIndexOrThrow("place_name")));
        dest.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        dest.setExpectedDate(cursor.getString(cursor.getColumnIndexOrThrow("expected_date")));
        dest.setCoverImagePath(cursor.getString(cursor.getColumnIndexOrThrow("cover_image_path")));
        dest.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        dest.setBudgetEstimate(cursor.getDouble(cursor.getColumnIndexOrThrow("budget_estimate")));
        dest.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow("sort_order")));
        dest.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        return dest;
    }
}
