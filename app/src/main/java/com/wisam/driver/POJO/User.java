package com.wisam.driver.POJO;

import com.google.gson.annotations.SerializedName;

/**
 * Created by islam on 11/18/16.
 */
public class User {

    @SerializedName(value = "fullname")
    private String fullName;

    @SerializedName(value = "email")
    private String email;

    @SerializedName(value = "phone")
    private String phone;

    @SerializedName(value = "gender")
    private String gender;

    private String password;


    public User(String email, String fullName, String gender, String password, String phone) {
        this.email = email;
        this.fullName = fullName;
        this.gender = gender;
        this.password = password;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
