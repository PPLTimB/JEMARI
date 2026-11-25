
package com.jemari.auth.dto;

import com.jemari.umkm.BusinessScale;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Password wajib diisi")
    @Size(min = 8, message = "Password minimal 8 karakter")
    private String password;

    @NotBlank(message = "Nama usaha wajib diisi")
    private String businessName;

    @NotBlank(message = "Kategori usaha wajib diisi")
    private String category;

    @NotNull(message = "Skala usaha wajib dipilih")
    private BusinessScale businessScale;

    private String description;
    private String phoneNumber;
    private String contactEmail;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BusinessScale getBusinessScale() { return businessScale; }
    public void setBusinessScale(BusinessScale businessScale) {
        this.businessScale = businessScale;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
}
