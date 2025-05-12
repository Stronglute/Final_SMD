package com.example.final_smd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CardView cardTextToImage, cardPromptEnhancement, cardTextToVideo,
            cardVideoConversion, cardVideoInterpolation, cardTextToSpeech, cardScriptGeneration;
    private Button btnTextToImage, btnPromptEnhancement, btnTextToVideo,
            btnVideoConversion, btnVideoInterpolation, btnTextToSpeech, btnScriptGeneration;
    private FloatingActionButton fabCreateNew;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize UI components
        initializeViews();
        setClickListeners();
    }

    private void initializeViews() {
        // CardViews
        cardTextToImage = findViewById(R.id.card_text_to_image);
        cardPromptEnhancement = findViewById(R.id.card_prompt_enhancement);
        cardTextToVideo = findViewById(R.id.card_text_to_video);
        cardVideoConversion = findViewById(R.id.card_video_conversion);
        cardVideoInterpolation = findViewById(R.id.card_video_interpolation);
        cardTextToSpeech = findViewById(R.id.card_text_to_speech);
        cardScriptGeneration = findViewById(R.id.card_script_generation);

        // Buttons
        btnTextToImage = findViewById(R.id.btn_text_to_image);
        btnPromptEnhancement = findViewById(R.id.btn_prompt_enhancement);
        btnTextToVideo = findViewById(R.id.btn_text_to_video);
        btnVideoConversion = findViewById(R.id.btn_video_conversion);
        btnVideoInterpolation = findViewById(R.id.btn_video_interpolation);
        btnTextToSpeech = findViewById(R.id.btn_text_to_speech);
        btnScriptGeneration = findViewById(R.id.btn_script_generation);

        // Floating Action Button
        fabCreateNew = findViewById(R.id.fab_create_new);
    }

    private void setClickListeners() {
        // Set click listeners for CardViews
        cardTextToImage.setOnClickListener(this);
        cardPromptEnhancement.setOnClickListener(this);
        cardTextToVideo.setOnClickListener(this);
        cardVideoConversion.setOnClickListener(this);
        cardVideoInterpolation.setOnClickListener(this);
        cardTextToSpeech.setOnClickListener(this);
        cardScriptGeneration.setOnClickListener(this);

        // Set click listeners for Buttons
        btnTextToImage.setOnClickListener(this);
        btnPromptEnhancement.setOnClickListener(this);
        btnTextToVideo.setOnClickListener(this);
        btnVideoConversion.setOnClickListener(this);
        btnVideoInterpolation.setOnClickListener(this);
        btnTextToSpeech.setOnClickListener(this);
        btnScriptGeneration.setOnClickListener(this);

        // Set click listener for FAB
        fabCreateNew.setOnClickListener(view -> {
            // Show options dialog for creating a new project
            showNewProjectOptions();
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.card_text_to_image || id == R.id.btn_text_to_image) {
            navigateToTextToImage();
        } else if (id == R.id.card_prompt_enhancement || id == R.id.btn_prompt_enhancement) {
            navigateToPromptEnhancement();
        } else if (id == R.id.card_text_to_video || id == R.id.btn_text_to_video) {
            navigateToTextToVideo();
        } else if (id == R.id.card_video_conversion || id == R.id.btn_video_conversion) {
            navigateToVideoConversion();
        } else if (id == R.id.card_video_interpolation || id == R.id.btn_video_interpolation) {
            navigateToVideoInterpolation();
        } else if (id == R.id.card_text_to_speech || id == R.id.btn_text_to_speech) {
            navigateToTextToSpeech();
        } else if (id == R.id.card_script_generation || id == R.id.btn_script_generation) {
            navigateToScriptGeneration();
        }
    }

    private void navigateToTextToImage() {
        Intent intent = new Intent(MainActivity.this, TestToImageActivity.class);
        startActivity(intent);
    }

    private void navigateToPromptEnhancement() {
        Intent intent = new Intent(MainActivity.this, PromptEnhancementActivity.class);
        startActivity(intent);
    }

    private void navigateToTextToVideo() {
        Intent intent = new Intent(MainActivity.this, VideoGenerationActivity.class);
        startActivity(intent);
    }

    private void navigateToVideoConversion() {
        Intent intent = new Intent(MainActivity.this, VideoExportActivity.class);
        startActivity(intent);
    }

    private void navigateToVideoInterpolation() {
        Intent intent = new Intent(MainActivity.this, VideoEditingActivity.class);
        startActivity(intent);
    }

    private void navigateToTextToSpeech() {
        Intent intent = new Intent(MainActivity.this, TextToSpeechActivity.class);
        startActivity(intent);
    }

    private void navigateToScriptGeneration() {
        Intent intent = new Intent(MainActivity.this, ScriptCaptionActivity.class);
        startActivity(intent);
    }

    private void showNewProjectOptions() {
        // 1) Firebase sign-out
        FirebaseAuth.getInstance().signOut();
        // 2) Google sign-out (now mGoogleSignInClient is non-null)
        mGoogleSignInClient.signOut();

        // 3) Clear preferences & return to sign-in
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}