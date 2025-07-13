package com.example.review_rating_service.enums;

import lombok.Getter;

@Getter
public enum Role {
    FREE("FREE"),
    ADMIN("ADMIN"),
    PREMIUM("PREMIUM");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.getValue().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
