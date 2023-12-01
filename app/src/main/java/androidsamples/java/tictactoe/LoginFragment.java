package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {
    EditText Email, Password;
    private FirebaseAuth AuthFB;
    private FirebaseUser UserFB;
    private DatabaseReference userReference;
    NavController NavController;
    private String TAG = "LoginFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthFB = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance("https://tictactoe-ajbbk-default-rtdb.firebaseio.com/").getReference("users");
        //if a user is logged in, go to Dashboard
        if (AuthFB.getCurrentUser() != null) {
            NavHostFragment.findNavController(this).navigate(R.id.action_login_successful);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        Email = view.findViewById(R.id.edit_email);
        Password = view.findViewById(R.id.edit_password);

        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {

                    String email = Email.getText().toString(), password = Password.getText().toString();
                    NavController = Navigation.findNavController(view);

                    if(email.equals("") || password.equals("")) {
                        Toast.makeText(getActivity(), "Please enter values", Toast.LENGTH_SHORT).show();
                    } else {
                        AuthFB.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "createUserWithEmail:success");
                                        Toast.makeText(getContext(), "User Registered", Toast.LENGTH_SHORT).show();

                                        UserFB = AuthFB.getCurrentUser();
                                        userReference.child(task.getResult().getUser().getUid()).child("wins").setValue(0);
                                        userReference.child(task.getResult().getUser().getUid()).child("losses").setValue(0);
                                        userReference.child(task.getResult().getUser().getUid()).child("draws").setValue(0);
                                        NavHostFragment.findNavController(this).navigate(R.id.action_login_successful);
                                    } else {
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthWeakPasswordException e) {
                                            Toast.makeText(getActivity(),
                                                    "Enter a password of length greater than six characters.",
                                                    Toast.LENGTH_LONG).show();
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            Toast.makeText(getActivity(), "Incorrect credentials.", Toast.LENGTH_LONG)
                                                    .show();
                                        } catch (Exception e) {
                                            AuthFB.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                                                    requireActivity(), task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                                            NavHostFragment.findNavController(this).navigate(R.id.action_login_successful);
                                                        } else {
                                                            Log.w(TAG, "signInWithEmail:failure", task1.getException());
                                                            Toast.makeText(getActivity(), "Login failed.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    }
                                });

                    }
                });

        return view;
    }

    // No options menu in login fragment.
}