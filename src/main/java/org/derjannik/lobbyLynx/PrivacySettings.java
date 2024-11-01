package org.derjannik.lobbyLynx;

public class PrivacySettings {
    public boolean allowMessages;
    public boolean privateMode;
    public boolean showLastSeen;

    // Constructor to initialize the privacy settings with default values
    public PrivacySettings() {
        this.allowMessages = true; // Default to allowing messages
        this.privateMode = false;  // Default to public mode
        this.showLastSeen = true;  // Default to showing last seen
    }
}