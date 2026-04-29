package com.example.memoriva.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.models.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendDao {

    // -------------------------------------------------------------------------
    // Insert
    // -------------------------------------------------------------------------

    /**
     * Inserts a new friend request row and returns the new row ID, or -1 on failure.
     */
    public long insertFriendRequest(SQLiteDatabase db, Friend friend) {
        ContentValues values = buildContentValues(friend);
        return db.insert("friends", null, values);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns all accepted friends for the given user.
     */
    public List<Friend> getFriendsByUserId(SQLiteDatabase db, int userId) {
        Cursor cursor = db.query(
                "friends", null,
                "user_id = ? AND status = ?",
                new String[]{String.valueOf(userId), Friend.STATUS_ACCEPTED},
                null, null, "created_at DESC"
        );
        return cursorToList(cursor);
    }

    /**
     * Returns all pending friend requests directed at the given user.
     */
    public List<Friend> getPendingRequests(SQLiteDatabase db, int userId) {
        Cursor cursor = db.query(
                "friends", null,
                "friend_id = ? AND status = ?",
                new String[]{String.valueOf(userId), Friend.STATUS_PENDING},
                null, null, "created_at DESC"
        );
        return cursorToList(cursor);
    }

    // -------------------------------------------------------------------------
    // Update / Delete
    // -------------------------------------------------------------------------

    /**
     * Updates the status of a friendship record. Returns the number of rows affected.
     */
    public int updateFriendStatus(SQLiteDatabase db, int friendshipId, String status) {
        ContentValues values = new ContentValues();
        values.put("status", status);
        return db.update(
                "friends", values,
                "friendship_id = ?", new String[]{String.valueOf(friendshipId)}
        );
    }

    /**
     * Deletes the friendship record with the given ID. Returns the number of rows deleted.
     */
    public int deleteFriend(SQLiteDatabase db, int friendshipId) {
        return db.delete(
                "friends",
                "friendship_id = ?",
                new String[]{String.valueOf(friendshipId)}
        );
    }

    // -------------------------------------------------------------------------
    // Aggregate
    // -------------------------------------------------------------------------

    /**
     * Returns the number of accepted friends for the given user.
     */
    public int getFriendsCount(SQLiteDatabase db, int userId) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM friends WHERE user_id = ? AND status = ?",
                new String[]{String.valueOf(userId), Friend.STATUS_ACCEPTED}
        );
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContentValues buildContentValues(Friend friend) {
        ContentValues values = new ContentValues();
        values.put("user_id", friend.getUserId());
        values.put("friend_id", friend.getFriendId());
        values.put("status", friend.getStatus());
        values.put("created_at", friend.getCreatedAt());
        return values;
    }

    private List<Friend> cursorToList(Cursor cursor) {
        List<Friend> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToFriend(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private Friend cursorToFriend(Cursor cursor) {
        Friend friend = new Friend();
        friend.setFriendshipId(cursor.getInt(cursor.getColumnIndexOrThrow("friendship_id")));
        friend.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        friend.setFriendId(cursor.getInt(cursor.getColumnIndexOrThrow("friend_id")));
        friend.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        friend.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        return friend;
    }
}
