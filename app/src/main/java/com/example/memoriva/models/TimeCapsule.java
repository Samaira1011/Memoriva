package com.example.memoriva.models;

public class TimeCapsule {

    private int capsuleId;
    private int memoryId;
    private int userId;
    private String openDate;
    private String message;
    private boolean isOpened;
    private long createdAt;
    private long openedAt;

    public TimeCapsule() {
    }

    public TimeCapsule(int capsuleId, int memoryId, int userId, String openDate,
                       String message, boolean isOpened, long createdAt, long openedAt) {
        this.capsuleId = capsuleId;
        this.memoryId = memoryId;
        this.userId = userId;
        this.openDate = openDate;
        this.message = message;
        this.isOpened = isOpened;
        this.createdAt = createdAt;
        this.openedAt = openedAt;
    }

    public int getCapsuleId() { return capsuleId; }
    public void setCapsuleId(int capsuleId) { this.capsuleId = capsuleId; }

    public int getMemoryId() { return memoryId; }
    public void setMemoryId(int memoryId) { this.memoryId = memoryId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOpenDate() { return openDate; }
    public void setOpenDate(String openDate) { this.openDate = openDate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isOpened() { return isOpened; }
    public void setOpened(boolean opened) { isOpened = opened; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getOpenedAt() { return openedAt; }
    public void setOpenedAt(long openedAt) { this.openedAt = openedAt; }
}
