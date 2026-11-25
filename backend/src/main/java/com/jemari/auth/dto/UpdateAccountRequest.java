package com.jemari.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class UpdateAccountRequest {

    @NotBlank(message = "Nama usaha wajib diisi")
    private String businessName;

    @NotBlank(message = "Kategori usaha wajib diisi")
    private String category;

    private String description;
    private String phoneNumber;

    @Email(message = "Format email kontak tidak valid")
    private String contactEmail;

    @Pattern(
    regexp = "^$|.{8,}",
    message = "Password minimal 8 karakter"
    )
    private String password;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}