package com.example.review_rating_service.enums;

import lombok.Getter;

@Getter
public enum Membership {
    FREE("FREE"),
    PREMIUM("PREMIUM");

    private final String value;

    Membership(String value) {
        this.value = value;
    }

    public static Membership fromValue(String value) {
        for (Membership membership : Membership.values()) {
            if (membership.getValue().equalsIgnoreCase(value)) {
                return membership;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
