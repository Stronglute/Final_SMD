package com.example.final_smd;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_smd.utilis.SQLiteHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button   signInButton;
    private TextView signUpTextView;
    private SQLiteHelper dbHelper;
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton       googleButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // ——— SKIP SIGN-IN IF ALREADY LOGGED IN ———
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedInPref = sp.getBoolean("isLoggedIn", false);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (isLoggedInPref || currentUser != null) {
            // user signed in via SQLite OR Google/Firebase
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        emailEditText  = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton   = findViewById(R.id.signInButton);
        signUpTextView = findViewById(R.id.signUpTextView);

        mAuth = FirebaseAuth.getInstance();

        // 2) Configure Google Sign-In to request the user’s ID token
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // 3) Build a GoogleSignInClient with the options
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 4) Hook up the layout’s SignInButton
        googleButton = findViewById(R.id.sign_in_button);
        googleButton.setSize(SignInButton.SIZE_WIDE);
        googleButton.setOnClickListener(v -> signInWithGoogle());
        dbHelper = new SQLiteHelper(this);

        signInButton.setOnClickListener(v -> {
            String email    = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this,
                        "Please enter both email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.validateUser(email, password)) {
                getSharedPreferences("UserPrefs", MODE_PRIVATE);
                sp.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("userEmail", email)
                        .apply();

                Toast.makeText(this, "Sign‑In Successful", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(this, MainActivity.class)); // or Dashboard
                finish();
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        signUpTextView.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });
    }
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acct = task.getResult(ApiException.class);
                Log.d(TAG, "Google sign-in succeeded!");  // <-- add this
                firebaseAuthWithGoogle(acct.getIdToken());
            } catch (ApiException e) {
                Log.e(TAG, "Google sign-in failed, code=" + e.getStatusCode()
                        + " msg=" + e.getMessage(), e);
                Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in success!
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Update SharedPreferences / UI
                        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        sp.edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("userEmail", user.getEmail())
                                .apply();

                        // Navigate to MainActivity
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        // If sign-in fails, display a message to the user.
                        Toast.makeText(this, "Firebase authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
