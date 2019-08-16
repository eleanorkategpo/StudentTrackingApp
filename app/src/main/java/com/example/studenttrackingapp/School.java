package com.example.studenttrackingapp;

public class School {
    private String schoolName, schoolAddress, schoolPhone, schoolLat, schoolLong;

    public School(){

    }

    public School(String schoolName, String schoolAddress, String schoolPhone, String schoolLat, String schoolLong) {
        this.schoolName = schoolName;
        this.schoolAddress = schoolAddress;
        this.schoolPhone = schoolPhone;
        this.schoolLat = schoolLat;
        this.schoolLong = schoolLong;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getSchoolAddress() {
        return schoolAddress;
    }

    public String getSchoolPhone() {
        return schoolPhone;
    }

    public String getSchoolLat() {
        return schoolLat;
    }

    public String getSchoolLong() {
        return schoolLong;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public void setSchoolAddress(String schoolAddress) {
        this.schoolAddress = schoolAddress;
    }

    public void setSchoolPhone(String schoolPhone) {
        this.schoolPhone = schoolPhone;
    }

    public void setSchoolLat(String schoolLat) {
        this.schoolLat = schoolLat;
    }

    public void setSchoolLong(String schoolLong) {
        this.schoolLong = schoolLong;
    }
}
