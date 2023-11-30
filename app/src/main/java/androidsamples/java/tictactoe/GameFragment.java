
package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.firestore.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class GameFragment extends Fragment {
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;

  private final Button[] mButtons = new Button[GRID_SIZE];
  private NavController mNavController;

  String gameType = "One-Player", gameID = "", status = "playing";



  // ................................
  private DatabaseReference gameReference, userReference;

  private String myChar = "X", otherChar = "O";
  private boolean myTurn = true, isSinglePlayer = false, gameEnded = false, isHost = true, gameEnd = false;
  String[] gameArray = new String[]{"", "", "", "", "", "", "", "", ""};
  private GameModel game;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    game = new ViewModelProvider(requireActivity()).get(GameModel.class);
    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
    Log.d(TAG, "New game type = " + args.getGameType());
    Log.d(TAG, "New game ID = " + args.getGameID());

    if (args.getGameType().toString().equals("One-Player")) {
      isSinglePlayer = true;
    }
    userReference = FirebaseDatabase.getInstance("https://tictactoe-ajbbk-default-rtdb.firebaseio.com/").getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

    if (!isSinglePlayer) {
      gameReference = FirebaseDatabase.getInstance("https://tictactoe-ajbbk-default-rtdb.firebaseio.com/").getReference("games").child(args.getGameID());
      gameReference.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          game = snapshot.getValue(GameModel.class);
          if (game != null) {
            gameArray = (game.getGameArray()).toArray(new String[9]);
            gameID = game.getGameID();
            gameEnd = game.isGameEnd();
            if (game != null && game.getTurn() == 1) {
              if (game != null && game.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                isHost = true;
                myTurn = true;
                myChar = "X";
                otherChar = "O";
              } else {
                isHost = false;
                myTurn = false;
                myChar = "O";
                otherChar = "X";
              }
            } else {
              if (game != null && !game.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                myTurn = true;
                myChar = "O";
                otherChar = "X";
                isHost = false;
              } else {
                isHost = true;
                myTurn = false;
                myChar = "X";
                otherChar = "O";
              }
            }
          }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
          Log.e("Game setup error", error.getMessage());
        }
      });
    }

    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
