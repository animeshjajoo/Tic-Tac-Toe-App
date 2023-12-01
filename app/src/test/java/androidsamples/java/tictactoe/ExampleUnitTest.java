package androidsamples.java.tictactoe;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void checkColumnWin1() {
        GameFragment x = new GameFragment();
        String[] check = new String[]{"X", "", "", "X", "", "", "X", "", ""};
        x.grid = check;
        int result  = x.checkGameEnd2();
        assertEquals(result, 1);
    }
    @Test
    public void checkColumnWin2() {
        GameFragment x = new GameFragment();
        String[] check = new String[]{"", "X", "", "", "X", "", "", "X", ""};
        x.grid = check;
        int result = x.checkGameEnd2();
        assertEquals(result, 1);
    }
    @Test
    public void checkColumnLoss() {
        GameFragment x = new GameFragment();
        String[] check = new String[]{"", "", "O", "", "", "O", "", "", "O"};
        x.grid = check;
        int result = x.checkGameEnd2();
        assertEquals(result, -1);
    }
    @Test
    public void checkRowLoss() {
        GameFragment x = new GameFragment();
        String[] check = new String[]{"O", "O", "O", "", "", "", "", "", ""};
        x.grid = check;
        int result = x.checkGameEnd2();
        assertEquals(result, -1);
    }
    @Test
    public void checkRowWin() {
        GameFragment x = new GameFragment();
        String[] check = new String[]{"", "", "", "X", "X", "X", "", "", ""};
        x.grid = check;
        int result = x.checkGameEnd2();
        assertEquals(result, 1);
    }

    @Test
    public void checkDiagonalWin() {
        GameFragment x = new GameFragment();
        String[] check = new String[]{"X", "", "", "", "X", "", "", "", "X"};
        x.grid = check;
        int result = x.checkGameEnd2();
        assertEquals(result, 1);
    }
    @Test
    public void checkDiagonalLoss() {
        GameFragment x = new GameFragment();
        String[] check = new String[]{"", "", "O", "", "O", "", "O", "", ""};
        x.grid = check;
        int result = x.checkGameEnd2();
        assertEquals(result, -1);
    }
    @Test
    public void checkGameContinue() {
        GameFragment x = new GameFragment();
        String[] check = new String[]{"X", "O", "X", "X", "O", "O", "O", "X", ""};
        x.grid = check;
        int result = x.checkGameEnd2();
        assertEquals(result,-1);
    }

}