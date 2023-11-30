package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class GameFragment extends Fragment {

  private Button[] mButtons = new Button[GRID_SIZE];
  private NavController mNavController;
  private TextView display;
  private boolean isSinglePlayer = true;
  private String myChar = "X";
  private String otherChar = "O";
  private boolean myTurn = true;
  private String[] gameState = {"", "", "", "", "", "", "", "", ""};
  private boolean gameEnded = false;
  private GameModel game;
  private boolean isHost = true;
  private CollectionReference gameReference;
  private CollectionReference userReference;

  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(requireArguments());
    Log.d(TAG, "New game type = " + args.getGameType());
    isSinglePlayer = (args.getGameType().equals("One-Player"));

    gameReference = FirebaseFirestore.getInstance().collection("games");
    userReference = FirebaseFirestore.getInstance().collection("users");

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");

        if (!gameEnded) {
          AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                  .setTitle(R.string.confirm)
                  .setMessage(R.string.forfeit_game_dialog_message)
                  .setPositiveButton(R.string.yes, (dialog1, which) -> {
                    endGame(-1);
                    mNavController.popBackStack();
                    if (!isSinglePlayer) {
                      gameReference.document(game.getGameId())
                              .update("forfeited", FirebaseAuth.getInstance().getCurrentUser().getUid());
                      gameReference.document(game.getGameId())
                              .update("open", false);
                    }
                  })
                  .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                  .create();
          dialog.show();
        } else {
          assert getParentFragment() != null;
          NavHostFragment.findNavController(getParentFragment()).navigateUp();
        }
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    display = view.findViewById(R.id.display_tv);

    if (!isSinglePlayer) {
      gameReference.document(game.getGameId())
              .addSnapshotListener((value, error) -> {
                game = new GameModel(
                        (java.util.List<String>) value.get("gameState"),
                        (boolean) value.get("open"),
                        (String) value.get("currentHost"),
                        (String) value.get("challenger"),
                        ((Long) value.get("turn")).intValue(),
                        (String) value.get("gameId")
                );
                Log.d(TAG, "onViewCreated: " + value.getData().containsKey("forfeited"));
                if (value.getData().get("forfeited").equals(value.getData().get("challenger")) && !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(value.getData().get("forfeited"))) {
                  endGame(1);
                  mNavController.popBackStack();
                  Toast.makeText(getContext(), "Other player forfeited, you win", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "Game ID: " + game.getGameId());
                Log.d(TAG, "Game State: " + game.getGameState());
                gameState = game.getGameState().toArray(new String[0]);
                String hostMail = getString(R.string.host);
                String challengerMail = getString(R.string.challenger);

                TextView gameIdTextView = view.findViewById(R.id.display_game_id);
                gameIdTextView.setText("Game ID: " + game.getGameId());

                TextView hostTextView = view.findViewById(R.id.display_host);
                userReference.document(value.get("currentHost").toString()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                          hostTextView.setText("Host: " + hostMail);
                        });

                if (!value.getData().get("challenger").equals("") && value.getData().get("challenger") != null) {
                  TextView challengerTextView = view.findViewById(R.id.display_challenger);
                  userReference.document(value.getData().get("challenger").toString()).get()
                          .addOnSuccessListener(documentSnapshot -> {
                            challengerTextView.setText("Challenger: " + challengerMail);
                          });
                }

                if (game.getTurn() == 1) {
                  if (game.getCurrentHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    isHost = true;
                    myTurn = true;
                    myChar = "X";
                    display.setText(R.string.your_turn);
                    otherChar = "O";
                  } else {
                    isHost = false;
                    myTurn = false;
                    myChar = "O";
                    otherChar = "X";
                    display.setText(R.string.their_turn);
                    gameReference.document(game.getGameId())
                            .update("challenger", FirebaseAuth.getInstance().getCurrentUser().getUid());
                  }
                } else {
                  if (!game.getCurrentHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    myTurn = true;
                    myChar = "O";
                    otherChar = "X";
                    isHost = false;
                    display.setText(R.string.your_turn);
                    gameReference.document(game.getGameId())
                            .update("challenger", FirebaseAuth.getInstance().getCurrentUser().getUid());
                  } else {
                    isHost = true;
                    myTurn = false;
                    myChar = "X";
                    otherChar = "O";
                    display.setText(R.string.their_turn);
                  }
                }
                updateUI();
              });
    }

    mNavController = Navigation.findNavController(view);
    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);
    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);
    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);
    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        if (myTurn) {
          Log.d(TAG, "Button " + finalI + " clicked");
          ((Button) v).setText(myChar);
          gameState[finalI] = myChar;
          v.setClickable(false);
          display.setText(R.string.waiting);
          if (!isSinglePlayer) {
            updateDB();
            myTurn = updateTurn(game.getTurn());
          }
          int win = checkWin();
          if (win == 1 || win == -1) {
            endGame(win);
            return;
          } else if (checkDraw()) {
            endGame(0);
            return;
          }
          myTurn = !myTurn;
          if (isSinglePlayer) {
            computerPlayerMove();
          } else {
            waitForOtherPlayer();
          }
        } else {
          Toast.makeText(getContext(), "Please wait for your turn!", Toast.LENGTH_SHORT).show();
        }
      });
    }
  }

  private boolean checkDraw() {
    if (checkWin() != 0) return false;
    Log.i("CHECKING WIN IN DRAW", "Complete: " + checkWin());
    for (String s : gameState) {
      if (s.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private void endGame(int win) {
    switch (win) {
      case 1:
        display.setText(R.string.you_win);
        if (!gameEnded) {
          final DocumentSnapshot[] userData = new DocumentSnapshot[1];
          userReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                  .get().addOnSuccessListener(documentSnapshot -> {
                    userData[0] = documentSnapshot;
                    int won = ((Long) userData[0].get("won")).intValue();
                    won += 1;
                    userReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .update("won", won);
                  });
        }
        Toast.makeText(getContext(), "YOU WIN!!!!", Toast.LENGTH_SHORT).show();
        break;
      case -1:
        display.setText(R.string.you_lose);
        if (!gameEnded) {
          final DocumentSnapshot[] userData = new DocumentSnapshot[1];
          userReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                  .get().addOnSuccessListener(documentSnapshot -> {
                    userData[0] = documentSnapshot;
                    int lost = ((Long) userData[0].get("lost")).intValue();
                    lost += 1;
                    userReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .update("lost", lost);
                  });
        }
        Toast.makeText(getContext(), "You Lose.... :(", Toast.LENGTH_SHORT).show();
        break;
      case 0:
        display.setText(R.string.draw);
        if (!gameEnded) {
          final DocumentSnapshot[] userData = new DocumentSnapshot[1];
          userReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                  .get().addOnSuccessListener(documentSnapshot -> {
                    userData[0] = documentSnapshot;
                    int draw = ((Long) userData[0].get("draw")).intValue();
                    draw += 1;
                    userReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .update("draw", draw);
                  });
        }
        Toast.makeText(getContext(), "Match Draw.", Toast.LENGTH_SHORT).show();
        break;
      default:
        display.setText(R.string.error);
        Log.i("CHECKING DRAW", "Error: " + win);
    }
    for (Button mButton : mButtons) {
      mButton.setClickable(false);
    }
    gameEnded = true;
    if (!isSinglePlayer) gameReference.document(game.getGameId()).update("open", !gameEnded);
  }

  private void waitForOtherPlayer() {
    gameReference.document(game.getGameId()).addSnapshotListener((value, error) -> {
      GameModel l = new GameModel(
              (java.util.List<String>) value.get("gameState"),
              (boolean) value.get("open"),
              (String) value.get("currentHost"),
              (String) value.get("challenger"),
              ((Long) value.get("turn")).intValue(),
              (String) value.get("gameId")
      );
      game.updateGameState(l);
      gameState = game.getGameState().toArray(new String[0]);
      updateUI();
      myTurn = updateTurn(game.getTurn());
      int win = checkWin();
      if (win == 1 || win == -1) endGame(win);
      else if (checkDraw()) endGame(0);
    });
  }

  private boolean updateTurn(int turn) {
    return turn == 1 == isHost;
  }

  private void updateUI() {
    for (int i = 0; i < gameState.length; i++) {
      String v = gameState[i];
      if (!v.isEmpty()) {
        mButtons[i].setText(v);
        mButtons[i].setClickable(false);
      }
    }
  }

  private void updateDB() {
    gameReference.document(game.getGameId()).update("gameState", Arrays.asList(gameState));
    gameReference.document(game.getGameId()).update("open", !gameEnded);

    if (game.getTurn() == 1) {
      game.setTurn(2);
    } else {
      game.setTurn(1);
    }
    gameReference.document(game.getGameId()).update("turn", game.getTurn());
  }

  private void computerPlayerMove() {
    Random rand = new Random();
    int x = rand.nextInt(9);
    if (checkDraw()) {
      endGame(0);
      return;
    }
    while (!gameState[x].isEmpty()) x = rand.nextInt(9);
    Log.i("CHECKING CONDITIONS", "Complete");
    gameState[x] = otherChar;
    mButtons[x].setText(otherChar);
    mButtons[x].setClickable(false);
    myTurn = !myTurn;
    display.setText(R.string.your_turn);
    int win = checkWin();
    if (win == 1 || win == -1) endGame(win);
    else if (checkDraw()) endGame(0);
  }

  private int checkWin() {
    String winChar = "";
    if (gameState[0].equals(gameState[1]) && gameState[1].equals(gameState[2]) && !gameState[0].isEmpty()) {
      winChar = gameState[0];
    } else if (gameState[3].equals(gameState[4]) && gameState[4].equals(gameState[5]) && !gameState[3].isEmpty()) {
      winChar = gameState[3];
    } else if (gameState[6].equals(gameState[7]) && gameState[7].equals(gameState[8]) && !gameState[6].isEmpty()) {
      winChar = gameState[6];
    } else if (gameState[0].equals(gameState[3]) && gameState[3].equals(gameState[6]) && !gameState[0].isEmpty()) {
      winChar = gameState[0];
  } else if (gameState[1].equals(gameState[4]) && gameState[4].equals(gameState[7]) && !gameState[1].isEmpty()) {
    winChar = gameState[1];
  } else if (gameState[2].equals(gameState[5]) && gameState[5].equals(gameState[8]) && !gameState[2].isEmpty()) {
    winChar = gameState[2];
  } else if (gameState[0].equals(gameState[4]) && gameState[4].equals(gameState[8]) && !gameState[0].isEmpty()) {
    winChar = gameState[0];
  } else if (gameState[6].equals(gameState[4]) && gameState[4].equals(gameState[2]) && !gameState[2].isEmpty()) {
    winChar = gameState[2];
  } else {
    return 0;
  }

        return winChar.equals(myChar) ? 1 : -1;
}
}
