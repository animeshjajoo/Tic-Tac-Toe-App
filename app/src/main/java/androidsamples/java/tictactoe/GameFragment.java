
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
  private final Button[] Buttons = new Button[GRID_SIZE];
  private NavController NavController;
  String gameType = "Single Player", gameID = "", status = "playing";
  private DatabaseReference gameReference, userReference;
  private String myMove = "X", otherMove = "O";
  private boolean myTurn = true, isSinglePlayer = false, gameEnded = false, isHost = true, gameEnd = false;
  String[] grid = new String[]{"", "", "", "", "", "", "", "", ""};
  private GameModel game;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    game = new ViewModelProvider(requireActivity()).get(GameModel.class);
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());

    if (args.getGameType().toString().equals("Single Player")) {
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
            grid = (game.getGrid()).toArray(new String[9]);
            gameID = game.getGameID();
            gameEnd = game.isGameEnd();
            if (game != null && game.getTurn() == 1) {
              if (game != null && game.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                isHost = true;
                myTurn = true;
                myMove = "X";
                otherMove = "O";
              } else {
                isHost = false;
                myTurn = false;
                myMove = "O";
                otherMove = "X";
              }
            } else {
              if (game != null && !game.getHost().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                myTurn = true;
                myMove = "O";
                otherMove = "X";
                isHost = false;
              } else {
                isHost = true;
                myTurn = false;
                myMove = "X";
                otherMove = "O";
              }
            }
          }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
      });
    }
    
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
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
                          Buttons[i].setClickable(false);
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
                    NavController.popBackStack();
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
      for (String s : grid) {
        if (!s.isEmpty()) {
          check = true;
          break;
        }
      }
      if (!check) {
        waitForOtherPlayer();
      }
    }

    NavController = Navigation.findNavController(view);

    Buttons[0] = view.findViewById(R.id.button0);
    Buttons[1] = view.findViewById(R.id.button1);
    Buttons[2] = view.findViewById(R.id.button2);
    Buttons[3] = view.findViewById(R.id.button3);
    Buttons[4] = view.findViewById(R.id.button4);
    Buttons[5] = view.findViewById(R.id.button5);
    Buttons[6] = view.findViewById(R.id.button6);
    Buttons[7] = view.findViewById(R.id.button7);
    Buttons[8] = view.findViewById(R.id.button8);

    if (savedInstanceState != null) {
      checkGameEnd();
      grid = game.getGrid().toArray(new String[9]);
      for (int i = 0; i < 9; i++) {
        if(!grid[i].isEmpty()) {
          Buttons[i].setText(grid[i]);
          Buttons[i].setClickable(false);
        }
      }
    }

    for (int i = 0; i < Buttons.length; i++) {
      int finalI = i;
      Buttons[i].setOnClickListener(v -> {
        if( myTurn ) {
          ((Button) v).setText(myMove);
          grid[finalI] = myMove;
          game.setGrid(Arrays.asList(grid));
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
            while (!grid[x].isEmpty()) x = rand.nextInt(9);
            grid[x] = otherMove;
            Buttons[x].setText(otherMove);
            Buttons[x].setClickable(false);
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
  public void gameDialog(String title, String body, String winner, String gameType) {

    Log.d(TAG, "gameDialog: lol 1");
    if (gameEnded) return;

    int win;
    if (winner.equals(myMove)) win = 1;
    else if (winner.equals("draw")) win = 0;
    else win = -1;

    switch (win) {
      case 1:
        if(!gameEnded) {
//          gameEnd = true;
          userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              int value = Integer.parseInt(dataSnapshot.child("wins").getValue().toString());
              value = value + 1;
              dataSnapshot.getRef().child("wins").setValue(value);
              Toast.makeText(requireContext(), "Congratulations, You Won", Toast.LENGTH_SHORT).show();
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
          userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              int value = Integer.parseInt(dataSnapshot.child("losses").getValue().toString());
              value = value + 1;
              dataSnapshot.getRef().child("losses").setValue(value);
              Toast.makeText(requireContext(), "Sorry, You Lost :(", Toast.LENGTH_SHORT).show();
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
          userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              int value = Integer.parseInt(dataSnapshot.child("draws").getValue().toString());
              value = value + 1;
              dataSnapshot.getRef().child("draws").setValue(value);
              Toast.makeText(requireContext(), "You Drew", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
          });
        }
        break;
      default:
        break;
    }

    gameEnded = true;

    if(!isSinglePlayer)
      updateDB();
  }

  public void checkGameEnd() {
    if (gameEnded) return;
    String player = "";
    if ( Objects.equals(grid[0], grid[1]) && Objects.equals(grid[0], grid[2]) && !Objects.equals(grid[0], "") ) {
      player = grid[0];
      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[3], grid[4]) && Objects.equals(grid[3], grid[5]) && !Objects.equals(grid[3], "") ) {
      player = grid[3];
      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[6], grid[7]) && Objects.equals(grid[6], grid[8]) && !Objects.equals(grid[6], "") ) {
      player = grid[6];
      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[0], grid[3]) && Objects.equals(grid[0], grid[6]) && !Objects.equals(grid[0], "") ) {
      player = grid[0];
      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[1], grid[4]) && Objects.equals(grid[1], grid[7]) && !Objects.equals(grid[1], "") ) {
      player = grid[1];
      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[2], grid[5]) && Objects.equals(grid[2], grid[8]) && !Objects.equals(grid[2], "") ) {
      player = grid[2];
      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[0], grid[4]) && Objects.equals(grid[0], grid[8]) && !Objects.equals(grid[0], "") ) {
      player = grid[0];
      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[2], grid[4]) && Objects.equals(grid[2], grid[6]) && !Objects.equals(grid[2], "") ) {
      player = grid[2];
      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( !Arrays.asList(grid).contains("") ) {
      player = "draw";
      gameDialog("Draw", "It is a draw", "draw", gameType);
    }
  }


  public int checkGameEnd2() {
    if (gameEnded) return 0;
    String player = "";
    if ( Objects.equals(grid[0], grid[1]) && Objects.equals(grid[0], grid[2]) && !Objects.equals(grid[0], "") ) {
      player = grid[0];
//      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[3], grid[4]) && Objects.equals(grid[3], grid[5]) && !Objects.equals(grid[3], "") ) {
      player = grid[3];
//      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[6], grid[7]) && Objects.equals(grid[6], grid[8]) && !Objects.equals(grid[6], "") ) {
      player = grid[6];
//      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[0], grid[3]) && Objects.equals(grid[0], grid[6]) && !Objects.equals(grid[0], "") ) {
      player = grid[0];
//      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[1], grid[4]) && Objects.equals(grid[1], grid[7]) && !Objects.equals(grid[1], "") ) {
      player = grid[1];
//      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[2], grid[5]) && Objects.equals(grid[2], grid[8]) && !Objects.equals(grid[2], "") ) {
      player = grid[2];
//      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[0], grid[4]) && Objects.equals(grid[0], grid[8]) && !Objects.equals(grid[0], "") ) {
      player = grid[0];
//      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( Objects.equals(grid[2], grid[4]) && Objects.equals(grid[2], grid[6]) && !Objects.equals(grid[2], "") ) {
      player = grid[2];
//      gameDialog("Game Up", player + " wins!", player, gameType);
    } else if ( !Arrays.asList(grid).contains("") ) {
      player = "draw";
      gameDialog("Draw", "It is a draw", "draw", gameType);
    }
    if(player.equals("draw")) return 0;
    if (player.equals(myMove)) return 1;
    else return -1;
  }

  private boolean updateTurn (int turn) {
    return (turn == 1) == isHost;
  }
  private void waitForOtherPlayer() {
    gameReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        GameModel gamemodel = snapshot.getValue(GameModel.class);
        if (game != null) {
          game.updateGrid(gamemodel);
          gameID = game.getGameID();
          grid = (game.getGrid()).toArray(new String[9]);
          gameEnd = game.isGameEnd();

          if (gameEnd && !gameEnded) {
            NavDirections action = (NavDirections) GameFragmentDirections.actionBack();
            NavController.navigate(action);
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
          } else {
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
    gameReference.child("grid").setValue(Arrays.asList(grid));
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
      if(!grid[i].isEmpty()) {
        Buttons[i].setText(grid[i]);
        Buttons[i].setClickable(false);
      }
    }
  }
}
