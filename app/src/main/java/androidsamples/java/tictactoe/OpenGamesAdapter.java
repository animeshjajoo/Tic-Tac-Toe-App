package androidsamples.java.tictactoe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {

  private List<GameModel> gameIDs;
  private NavController navController;

  public OpenGamesAdapter(List<GameModel> gameIDs, NavController nav) {
    this.gameIDs = gameIDs;
    this.navController = nav;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    holder.populate(gameIDs.get(position).getGameID(), gameIDs.get(position).getHost(), position + 1, navController);
  }

  @Override
  public int getItemCount() {
    return gameIDs.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.item_number);
      mContentView = view.findViewById(R.id.content);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }

    public void populate (String game, String Host, int i, NavController nav) {
      mContentView.setText(Host+"\n"+game);
      mIdView.setText("#" + i);
      mView.setOnClickListener(v -> {
        NavDirections action = DashboardFragmentDirections.actionGame("Two-Player", game);
        Navigation.findNavController(mView).navigate(action);
      });
    }
  }
}