package com.example.memoriva.models;

public class Review {

    private int reviewId;
    private int userId;
    private int placeId;
    private int rating;
    private String reviewText;
    private long createdAt;
    private long updatedAt;

    public Review() {
    }

    public Review(int reviewId, int userId, int placeId, int rating,
                  String reviewText, long createdAt, long updatedAt) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.placeId = placeId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getPlaceId() { return placeId; }
    public void setPlaceId(int placeId) { this.placeId = placeId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
