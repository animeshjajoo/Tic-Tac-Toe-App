package androidsamples.java.tictactoe;

import static java.util.List.of;

import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

public class GameModel extends ViewModel {
    private List<String> gameState;
    private boolean isOpen;
    private String currentHost;
    private String challenger;
    private int turn;
    private String gameId;
    public GameModel(){
        String arr[] = {"", "", "", "", "", "", "", "", ""};
        gameState = Arrays.asList(arr);
    }
    public GameModel(List<String> gameState, boolean isOpen, String currentHost, String challenger, int turn, String gameId) {
        this.gameState = gameState;
        this.isOpen = isOpen;
        this.currentHost = currentHost;
        this.challenger = challenger;
        this.turn = turn;
        this.gameId = gameId;
    }

    public List<String> getGameState() {
        return gameState;
    }

    public void setGameState(List<String> gameState) {
        this.gameState = gameState;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getCurrentHost() {
        return currentHost;
    }

    public void setCurrentHost(String currentHost) {
        this.currentHost = currentHost;
    }

    public String getChallenger() {
        return challenger;
    }

    public void setChallenger(String challenger) {
        this.challenger = challenger;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void updateGameState(GameModel o) {
        gameState = o.getGameState();
        turn = o.getTurn();
    }
}