package com.example.memoriva.models;

public class Friend {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_DECLINED = "DECLINED";

    private int friendshipId;
    private int userId;
    private int friendId;
    private String status;
    private long createdAt;

    public Friend() {
    }

    public Friend(int friendshipId, int userId, int friendId, String status, long createdAt) {
        this.friendshipId = friendshipId;
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getFriendshipId() { return friendshipId; }
    public void setFriendshipId(int friendshipId) { this.friendshipId = friendshipId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getFriendId() { return friendId; }
    public void setFriendId(int friendId) { this.friendId = friendId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
