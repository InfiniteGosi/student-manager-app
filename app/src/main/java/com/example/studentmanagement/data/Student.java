package com.example.studentmanagement.data;

import java.io.Serializable;

public class Student implements Serializable {
    private String fullName;
    private String dateOfBirth;
    private String nationality;
    private String phoneNumber;
    private String email;
    private String studentID;
    private String currentClass;
    private float gpa;
    private String guardianName;
    private String guardianPhone;

    // Constructor không tham số để Firestore có thể khởi tạo đối tượng
    public Student() {}

    // Constructor có tham số
    public Student(String fullName, String dateOfBirth, String nationality, String phoneNumber, String email,
                   String studentID, String currentClass, float gpa, String guardianName, String guardianPhone) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.studentID = studentID;
        this.currentClass = currentClass;
        this.gpa = gpa;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(String currentClass) {
        this.currentClass = currentClass;
    }

    public float getGpa() {
        return gpa;
    }

    public void setGpa(float gpa) {
        this.gpa = gpa;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    // Override toString() for easy display
    @Override
    public String toString() {
        return "Student{" +
                "fullName='" + fullName + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", nationality='" + nationality + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", studentID='" + studentID + '\'' +
                ", currentClass='" + currentClass + '\'' +
                ", gpa=" + gpa +
                ", guardianName='" + guardianName + '\'' +
                ", guardianPhone='" + guardianPhone + '\'' +
                '}';
    }
}
