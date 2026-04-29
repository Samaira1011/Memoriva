package com.example.memoriva.models;

public class User {

    private int userId;
    private String firebaseUid;
    private String email;
    private String fullName;
    private String username;
    private String bio;
    private String avatarUrl;
    private long createdAt;

    public User() {
    }

    public User(int userId, String firebaseUid, String email, String fullName,
                String username, String bio, String avatarUrl, long createdAt) {
        this.userId = userId;
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.fullName = fullName;
        this.username = username;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
