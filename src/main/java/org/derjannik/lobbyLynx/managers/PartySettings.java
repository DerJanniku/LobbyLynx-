package org.derjannik.lobbyLynx.managers;

public class PartySettings {
    private int maxPlayers;
    private int partyDuration; // in minutes
    private boolean allowSpectators;

    public PartySettings(int maxPlayers, int partyDuration, boolean allowSpectators) {
        this.maxPlayers = maxPlayers;
        this.partyDuration = partyDuration;
        this.allowSpectators = allowSpectators;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getPartyDuration() {
        return partyDuration;
    }

    public void setPartyDuration(int partyDuration) {
        this.partyDuration = partyDuration;
    }

    public boolean isAllowSpectators() {
        return allowSpectators;
    }

    public void setAllowSpectators(boolean allowSpectators) {
        this.allowSpectators = allowSpectators;
    }

    public void resetSettings() {
        this.maxPlayers = 10; // default value
        this.partyDuration = 60; // default value in minutes
        this.allowSpectators = false; // default value
    }
}