//        Log.d(TAG, "Back pressed");
        if( !gameEnded ) {
          AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                  .setTitle(R.string.confirm)
                  .setMessage(R.string.forfeit_game_dialog_message)
                  .setPositiveButton(R.string.yes, (d, which) -> {
                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(DataSnapshot dataSnapshot) {
                        int value = Integer.parseInt(dataSnapshot.child("losses").getValue().toString());
                        value = value + 1;
                        dataSnapshot.getRef().child("losses").setValue(value);

                        for (int i = 0; i < 9; i++) {
                          mButtons[i].setClickable(false);
                        }
                        gameEnded = true;
                        gameEnd = true;

                        if(!isSinglePlayer)
                          updateDB();
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {

                      }
                    });
                    gameEnd = true;
                    gameEnded = true;
                    mNavController.popBackStack();
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
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (!isSinglePlayer) {
      boolean check = false;
      for (String s : gameArray) {
        if (!s.isEmpty()) {
          check = true;
          break;
        }
      }
      if (!check) {
        waitForOtherPlayer();
      }
    }

    Log.d(TAG, "onViewCreated: " + game.getGameArray());

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

    if (savedInstanceState != null) {
      checkGameEnd();
      gameArray = game.getGameArray().toArray(new String[9]);
      for (int i = 0; i < 9; i++) {
        if(!gameArray[i].isEmpty()) {
          mButtons[i].setText(gameArray[i]);
          mButtons[i].setClickable(false);
        }
      }
    }

    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        if( myTurn ) {
          Log.d(TAG, "Button " + finalI + " clicked");
          ((Button) v).setText(myChar);
          gameArray[finalI] = myChar;
          game.setGameArray(Arrays.asList(gameArray));
          v.setClickable(false);
          if (!isSinglePlayer) {
            updateDB();
            myTurn = updateTurn(game.getTurn());
          }
          checkGameEnd();
          if(gameEnded) return;
          myTurn = !myTurn ;

          if ( isSinglePlayer ) {
            Random rand = new Random();

            int x = rand.nextInt(9);
            while (!gameArray[x].isEmpty()) x = rand.nextInt(9);

            gameArray[x] = otherChar;
            mButtons[x].setText(otherChar);
            mButtons[x].setClickable(false);
            myTurn = !myTurn;

            checkGameEnd();
          } else {
            waitForOtherPlayer();
          }

        } else {
          Toast.makeText(getContext(), "Please wait for your turn!", Toast.LENGTH_SHORT).show();
        }
      });
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }
  private void showResultDialog(String message) {
    AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setTitle(message)
            .setMessage("")
            .setPositiveButton(R.string.ok, (d, which) -> {
              d.dismiss();
              if (isSinglePlayer) {
                // Navigate back to the dashboard fragment for single-player mode
                mNavController.navigateUp();
              } else {
                // Navigate back to the list of open games for two-player mode
                mNavController.popBackStack();
              }
            })
            .create();
    dialog.show();
  }
  public void gameUpDialog(String title, String body, String winner, String gameType) {

    Log.d(TAG, "gameUpDialog: lol 1");
    if (gameEnded) return;

    int win;
    if (winner.equals(myChar)) win = 1;
    else if (winner.equals("draw")) win = 0;
    else win = -1;

    switch (win) {
      case 1:
        if(!gameEnded) {
//          gameEnd = true;
          showResultDialog(getString(R.string.congratulations));
          userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              int value = Integer.parseInt(dataSnapshot.child("wins").getValue().toString());
              value = value + 1;
              dataSnapshot.getRef().child("wins").setValue(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
          });
        }
        break;

      case -1:
        if(!gameEnded) {
//          gameEnd = true;
          showResultDialog(getString(R.string.sorry));
          userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              int value = Integer.parseInt(dataSnapshot.child("losses").getValue().toString());
              value = value + 1;
              dataSnapshot.getRef().child("losses").setValue(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
          });
        }
        break;
      case 0:
        if(!gameEnded) {
//          gameEnd = true;
          showResultDialog(getString(R.string.draw));
          userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              int value = Integer.parseInt(dataSnapshot.child("draws").getValue().toString());
              value = value + 1;
              dataSnapshot.getRef().child("draws").setValue(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
          });
        }
        break;
      default:
        Log.i(TAG, "Error game dialog: " + win);
        break;
    }

//    for (int i = 0; i < 9; i++) {
//      mButtons[i].setClickable(false);
//    }
    gameEnded = true;

    if(!isSinglePlayer)
      updateDB();

    Log.d(TAG, "gameUpDialog: kekw");

//    mNavController.popBackStack();

