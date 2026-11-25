
package com.jemari.auth.dto;

public class LoginResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String role;

    public LoginResponse(String token, Long userId, String email, String role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
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
}
