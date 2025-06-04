package com.fitness.fitnessapi.dto;

public class UserProfileResponse {
    private String fullName;
    private String gender;
    private String dateOfBirth;
    private String bio;
    private String country;
    private String city;
    private String zipCode;
    private String imageBase64;

    public UserProfileResponse(String fullName, String gender, String dateOfBirth, String bio, String country, String city, String zipCode, String imageBase64) {
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.bio = bio;
        this.country = country;
        this.city = city;
        this.zipCode = zipCode;
        this.imageBase64 = imageBase64;
    }

    public UserProfileResponse() {

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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    // constructor, getters, and setters
}
