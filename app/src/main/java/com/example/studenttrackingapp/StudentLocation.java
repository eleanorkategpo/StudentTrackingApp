package com.example.studenttrackingapp;

public class StudentLocation {
    private String userId;
    private double latitude, longitude;
    private boolean inSchool;

    public StudentLocation () {

    }

    public StudentLocation(String userId, double latitude, double longitude, boolean inSchool) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.inSchool = inSchool;
    }

    public String getUserId() {
        return userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isInSchool() {
        return inSchool;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setInSchool(boolean inSchool) {
        this.inSchool = inSchool;
    }
}
