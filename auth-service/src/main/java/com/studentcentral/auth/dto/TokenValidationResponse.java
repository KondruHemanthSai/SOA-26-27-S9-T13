package com.studentcentral.auth.dto;

import com.studentcentral.auth.model.Role;

public class TokenValidationResponse {

    private boolean valid;
    private String userId;
    private String email;
    private Role role;
    private String message;

    public TokenValidationResponse() {
    }

    public TokenValidationResponse(boolean valid, String userId, String email, Role role, String message) {
        this.valid = valid;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public static TokenValidationResponse valid(String userId, String email, Role role) {
        return new TokenValidationResponse(true, userId, email, role, "Token is valid");
    }

    public static TokenValidationResponse invalid(String message) {
        return new TokenValidationResponse(false, null, null, null, message);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
