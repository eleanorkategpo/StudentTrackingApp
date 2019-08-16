package com.example.studenttrackingapp;

public class User {
    public String userId, name, gender, birthday, email, password, address, phoneNumber, schoolId;
    public int isActive, userType; //1- Admin, 2- Parent, 3-Student

    public User() {

    }

    public User(String userId, String name, String gender, String birthday, String email, String password, String address, String phoneNumber,  String schoolId, int isActive, int userType) {
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.birthday = birthday;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.schoolId = schoolId;
        this.isActive = isActive;
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public int getIsActive() {
        return isActive;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }
}
