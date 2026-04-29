package com.example.memoriva.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.models.Place;

import java.util.ArrayList;
import java.util.List;

public class PlaceDao {

    // -------------------------------------------------------------------------
    // Insert
    // -------------------------------------------------------------------------

    /**
     * Inserts a new place row and returns the new row ID, or -1 on failure.
     */
    public long insertPlace(SQLiteDatabase db, Place place) {
        ContentValues values = buildContentValues(place);
        return db.insert("places", null, values);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the Place with the given primary key, or null if not found.
     */
    public Place getPlaceById(SQLiteDatabase db, int placeId) {
        Cursor cursor = db.query(
                "places", null,
                "place_id = ?", new String[]{String.valueOf(placeId)},
                null, null, null, "1"
        );
        try {
            return cursor.moveToFirst() ? cursorToPlace(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns all distinct places visited by a user (via their memories).
     */
    public List<Place> getPlacesByUserId(SQLiteDatabase db, int userId) {
        String sql =
                "SELECT DISTINCT p.* FROM places p " +
                "INNER JOIN memories m ON p.place_id = m.place_id " +
                "WHERE m.user_id = ? " +
                "ORDER BY p.name ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        return cursorToList(cursor);
    }

    // -------------------------------------------------------------------------
    // Update / Delete
    // -------------------------------------------------------------------------

    /**
     * Updates all mutable fields for the given place. Returns the number of rows affected.
     */
    public int updatePlace(SQLiteDatabase db, Place place) {
        ContentValues values = buildContentValues(place);
        return db.update(
                "places", values,
                "place_id = ?", new String[]{String.valueOf(place.getPlaceId())}
        );
    }

    /**
     * Deletes the place with the given ID. Returns the number of rows deleted.
     */
    public int deletePlace(SQLiteDatabase db, int placeId) {
        return db.delete("places", "place_id = ?", new String[]{String.valueOf(placeId)});
    }

    // -------------------------------------------------------------------------
    // Aggregate stats
    // -------------------------------------------------------------------------

    /**
     * Returns the number of distinct countries visited by the user (via their memories).
     */
    public int getUniqueCountriesCount(SQLiteDatabase db, int userId) {
        String sql =
                "SELECT COUNT(DISTINCT p.country) FROM places p " +
                "INNER JOIN memories m ON p.place_id = m.place_id " +
                "WHERE m.user_id = ? AND p.country IS NOT NULL";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns the number of distinct cities visited by the user (via their memories).
     */
    public int getUniqueCitiesCount(SQLiteDatabase db, int userId) {
        String sql =
                "SELECT COUNT(DISTINCT p.city) FROM places p " +
                "INNER JOIN memories m ON p.place_id = m.place_id " +
                "WHERE m.user_id = ? AND p.city IS NOT NULL";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns the Place that appears most frequently in the user's memories,
     * or null if the user has no memories with locations.
     */
    public Place getMostVisitedPlace(SQLiteDatabase db, int userId) {
        String sql =
                "SELECT p.*, COUNT(m.memory_id) AS visit_count FROM places p " +
                "INNER JOIN memories m ON p.place_id = m.place_id " +
                "WHERE m.user_id = ? " +
                "GROUP BY p.place_id " +
                "ORDER BY visit_count DESC " +
                "LIMIT 1";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        try {
            return cursor.moveToFirst() ? cursorToPlace(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContentValues buildContentValues(Place place) {
        ContentValues values = new ContentValues();
        values.put("name", place.getName());
        values.put("latitude", place.getLatitude());
        values.put("longitude", place.getLongitude());
        values.put("address", place.getAddress());
        values.put("city", place.getCity());
        values.put("country", place.getCountry());
        values.put("created_at", place.getCreatedAt());
        return values;
    }

    private List<Place> cursorToList(Cursor cursor) {
        List<Place> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToPlace(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private Place cursorToPlace(Cursor cursor) {
        Place place = new Place();
        place.setPlaceId(cursor.getInt(cursor.getColumnIndexOrThrow("place_id")));
        place.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        place.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
        place.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
        place.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
        place.setCity(cursor.getString(cursor.getColumnIndexOrThrow("city")));
        place.setCountry(cursor.getString(cursor.getColumnIndexOrThrow("country")));
        place.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        return place;
    }
}
