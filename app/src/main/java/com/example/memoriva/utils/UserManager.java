package com.example.memoriva.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.database.MemorivaDbHelper;
import com.google.firebase.auth.FirebaseUser;
/**
 * Manages the mapping between Firebase UIDs and local SQLite user IDs.
 * Each Firebase account gets its own local user_id, ensuring data separation.
 */
public class UserManager {

    private static int cachedUserId = -1;
    private static String cachedFirebaseUid = null;

    /**
     * Gets or creates the local SQLite user_id for the given Firebase user.
     * Returns 1 as fallback if user is null.
     */
    public static int getLocalUserId(Context context, FirebaseUser firebaseUser) {
        if (firebaseUser == null) return 1;

        String uid = firebaseUser.getUid();

        // Return cached value if same user
        if (uid.equals(cachedFirebaseUid) && cachedUserId != -1) {
            return cachedUserId;
        }

        MemorivaDbHelper dbHelper = new MemorivaDbHelper(context);
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            // Check if user already exists
            Cursor cursor = db.query("users", new String[]{"user_id"},
                    "firebase_uid = ?", new String[]{uid},
                    null, null, null, "1");
            try {
                if (cursor.moveToFirst()) {
                    int localId = cursor.getInt(0);
                    cachedUserId = localId;
                    cachedFirebaseUid = uid;
                    return localId;
                }
            } finally {
                cursor.close();
            }

            // Create new user record
            ContentValues values = new ContentValues();
            values.put("firebase_uid", uid);
            values.put("email", firebaseUser.getEmail() != null ? firebaseUser.getEmail() : uid + "@unknown.com");
            values.put("full_name", firebaseUser.getDisplayName() != null
                    ? firebaseUser.getDisplayName() : "Traveler");
            values.put("created_at", System.currentTimeMillis());

            long newId = db.insertWithOnConflict("users", null, values,
                    SQLiteDatabase.CONFLICT_IGNORE);
            if (newId > 0) {
                cachedUserId = (int) newId;
                cachedFirebaseUid = uid;
                return (int) newId;
            }

            // If insert failed due to conflict, try fetching again
            Cursor retry = db.query("users", new String[]{"user_id"},
                    "firebase_uid = ?", new String[]{uid}, null, null, null, "1");
            try {
                if (retry.moveToFirst()) {
                    int localId = retry.getInt(0);
                    cachedUserId = localId;
                    cachedFirebaseUid = uid;
                    return localId;
                }
            } finally {
                retry.close();
            }
        }

        return 1; // fallback
    }

    /** Call this on sign out to clear the cache */
    public static void clearCache() {
        cachedUserId = -1;
        cachedFirebaseUid = null;
    }
}
