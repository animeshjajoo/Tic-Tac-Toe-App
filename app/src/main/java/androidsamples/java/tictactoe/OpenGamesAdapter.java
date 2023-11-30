package androidsamples.java.tictactoe;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import java.util.ArrayList;

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {

  private final ArrayList<GameModel> list;

  public OpenGamesAdapter(ArrayList<GameModel> list) {
    // FIXME if needed
    this.list = list;
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
    // TODO bind the item at the given position to the holder
    holder.populate(list.get(position).getGameId(), position + 1);

  }

  @Override
  public int getItemCount() {
//    return 0;
//    FIXME
    return list.size();
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

    @SuppressLint("SetTextI18n")
    public void populate(String game, int i) {
      mContentView.setText(game);
      mIdView.setText("#" + i);
      mView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          NavDirections action = DashboardFragmentDirections.actionGame("Two-Player");
          Navigation.findNavController(mView).navigate(action);
        }
      });
    }

  }
}