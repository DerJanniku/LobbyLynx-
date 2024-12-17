package org.derjannik.lobbyLynx.managers;

public class PartyStats {
    private int wins;
    private int losses;

    public PartyStats() {
        this.wins = 0;
        this.losses = 0;
    }

    public int getWins() {
        return wins;
    }

    public void incrementWins() {
        this.wins++;
    }

    public int getLosses() {
        return losses;
    }

    public void incrementLosses() {
        this.losses++;
    }
}
