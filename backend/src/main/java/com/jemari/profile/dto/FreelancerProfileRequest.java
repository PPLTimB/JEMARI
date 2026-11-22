package com.jemari.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FreelancerProfileRequest {

    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(max = 150, message = "Nama lengkap maksimal 150 karakter")
    private String fullName;

    @NotBlank(message = "Nomor telepon wajib diisi")
    @Size(max = 20, message = "Nomor telepon maksimal 20 karakter")
    private String phoneNumber;

    @Size(max = 2000, message = "Bio maksimal 2000 karakter")
    private String bio;

    @Size(max = 200, message = "Headline maksimal 200 karakter")
    private String headline;

    @Size(max = 100, message = "Nama kota maksimal 100 karakter")
    private String city;

    @Size(max = 100, message = "Nama provinsi maksimal 100 karakter")
    private String province;

    @Size(max = 500, message = "URL avatar maksimal 500 karakter")
    private String avatarUrl;

    public FreelancerProfileRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}