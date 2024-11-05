package com.example.studentmanagement.models;

public class User {
    private String name;
    private Integer age;
    private String phone;
    private String email;
    private String role;
    private Boolean isLocked;

    public User() {
    }

    public User(String name, Integer age, String phone, String email, String role, Boolean isLocked) {
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.isLocked = isLocked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }
}
