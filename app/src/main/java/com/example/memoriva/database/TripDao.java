package com.example.memoriva.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.models.Trip;

import java.util.ArrayList;
import java.util.List;

public class TripDao {

    // -------------------------------------------------------------------------
    // Insert
    // -------------------------------------------------------------------------

    /**
     * Inserts a new trip row and returns the new row ID, or -1 on failure.
     */
    public long insertTrip(SQLiteDatabase db, Trip trip) {
        ContentValues values = buildContentValues(trip);
        return db.insert("trips", null, values);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the Trip with the given primary key, or null if not found.
     */
    public Trip getTripById(SQLiteDatabase db, int tripId) {
        Cursor cursor = db.query(
                "trips", null,
                "trip_id = ?", new String[]{String.valueOf(tripId)},
                null, null, null, "1"
        );
        try {
            return cursor.moveToFirst() ? cursorToTrip(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns all trips for the given user, ordered by start_date descending.
     */
    public List<Trip> getTripsByUserId(SQLiteDatabase db, int userId) {
        Cursor cursor = db.query(
                "trips", null,
                "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "start_date DESC"
        );
        return cursorToList(cursor);
    }

    // -------------------------------------------------------------------------
    // Update / Delete
    // -------------------------------------------------------------------------

    /**
     * Updates all mutable fields for the given trip. Returns the number of rows affected.
     */
    public int updateTrip(SQLiteDatabase db, Trip trip) {
        ContentValues values = buildContentValues(trip);
        return db.update(
                "trips", values,
                "trip_id = ?", new String[]{String.valueOf(trip.getTripId())}
        );
    }

    /**
     * Deletes the trip with the given ID. Returns the number of rows deleted.
     */
    public int deleteTrip(SQLiteDatabase db, int tripId) {
        return db.delete("trips", "trip_id = ?", new String[]{String.valueOf(tripId)});
    }

    // -------------------------------------------------------------------------
    // Memory-Trip mapping
    // -------------------------------------------------------------------------

    /**
     * Links a memory to a trip. Silently ignores duplicate mappings (UNIQUE constraint).
     */
    public void addMemoryToTrip(SQLiteDatabase db, int memoryId, int tripId) {
        ContentValues values = new ContentValues();
        values.put("memory_id", memoryId);
        values.put("trip_id", tripId);
        // INSERT OR IGNORE handles the UNIQUE(memory_id, trip_id) constraint gracefully
        db.insertWithOnConflict("memory_trip_mapping", null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Removes the link between a memory and a trip.
     */
    public void removeMemoryFromTrip(SQLiteDatabase db, int memoryId, int tripId) {
        db.delete(
                "memory_trip_mapping",
                "memory_id = ? AND trip_id = ?",
                new String[]{String.valueOf(memoryId), String.valueOf(tripId)}
        );
    }

    // -------------------------------------------------------------------------
    // Aggregate stats
    // -------------------------------------------------------------------------

    /**
     * Returns the number of calendar days spanned by the trip (inclusive).
     * Calculated from start_date and end_date stored as ISO strings (YYYY-MM-DD).
     * Returns 0 if the trip is not found.
     */
    public int getTripDaysCount(SQLiteDatabase db, int tripId) {
        String sql =
                "SELECT (julianday(end_date) - julianday(start_date) + 1) AS days " +
                "FROM trips WHERE trip_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(tripId)});
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns the number of distinct places visited during the trip
     * (memories linked to the trip that have a non-null place_id).
     */
    public int getTripPlacesCount(SQLiteDatabase db, int tripId) {
        String sql =
                "SELECT COUNT(DISTINCT m.place_id) FROM memories m " +
                "INNER JOIN memory_trip_mapping mtm ON m.memory_id = mtm.memory_id " +
                "WHERE mtm.trip_id = ? AND m.place_id IS NOT NULL";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(tripId)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns the total number of photos across all memories linked to the trip.
     * photo_paths is stored as a JSON array; this counts non-null photo_paths entries
     * using json_array_length (available in SQLite 3.38+). Falls back to counting
     * memories with photos if json_array_length is unavailable.
     */
    public int getTripPhotosCount(SQLiteDatabase db, int tripId) {
        // Use json_array_length to sum photo counts across all linked memories
        String sql =
                "SELECT SUM(CASE WHEN m.photo_paths IS NOT NULL AND m.photo_paths != '' " +
                "  THEN json_array_length(m.photo_paths) ELSE 0 END) " +
                "FROM memories m " +
                "INNER JOIN memory_trip_mapping mtm ON m.memory_id = mtm.memory_id " +
                "WHERE mtm.trip_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(tripId)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContentValues buildContentValues(Trip trip) {
        ContentValues values = new ContentValues();
        values.put("user_id", trip.getUserId());
        values.put("name", trip.getName());
        values.put("start_date", trip.getStartDate());
        values.put("end_date", trip.getEndDate());
        values.put("destination", trip.getDestination());
        values.put("description", trip.getDescription());
        values.put("cover_photo_path", trip.getCoverPhotoPath());
        values.put("created_at", trip.getCreatedAt());
        return values;
    }

    private List<Trip> cursorToList(Cursor cursor) {
        List<Trip> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToTrip(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private Trip cursorToTrip(Cursor cursor) {
        Trip trip = new Trip();
        trip.setTripId(cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")));
        trip.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        trip.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        trip.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
        trip.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
        trip.setDestination(cursor.getString(cursor.getColumnIndexOrThrow("destination")));
        trip.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        trip.setCoverPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow("cover_photo_path")));
        trip.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        return trip;
    }
}
