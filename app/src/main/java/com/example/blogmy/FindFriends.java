package com.example.blogmy;

public class FindFriends {
    public String profileimage, fullname, status, country, dob, gender, relationship, username;

    public FindFriends(){

    }

    public FindFriends(String profileimage, String fullname, String status, String country, String dob, String gender, String relationship, String username) {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.status = status;
        this.country = country;
        this.dob = dob;
        this.gender = gender;
        this.relationship = relationship;
        this.username = username;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
