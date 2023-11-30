package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;



public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_logout) {
      Log.d(TAG, "logout clicked");


      // TODO handle log out
      FirebaseAuth.getInstance().signOut();
      Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
      Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_need_auth);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


}