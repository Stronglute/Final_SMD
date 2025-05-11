package com.example.final_smd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_smd.utilis.SQLiteHelper;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button   signInButton;
    private TextView signUpTextView;
    private SQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailEditText  = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton   = findViewById(R.id.signInButton);
        signUpTextView = findViewById(R.id.signUpTextView);

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
                SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
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
}
