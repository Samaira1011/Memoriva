package com.example.memoriva.models;

public class Trip {

    private int tripId;
    private int userId;
    private String name;
    private String startDate;
    private String endDate;
    private String destination;
    private String description;
    private String coverPhotoPath;
    private long createdAt;

    public Trip() {
    }

    public Trip(int tripId, int userId, String name, String startDate, String endDate,
                String destination, String description, String coverPhotoPath, long createdAt) {
        this.tripId = tripId;
        this.userId = userId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destination = destination;
        this.description = description;
        this.coverPhotoPath = coverPhotoPath;
        this.createdAt = createdAt;
    }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverPhotoPath() { return coverPhotoPath; }
    public void setCoverPhotoPath(String coverPhotoPath) { this.coverPhotoPath = coverPhotoPath; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
