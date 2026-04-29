package com.example.memoriva.models;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Memory {

    private int memoryId;
    private int userId;
    private String title;
    private String date;
    private int placeId;
    private String notes;
    private String photoPaths; // JSON array string
    private boolean isShared;
    private boolean isLocked;
    private long createdAt;
    private long updatedAt;

    public Memory() {
    }

    public Memory(int memoryId, int userId, String title, String date, int placeId,
                  String notes, String photoPaths, boolean isShared, boolean isLocked,
                  long createdAt, long updatedAt) {
        this.memoryId = memoryId;
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.placeId = placeId;
        this.notes = notes;
        this.photoPaths = photoPaths;
        this.isShared = isShared;
        this.isLocked = isLocked;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Parses the photoPaths JSON string and returns a list of file path strings.
     * Returns an empty list if photoPaths is null, empty, or malformed.
     */
    public List<String> getPhotoPathList() {
        List<String> paths = new ArrayList<>();
        if (photoPaths == null || photoPaths.isEmpty()) {
            return paths;
        }
        try {
            JSONArray array = new JSONArray(photoPaths);
            for (int i = 0; i < array.length(); i++) {
                paths.add(array.getString(i));
            }
        } catch (JSONException e) {
            // Return empty list on parse failure
        }
        return paths;
    }

    public int getMemoryId() { return memoryId; }
    public void setMemoryId(int memoryId) { this.memoryId = memoryId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getPlaceId() { return placeId; }
    public void setPlaceId(int placeId) { this.placeId = placeId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPhotoPaths() { return photoPaths; }
    public void setPhotoPaths(String photoPaths) { this.photoPaths = photoPaths; }

    public boolean isShared() { return isShared; }
    public void setShared(boolean shared) { isShared = shared; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
