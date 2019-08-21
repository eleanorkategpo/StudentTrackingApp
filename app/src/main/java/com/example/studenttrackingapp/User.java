package com.example.studenttrackingapp;

public class User {
    public String userId, name, gender, birthday, email,  address, phoneNumber, schoolId, year, section, childId;
    public boolean isActive, isSchoolAdmin, isSuperAdmin;
    public int userType; //1- Admin, 2- Parent, 3-Student

    public User() {

    }

    public User(String userId, String name, String gender, String birthday, String email, String address, String phoneNumber,  String schoolId, String year, String section, String childId, boolean isActive, int userType, boolean isSchoolAdmin, boolean isSuperAdmin) {
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.birthday = birthday;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.schoolId = schoolId;
        this.year = year;
        this.section = section;
        this.childId = childId;
        this.isActive = isActive;
        this.userType = userType;
        this.isSchoolAdmin = isSchoolAdmin;
        this.isSuperAdmin = isSuperAdmin;
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

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public String getYear() {
        return year;
    }

    public String getSection() {
        return section;
    }

    public String getChildId() {
        return childId;
    }

    public int getUserType() {
        return userType;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isSchoolAdmin() {
        return isSchoolAdmin;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
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

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setSchoolAdmin(boolean schoolAdmin) {
        isSchoolAdmin = schoolAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }
}
