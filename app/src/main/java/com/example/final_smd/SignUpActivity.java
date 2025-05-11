package com.example.final_smd;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;             // ⬅️  NEW
import android.content.SharedPreferences; // ⬅️  NEW
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_smd.utilis.SQLiteHelper;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button   signUpButton;
    private SQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText  = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton   = findViewById(R.id.signUpButton);

        dbHelper = new SQLiteHelper(this);

        TextView signInTextView = findViewById(R.id.signInTextView);
        signInTextView.setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class)));

        signUpButton.setOnClickListener(v -> {
            String email    = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this,
                        "Please enter both email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            long result = dbHelper.insertUser(email, password);

            if (result > 0) {
                /* ------ NEW: save login flag & jump to MainActivity -------- */
                SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                sp.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("userEmail", email)
                        .apply();

                Toast.makeText(this, "Sign‑Up Successful", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                // Optional flags so back‑button can’t return to auth screens:
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();  // close SignUpActivity
            } else {
                Toast.makeText(this,
                        "Error signing up. Try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
