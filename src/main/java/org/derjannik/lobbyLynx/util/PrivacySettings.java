package org.derjannik.lobbyLynx.util;

public enum PrivacySettings {
    PUBLIC("Anyone can send you friend requests"),
    FRIENDS_OF_FRIENDS("Only friends of your friends can send requests"),
    PRIVATE("No one can send you friend requests");

    private final String description;

    PrivacySettings(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PrivacySettings fromString(String text) {
        try {
            return valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PUBLIC; // Default value
        }
    }
}
