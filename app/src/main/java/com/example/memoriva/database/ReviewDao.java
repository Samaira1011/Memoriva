package com.example.memoriva.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.memoriva.models.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    // -------------------------------------------------------------------------
    // Insert
    // -------------------------------------------------------------------------

    /**
     * Inserts a new review row and returns the new row ID, or -1 on failure.
     */
    public long insertReview(SQLiteDatabase db, Review review) {
        ContentValues values = buildContentValues(review);
        return db.insert("reviews", null, values);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the Review with the given primary key, or null if not found.
     */
    public Review getReviewById(SQLiteDatabase db, int reviewId) {
        Cursor cursor = db.query(
                "reviews", null,
                "review_id = ?", new String[]{String.valueOf(reviewId)},
                null, null, null, "1"
        );
        try {
            return cursor.moveToFirst() ? cursorToReview(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Returns all reviews for a given place, ordered by most recent first.
     */
    public List<Review> getReviewsByPlaceId(SQLiteDatabase db, int placeId) {
        Cursor cursor = db.query(
                "reviews", null,
                "place_id = ?", new String[]{String.valueOf(placeId)},
                null, null, "created_at DESC"
        );
        return cursorToList(cursor);
    }

    // -------------------------------------------------------------------------
    // Update / Delete
    // -------------------------------------------------------------------------

    /**
     * Updates all mutable fields for the given review. Returns the number of rows affected.
     */
    public int updateReview(SQLiteDatabase db, Review review) {
        ContentValues values = buildContentValues(review);
        return db.update(
                "reviews", values,
                "review_id = ?", new String[]{String.valueOf(review.getReviewId())}
        );
    }

    /**
     * Deletes the review with the given ID. Returns the number of rows deleted.
     */
    public int deleteReview(SQLiteDatabase db, int reviewId) {
        return db.delete("reviews", "review_id = ?", new String[]{String.valueOf(reviewId)});
    }

    // -------------------------------------------------------------------------
    // Aggregate
    // -------------------------------------------------------------------------

    /**
     * Returns the average rating for a place, or 0.0 if there are no reviews.
     */
    public float getAverageRating(SQLiteDatabase db, int placeId) {
        Cursor cursor = db.rawQuery(
                "SELECT AVG(rating) FROM reviews WHERE place_id = ?",
                new String[]{String.valueOf(placeId)}
        );
        try {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getFloat(0);
            }
            return 0.0f;
        } finally {
            cursor.close();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContentValues buildContentValues(Review review) {
        ContentValues values = new ContentValues();
        values.put("user_id", review.getUserId());
        values.put("place_id", review.getPlaceId());
        values.put("rating", review.getRating());
        values.put("review_text", review.getReviewText());
        values.put("created_at", review.getCreatedAt());
        values.put("updated_at", review.getUpdatedAt());
        return values;
    }

    private List<Review> cursorToList(Cursor cursor) {
        List<Review> list = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToReview(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private Review cursorToReview(Cursor cursor) {
        Review review = new Review();
        review.setReviewId(cursor.getInt(cursor.getColumnIndexOrThrow("review_id")));
        review.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        review.setPlaceId(cursor.getInt(cursor.getColumnIndexOrThrow("place_id")));
        review.setRating(cursor.getInt(cursor.getColumnIndexOrThrow("rating")));
        review.setReviewText(cursor.getString(cursor.getColumnIndexOrThrow("review_text")));
        review.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        review.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")));
        return review;
    }
}
