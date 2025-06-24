package com.fitness.fitnessapi.dto;

import java.time.LocalDate;

public class AvailablePartnerDTO {
    private Long userId;
    private String fullName;
    private String email;
    private String imageUrl; // ✅ added
    private String bio;
    private String gender;
    private LocalDate dob;
    private String city;
    private String zipCode;
    private String country;

    public AvailablePartnerDTO(Long userId, String fullName, String email, String imageUrl, String bio, String gender,
                               LocalDate dob, String city, String zipCode, String country) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.imageUrl = imageUrl;
        this.bio = bio;
        this.gender = gender;
        this.dob = dob;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // Getters and setters (or @Data if you're using Lombok)
}
