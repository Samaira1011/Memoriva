package com.example.memoriva.models;

public class Place {

    private int placeId;
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private String country;
    private long createdAt;

    public Place() {
    }

    public Place(int placeId, String name, double latitude, double longitude,
                 String address, String city, String country, long createdAt) {
        this.placeId = placeId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
        this.country = country;
        this.createdAt = createdAt;
    }

    public int getPlaceId() { return placeId; }
    public void setPlaceId(int placeId) { this.placeId = placeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
