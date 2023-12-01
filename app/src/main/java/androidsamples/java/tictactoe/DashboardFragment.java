package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment {

  private static final String TAG = "DashboardFragment";
  private NavController NavController;
  private FirebaseAuth auth;
  private DatabaseReference gamesRef, usersRef;
  private TextView wins, losses, draws;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);
    gamesRef = FirebaseDatabase.getInstance("https://tictactoe-ajbbk-default-rtdb.firebaseio.com/").getReference("games");
  }
  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dashboard, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    NavController = Navigation.findNavController(view);

    auth = FirebaseAuth.getInstance();
    if (auth.getCurrentUser() == null) {
      NavDirections action = DashboardFragmentDirections.actionNeedAuth();
      NavController.navigate(action);
    } else {

      wins = view.findViewById(R.id.txt_wins);
      losses = view.findViewById(R.id.txt_losses);
      draws = view.findViewById(R.id.txt_draws);

      usersRef = FirebaseDatabase.getInstance("https://tictactoe-ajbbk-default-rtdb.firebaseio.com/").getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

      List<GameModel> gameIDs = new ArrayList<>();

      RecyclerView recyclerView = view.findViewById(R.id.list);

      gamesRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          gameIDs.clear();
          for (DataSnapshot shot : snapshot.getChildren()) {
            GameModel game = shot.getValue(GameModel.class);
            if (game.isOpen() && !game.getHost().equals(auth.getCurrentUser().getUid())) gameIDs.add(game);
          }
          recyclerView.setAdapter(new OpenGamesAdapter(gameIDs, NavController));
          recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
      });

      usersRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
          wins.setText(snapshot.child("wins").getValue().toString());
          losses.setText(snapshot.child("losses").getValue().toString());
          draws.setText(snapshot.child("draws").getValue().toString());
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
      });

      view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

        DialogInterface.OnClickListener listener = (dialog, which) -> {
          String gameType = "No type";
          String gameID = "";
          if (which == DialogInterface.BUTTON_POSITIVE) {
            gameType = getString(R.string.two_player);
            gameID = gamesRef.push().getKey();
            assert gameID != null;

            gamesRef.child(gameID).setValue(new GameModel(FirebaseAuth.getInstance().getCurrentUser().getUid(), gameID));
            Log.i("FIREBASE", "Value set");
          } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            gameType = getString(R.string.one_player);
          }
          Log.d(TAG, "New Game: " + gameType);

          NavDirections action = (NavDirections) DashboardFragmentDirections.actionGame(gameType, gameID);
          NavController.navigate(action);
        };

        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.new_game)
                .setMessage(R.string.new_game_dialog_message)
                .setPositiveButton(R.string.two_player, listener)
                .setNegativeButton(R.string.one_player, listener)
                .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();
        dialog.show();
      });
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }
}