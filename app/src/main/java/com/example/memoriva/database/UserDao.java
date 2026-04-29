package com.example.memoriva.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.models.User;

public class UserDao {

    // -------------------------------------------------------------------------
    // Insert
    // -------------------------------------------------------------------------

    /**
     * Inserts a new user row and returns the new row ID, or -1 on failure.
     */
    public long insertUser(SQLiteDatabase db, User user) {
        ContentValues values = new ContentValues();
        values.put("firebase_uid", user.getFirebaseUid());
        values.put("email", user.getEmail());
        values.put("full_name", user.getFullName());
        values.put("username", user.getUsername());
        values.put("bio", user.getBio());
        values.put("avatar_url", user.getAvatarUrl());
        values.put("created_at", user.getCreatedAt());
        return db.insert("users", null, values);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the User matching the given Firebase UID, or null if not found.
     */
    public User getUserByFirebaseUid(SQLiteDatabase db, String firebaseUid) {
        Cursor cursor = db.query(
                "users",
                null,
                "firebase_uid = ?",
                new String[]{firebaseUid},
                null, null, null, "1"
        );
        try {
            if (cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns the User with the given primary key, or null if not found.
     */
    public User getUserById(SQLiteDatabase db, int userId) {
        Cursor cursor = db.query(
                "users",
                null,
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null, "1"
        );
        try {
            if (cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    // -------------------------------------------------------------------------
    // Update / Delete
    // -------------------------------------------------------------------------

    /**
     * Updates all mutable fields for the given user. Returns the number of rows affected.
     */
    public int updateUser(SQLiteDatabase db, User user) {
        ContentValues values = new ContentValues();
        values.put("firebase_uid", user.getFirebaseUid());
        values.put("email", user.getEmail());
        values.put("full_name", user.getFullName());
        values.put("username", user.getUsername());
        values.put("bio", user.getBio());
        values.put("avatar_url", user.getAvatarUrl());
        return db.update(
                "users",
                values,
                "user_id = ?",
                new String[]{String.valueOf(user.getUserId())}
        );
    }

    /**
     * Deletes the user with the given ID.
     */
    public void deleteUser(SQLiteDatabase db, int userId) {
        db.delete("users", "user_id = ?", new String[]{String.valueOf(userId)});
    }

    // -------------------------------------------------------------------------
    // Cursor mapping
    // -------------------------------------------------------------------------

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        user.setFirebaseUid(cursor.getString(cursor.getColumnIndexOrThrow("firebase_uid")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        user.setBio(cursor.getString(cursor.getColumnIndexOrThrow("bio")));
        user.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow("avatar_url")));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        return user;
    }
}
