package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

  private static final String TAG = "DashboardFragment";
  private NavController mNavController;

  private FirebaseAuth auth;
  private CollectionReference gameReference;
  private CollectionReference userReference;
  private RecyclerView recyclerView;
  private TextView won;
  private TextView lost;
  private TextView draw;
  private TextView info;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setHasOptionsMenu(true); // Needed to display the action menu for this fragment

    gameReference = FirebaseFirestore.getInstance().collection("games");
    userReference = FirebaseFirestore.getInstance().collection("users");
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
    mNavController = Navigation.findNavController(view);

    // TODO if a user is not logged in, go to LoginFragment

    recyclerView = view.findViewById(R.id.list);
    won = view.findViewById(R.id.won_score);
    lost = view.findViewById(R.id.lost_score);
    draw = view.findViewById(R.id.draw_score);
    info = view.findViewById(R.id.open_display);

    auth = FirebaseAuth.getInstance();
    if (auth.getCurrentUser() == null) {
      mNavController.navigate(R.id.action_need_auth);
      return;
    }

    List<GameModel> gameList = new ArrayList<>();
    gameReference.addSnapshotListener((value, error) -> {
      gameList.clear();
      if (value != null) {
        for (DocumentSnapshot shot : value.getDocuments()) {
          if (shot.getData() != null) {
            Log.d(TAG, "onViewCreated: " + shot.getData());
            if (shot.getData().get("challenger") == null ||
                    shot.getData().get("challenger").equals(auth.getCurrentUser().getUid()) ||
                    shot.getData().get("currentHost").equals(auth.getCurrentUser().getUid())) {

              GameModel game = new GameModel(
                      (List<String>) shot.getData().get("gameState"),
                      (Boolean) shot.getData().get("open"),
                      (String) shot.getData().get("currentHost"),
                      (String) shot.getData().get("challenger"),
                      ((Long) shot.getData().get("turn")).intValue(),
                      (String) shot.getData().get("gameId")
              );

              if (game.isOpen()) {
                gameList.add(game);
              }
              Log.d(TAG, "onViewCreated: " + game);
            }
          }
        }
      }

      recyclerView.setAdapter(new OpenGamesAdapter((ArrayList<GameModel>) gameList));
      recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
      info.setText(gameList.isEmpty() ? "No Open Games Available :(" : "Open Games");
    });

    userReference.document(auth.getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
      won.setText(value != null ? value.get("won").toString() : "");
      lost.setText(value != null ? value.get("lost").toString() : "");
      draw.setText(value != null ? value.get("draw").toString() : "");
    });

    // Show a dialog when the user clicks the "new game" button

    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        String gameType;
        String gameId = "";
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = getString(R.string.two_player);
          gameReference.add(
                  new GameModel(
                          null,
                          true,
                          auth.getCurrentUser().getUid(),
                          "",
                          1,
                          ""
                  )
          ).addOnSuccessListener(documentReference -> {
            final String finalGameId = gameId;
            gameReference.document(finalGameId).update("gameId", finalGameId);
            NavDirections action = DashboardFragmentDirections.actionGame(gameType);
            mNavController.navigate(action);
          });
          Log.i("FIREBASE", "Value set");
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = getString(R.string.one_player);
          NavDirections action = DashboardFragmentDirections.actionGame(gameType);
          mNavController.navigate(action);
        } else {
          gameType = "No type";
        }
        Log.d(TAG, "New Game: " + gameType);
      };

      // create the dialog
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

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_logout) {
      FirebaseAuth.getInstance().signOut();
      Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
      Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
              .navigate(R.id.action_need_auth);
      return true;
    }
//    if (item.getItemId() == R.id.menu_deregister) {
//      auth.getCurrentUser().delete().addOnCompleteListener(task -> {
//        Toast.makeText(requireContext(), "Deleted account", Toast.LENGTH_SHORT).show();
//        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
//                .navigate(R.id.action_need_auth);
//      });
//    }
    return super.onOptionsItemSelected(item);
  }

}