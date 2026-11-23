
package com.jemari.auth.dto;

import com.jemari.umkm.BusinessScale;

public class AccountProfileResponse {

    private Long userId;
    private String email;
    private String role;
    private String businessName;
    private String category;
    private BusinessScale businessScale;
    private String description;
    private String phoneNumber;
    private String contactEmail;

    public AccountProfileResponse(
            Long userId,
            String email,
            String role,
            String businessName,
            String category,
            BusinessScale businessScale,
            String description,
            String phoneNumber,
            String contactEmail) {

        this.userId = userId;
        this.email = email;
        this.role = role;
        this.businessName = businessName;
        this.category = category;
        this.businessScale = businessScale;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.contactEmail = contactEmail;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getCategory() {
        return category;
    }

    public BusinessScale getBusinessScale() {
        return businessScale;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }
}
