package com.example.memoriva.network;

public class FeedItem {
    private int memoryId;
    private int userId;
    private String username;
    private String avatarUrl;
    private String title;
    private String date;
    private String firstPhotoPath;

    public FeedItem() {}

    public int getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(int memoryId) {
        this.memoryId = memoryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFirstPhotoPath() {
        return firstPhotoPath;
    }

    public void setFirstPhotoPath(String firstPhotoPath) {
        this.firstPhotoPath = firstPhotoPath;
    }
}
