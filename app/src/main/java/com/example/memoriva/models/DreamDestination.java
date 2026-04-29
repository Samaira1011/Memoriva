package com.example.memoriva.models;

public class DreamDestination {

    public static final String STATUS_PLANNED = "PLANNED";
    public static final String STATUS_WISHLIST = "WISHLIST";

    private int dreamId;
    private int userId;
    private String placeName;
    private String status;
    private String expectedDate;
    private String coverImagePath;
    private String notes;
    private double budgetEstimate;
    private int sortOrder;
    private long createdAt;

    public DreamDestination() {
    }

    public DreamDestination(int dreamId, int userId, String placeName, String status,
                            String expectedDate, String coverImagePath, String notes,
                            double budgetEstimate, int sortOrder, long createdAt) {
        this.dreamId = dreamId;
        this.userId = userId;
        this.placeName = placeName;
        this.status = status;
        this.expectedDate = expectedDate;
        this.coverImagePath = coverImagePath;
        this.notes = notes;
        this.budgetEstimate = budgetEstimate;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
    }

    public int getDreamId() { return dreamId; }
    public void setDreamId(int dreamId) { this.dreamId = dreamId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExpectedDate() { return expectedDate; }
    public void setExpectedDate(String expectedDate) { this.expectedDate = expectedDate; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getBudgetEstimate() { return budgetEstimate; }
    public void setBudgetEstimate(double budgetEstimate) { this.budgetEstimate = budgetEstimate; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
