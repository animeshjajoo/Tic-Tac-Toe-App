package androidsamples.java.tictactoe;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private FirebaseAuth auth;
    private EditText email;
    private EditText password;
    private CollectionReference userReference;
    private ProgressDialog pd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        userReference = FirebaseFirestore.getInstance().collection("users");

        // if a user is logged in, go to Dashboard
        if (auth.getCurrentUser() != null) {
            Navigation.findNavController(requireView()).navigate(R.id.action_login_successful);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        email = view.findViewById(R.id.edit_email);
        pd = new ProgressDialog(requireContext());
        password = view.findViewById(R.id.edit_password);

        pd.setMessage("Loading...");
        pd.setTitle("Authentication");

        view.findViewById(R.id.btn_log_in).setOnClickListener(v -> login(
                email.getText().toString(),
                password.getText().toString()
        ));

        view.findViewById(R.id.btn_register).setOnClickListener(v -> {
            pd.show();
            if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                login(email.getText().toString(), password.getText().toString());
                                Log.i("User created", task.getResult().getUser().getUid());
                            } else {
                                Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                task.getException().printStackTrace();
                            }
                        } else {
                            NavHostFragment.findNavController(this).navigate(R.id.action_login_successful);
                            try {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email.getText().toString());
                                userData.put("won", 0);
                                userData.put("draw", 0);
                                userData.put("lost", 0);

                                userReference.document(task.getResult().getUser().getUid())
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> Log.d("TICTACTOEAUTH", "SUCCESS"))
                                        .addOnFailureListener(e -> Log.d("TICTACTOEAUTH", e.toString()));
                            } catch (Error e) {
                                Log.d("TICTACTOEAUTH", e.toString());
                            }

                            Toast.makeText(requireContext(), "User Registered", Toast.LENGTH_SHORT).show();
                        }
                        pd.dismiss();
                    });
        });

        return view;
    }

    private void login(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i("LOGIN", "SUCCESS");
                        Log.i("User logged in", task.getResult().getUser().getUid());
                        Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this).navigate(R.id.action_login_successful);
                    } else {
                        Log.i("LOGIN", "FAIL");
                        Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                    pd.dismiss();
                });
    }
}
