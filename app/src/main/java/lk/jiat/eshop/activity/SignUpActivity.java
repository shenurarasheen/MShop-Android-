package lk.jiat.eshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.jiat.eshop.databinding.ActivitySignUpBinding;

import lk.jiat.eshop.model.User;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Enable Firestore SDK debug logging to help diagnose runtime failures
        FirebaseFirestore.setLoggingEnabled(true);

        binding.signupSigninText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.signupBtn.setOnClickListener(view -> {
            String firstName = binding.firstNameInput.getText().toString().trim();
            String lastName = binding.lastNameInput.getText().toString().trim();
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();
            String confirmPassword = binding.confirmPasswordInput.getText().toString().trim();

            if (firstName.isEmpty()) {
                showError(binding.firstNameInput, "First name is required");
                return;
            }

            if (lastName.isEmpty()) {
                showError(binding.lastNameInput, "Last name is required");
                return;
            }

            if (email.isEmpty()) {
                showError(binding.emailInput, "Email is required");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError(binding.emailInput, "Please provide valid email");
                return;
            }

            if (password.isEmpty()) {
                showError(binding.passwordInput, "Password is required");
                return;
            }

            if (password.length() < 6) {
                showError(binding.passwordInput, "Password must be at least 6 characters");
                return;
            }

            if (confirmPassword.isEmpty()) {
                showError(binding.confirmPasswordInput, "Please confirm your password");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError(binding.confirmPasswordInput, "Passwords do not match");
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {

                                String uId = task.getResult().getUser().getUid();

                                Log.d("SignUpActivity", "Created user with uid=" + uId);

                                User user = User.builder()
                                                .uId(uId)
                                                .firstName(firstName)
                                                .lastName(lastName)
                                                .email(email)
                                                .build();

                                firebaseFirestore.collection("users")
                                                .document(uId).set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(getApplicationContext(), "Saved Success", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("SignUpActivity", "Failed to save user to Firestore", e);
                                                        Toast.makeText(SignUpActivity.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                            } else {
                                Exception e = task.getException();
                                String msg = e != null ? e.getMessage() : "Authentication failed";
                                Log.e("SignUpActivity", "Auth failed: " + msg, e);
                                Toast.makeText(SignUpActivity.this, "Authentication failed: " + msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

    }

    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(EditText editText, String msg) {
        editText.setError(msg);
        editText.requestFocus();
    }
}