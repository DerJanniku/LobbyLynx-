package org.derjannik.lobbyLynx.managers;

import org.derjannik.lobbyLynx.managers.GameStatus;

public class PartyGame {
    private String gameId;
    private String partyId;
    private String gameType;
    private GameStatus status;

    public PartyGame(String gameId, String partyId, String gameType, int duration) {
        this.gameId = gameId;
        this.partyId = partyId;
        this.gameType = gameType;
        this.status = GameStatus.NOT_STARTED; // Default status
    }

    /**
     * Starts the game and changes its status to In Progress.
     * Throws an IllegalStateException if the game has already been started or finished.
     */
    public void startGame() {
        if (status == GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is already in progress.");
        }
        if (status == GameStatus.FINISHED) {
            throw new IllegalStateException("Game has already finished.");
        }
        this.status = GameStatus.IN_PROGRESS;
        // Logic to start the game
    }

    /**
     * Ends the game and changes its status to Finished.
     * Throws an IllegalStateException if the game has not been started or has already finished.
     */
    public void endGame() {
        if (status == GameStatus.NOT_STARTED) {
            throw new IllegalStateException("Game has not been started yet.");
        }
        if (status == GameStatus.FINISHED) {
            throw new IllegalStateException("Game has already finished.");
        }
        this.status = GameStatus.FINISHED;
        // Logic to end the game
    }

    public GameStatus getGameStatus() {
        return this.status;
    }
}
