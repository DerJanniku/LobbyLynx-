package org.derjannik.lobbyLynx.enums;

public enum PrivacyLevel {
    PUBLIC("Public", "Everyone can join"),
    FRIENDS("Friends Only", "Only friends can join"),
    PRIVATE("Private", "Invitation only");

    private final String displayName;
    private final String description;

    PrivacyLevel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}