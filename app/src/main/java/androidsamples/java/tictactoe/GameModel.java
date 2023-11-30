package androidsamples.java.tictactoe;

import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

public class GameModel extends ViewModel {
    private String host, gameID;
    private int turn;
    private boolean isOpen;

    public boolean isGameEnd() {
        return gameEnd;
    }

    public void setGameEnd(boolean gameEnd) {
        this.gameEnd = gameEnd;
    }

    private boolean gameEnd;
    private List<String> gameArray = null;
    public GameModel() {}
    public GameModel(String host, String id) {
        this.host = host;
        isOpen = true;
        gameEnd = false;
        gameArray = Arrays.asList("", "", "", "", "", "", "", "", "");
        this.gameID = id;
        turn = 1;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public List<String> getGameArray() {
        return gameArray;
    }

    public void setGameArray(List<String> gameArray) {
        this.gameArray = gameArray;
    }

    public void updateGameArray(GameModel o) {
        gameArray = o.gameArray;
        turn = o.turn;
        gameEnd = o.gameEnd;
    }

}
