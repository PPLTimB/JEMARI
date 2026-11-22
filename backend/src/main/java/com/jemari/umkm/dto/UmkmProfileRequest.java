package com.jemari.umkm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UmkmProfileRequest {

    @NotBlank(message = "Nama usaha wajib diisi")
    @Size(max = 150, message = "Nama usaha maksimal 150 karakter")
    private String businessName;

    @Size(max = 2000, message = "Deskripsi maksimal 2000 karakter")
    private String description;

    @NotBlank(message = "Kategori usaha wajib diisi")
    @Size(max = 100, message = "Kategori usaha maksimal 100 karakter")
    private String category;

    @Size(max = 20, message = "Nomor telepon maksimal 20 karakter")
    private String phoneNumber;

    @Email(message = "Format email kontak tidak valid")
    @Size(max = 255, message = "Email kontak maksimal 255 karakter")
    private String contactEmail;

    public UmkmProfileRequest() {
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
}
