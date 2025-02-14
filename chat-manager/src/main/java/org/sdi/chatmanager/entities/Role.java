package org.sdi.chatmanager.entities;

public enum Role {
    USER,
    ADMIN;

    public static Role fromString(String role) {
        return switch (role.toUpperCase()) {
            case "USER" -> USER;
            case "ADMIN" -> ADMIN;
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}