//    AlertDialog dialog = new AlertDialog.Builder(requireActivity())
//            .setTitle(title)
//            .setMessage(body)
//            .setPositiveButton("okay", (d, which) -> {
//              mNavController.popBackStack();
//            })
//            .setCancelable(false)
//            .create();
//    dialog.show();
  }

  public void checkGameEnd() {
    if (gameEnded) return;
    String player = "";
    if ( Objects.equals(gameArray[0], gameArray[1]) && Objects.equals(gameArray[0], gameArray[2]) && !Objects.equals(gameArray[0], "") ) {
      player = gameArray[0];
      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[3], gameArray[4]) && Objects.equals(gameArray[3], gameArray[5]) && !Objects.equals(gameArray[3], "") ) {
      player = gameArray[3];
      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[6], gameArray[7]) && Objects.equals(gameArray[6], gameArray[8]) && !Objects.equals(gameArray[6], "") ) {
      player = gameArray[6];
      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[0], gameArray[3]) && Objects.equals(gameArray[0], gameArray[6]) && !Objects.equals(gameArray[0], "") ) {
      player = gameArray[0];
      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[1], gameArray[4]) && Objects.equals(gameArray[1], gameArray[7]) && !Objects.equals(gameArray[1], "") ) {
      player = gameArray[1];
      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[2], gameArray[5]) && Objects.equals(gameArray[2], gameArray[8]) && !Objects.equals(gameArray[2], "") ) {
      player = gameArray[2];
      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[0], gameArray[4]) && Objects.equals(gameArray[0], gameArray[8]) && !Objects.equals(gameArray[0], "") ) {
      player = gameArray[0];
      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[2], gameArray[4]) && Objects.equals(gameArray[2], gameArray[6]) && !Objects.equals(gameArray[2], "") ) {
      player = gameArray[2];
      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( !Arrays.asList(gameArray).contains("") ) {
      player = "draw";
      gameUpDialog("Draw", "It is a draw", "draw", gameType);
    }
  }


  public int checkGameEnd2() {
    if (gameEnded) return 0;
    String player = "";
    if ( Objects.equals(gameArray[0], gameArray[1]) && Objects.equals(gameArray[0], gameArray[2]) && !Objects.equals(gameArray[0], "") ) {
      player = gameArray[0];
//      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[3], gameArray[4]) && Objects.equals(gameArray[3], gameArray[5]) && !Objects.equals(gameArray[3], "") ) {
      player = gameArray[3];
//      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[6], gameArray[7]) && Objects.equals(gameArray[6], gameArray[8]) && !Objects.equals(gameArray[6], "") ) {
      player = gameArray[6];
//      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[0], gameArray[3]) && Objects.equals(gameArray[0], gameArray[6]) && !Objects.equals(gameArray[0], "") ) {
      player = gameArray[0];
//      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[1], gameArray[4]) && Objects.equals(gameArray[1], gameArray[7]) && !Objects.equals(gameArray[1], "") ) {
      player = gameArray[1];
//      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[2], gameArray[5]) && Objects.equals(gameArray[2], gameArray[8]) && !Objects.equals(gameArray[2], "") ) {
      player = gameArray[2];
//      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[0], gameArray[4]) && Objects.equals(gameArray[0], gameArray[8]) && !Objects.equals(gameArray[0], "") ) {
      player = gameArray[0];
//      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(gameArray[2], gameArray[4]) && Objects.equals(gameArray[2], gameArray[6]) && !Objects.equals(gameArray[2], "") ) {
      player = gameArray[2];
//      gameUpDialog("Game Up", player + " wins!", player, gameType);
    } else if ( !Arrays.asList(gameArray).contains("") ) {
      player = "draw";
//      gameUpDialog("Draw", "It is a draw", "draw", gameType);
    }
    if(player.equals("draw")) return 0;
    if (player.equals(myChar)) return 1;
    else return -1;
  }

  private boolean updateTurn (int turn) {
    return (turn == 1) == isHost;
  }
  private void waitForOtherPlayer() {
    gameReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        GameModel l = snapshot.getValue(GameModel.class);
        if (game != null) {
          game.updateGameArray(l);
          gameID = game.getGameID();
          gameArray = (game.getGameArray()).toArray(new String[9]);
          gameEnd = game.isGameEnd();

          if (gameEnd && !gameEnded) {
            Log.d(TAG, "onDataChange: uwu " + gameEnd);


            NavDirections action = (NavDirections) GameFragmentDirections.actionBack();
            mNavController.navigate(action);
            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                int value = Integer.parseInt(dataSnapshot.child("wins").getValue().toString());
                value = value + 1;
                dataSnapshot.getRef().child("wins").setValue(value);
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });
            Log.d(TAG, "onDataChange: no");
          } else {
            Log.d(TAG, "waitForOtherPlayer: lol 2 " + game.getTurn());
            Log.d(TAG, "waitForOtherPlayer: lol 2 " + game.isOpen());


            updateUI();
            myTurn = updateTurn(game.getTurn());
            checkGameEnd();
          }
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });
  }

  private void updateDB() {
    GameModel gm = new GameModel();
    gameReference.child("gameArray").setValue(Arrays.asList(gameArray));
    gameReference.child("open").setValue(!gameEnded);
    gameReference.child("gameEnd").setValue(gameEnd);
    if (game.getTurn() == 1) {
      game.setTurn(2);
    } else {
      game.setTurn(1);
    }

    gameReference.child("turn").setValue(game.getTurn());
  }

  public void updateUI() {
    for (int i = 0; i < 9; i++) {
      if(!gameArray[i].isEmpty()) {
        mButtons[i].setText(gameArray[i]);
        mButtons[i].setClickable(false);
      }
    }
  }
}
