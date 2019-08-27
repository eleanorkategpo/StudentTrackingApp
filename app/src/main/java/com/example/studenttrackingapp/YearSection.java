package com.example.studenttrackingapp;

public class YearSection {
    private String schoolId, sectionId, sectionDesc, yearId, yearDesc;

    public YearSection() {

    }

    public YearSection(String schoolId, String sectionId, String sectionDesc, String yearId, String yearDesc) {
        this.schoolId = schoolId;
        this.sectionId = sectionId;
        this.sectionDesc = sectionDesc;
        this.yearId = yearId;
        this.yearDesc = yearDesc;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getSectionDesc() {
        return sectionDesc;
    }

    public String getYearId() {
        return yearId;
    }

    public String getYearDesc() {
        return yearDesc;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public void setSectionDesc(String sectionDesc) {
        this.sectionDesc = sectionDesc;
    }

    public void setYearId(String yearId) {
        this.yearId = yearId;
    }

    public void setYearDesc(String yearDesc) {
        this.yearDesc = yearDesc;
    }
}
