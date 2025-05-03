package com.example.final_smd;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PromptEnhancementActivity extends AppCompatActivity {

    private TextInputEditText editTextOriginalPrompt;
    private TextInputEditText editTextEnhancedPrompt;
    private RadioGroup radioGroupEnhancementType;
    private RadioButton radioEnhanceDetail;
    private RadioButton radioEnhanceStyle;
    private RadioButton radioEnhanceTechnical;
    private Button btnEnhance;
    private Button btnRefineFurther;
    private Button btnUsePrompt;
    private MaterialCardView cardEnhancedPrompt;
    private MaterialCardView cardEnhancementExplanation;
    private TextView textEnhancementExplanation;
    private ProgressBar progressEnhancement;

    // Enhancement types and their explanations
    private final Map<String, String> enhancementExplanations = new HashMap<>();

    // Executor for background tasks
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt_enhancement);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize explanations
        initExplanations();

        // Initialize UI components
        initializeViews();
        setupButtonListeners();

        // Check if we have a prompt from another activity
        checkForIncomingPrompt();
    }

    private void initExplanations() {
        enhancementExplanations.put("detail", "Added specific descriptive elements to make the " +
                "output more precise and visually rich. Enhanced with details about lighting, " +
                "perspective, and environmental context.");

        enhancementExplanations.put("style", "Added stylistic elements to give the output a " +
                "distinctive artistic quality. Incorporated terms that influence the aesthetic " +
                "direction like art movement references, color palette suggestions, and mood indicators.");

        enhancementExplanations.put("technical", "Added technical parameters that help the AI model " +
                "produce higher quality results. These include resolution specifications, aspect " +
                "ratio guidance, and quality enhancers like 'detailed,' 'high resolution,' and 'photorealistic'.");
    }

    private void initializeViews() {
        editTextOriginalPrompt = findViewById(R.id.edit_text_original_prompt);
        editTextEnhancedPrompt = findViewById(R.id.edit_text_enhanced_prompt);
        radioGroupEnhancementType = findViewById(R.id.radio_group_enhancement_type);
        radioEnhanceDetail = findViewById(R.id.radio_enhance_detail);
        radioEnhanceStyle = findViewById(R.id.radio_enhance_style);
        radioEnhanceTechnical = findViewById(R.id.radio_enhance_technical);
        btnEnhance = findViewById(R.id.btn_enhance);
        btnRefineFurther = findViewById(R.id.btn_refine_further);
        btnUsePrompt = findViewById(R.id.btn_use_prompt);
        cardEnhancedPrompt = findViewById(R.id.card_enhanced_prompt);
        cardEnhancementExplanation = findViewById(R.id.card_enhancement_explanation);
        textEnhancementExplanation = findViewById(R.id.text_enhancement_explanation);
        progressEnhancement = findViewById(R.id.progress_enhancement);
    }

    private void setupButtonListeners() {
        btnEnhance.setOnClickListener(v -> enhancePrompt());
        btnRefineFurther.setOnClickListener(v -> refineFurther());
        btnUsePrompt.setOnClickListener(v -> usePrompt());
    }

    private void checkForIncomingPrompt() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("prompt")) {
            String incomingPrompt = intent.getStringExtra("prompt");
            editTextOriginalPrompt.setText(incomingPrompt);
        }
    }

    private void enhancePrompt() {
        String originalPrompt = editTextOriginalPrompt.getText().toString().trim();
        if (originalPrompt.isEmpty()) {
            Toast.makeText(this, "Please enter a prompt to enhance", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress and hide results initially
        progressEnhancement.setVisibility(View.VISIBLE);
        cardEnhancedPrompt.setVisibility(View.GONE);
        cardEnhancementExplanation.setVisibility(View.GONE);

        // Determine enhancement type
        String enhancementType;
        if (radioEnhanceStyle.isChecked()) {
            enhancementType = "style";
        } else if (radioEnhanceTechnical.isChecked()) {
            enhancementType = "technical";
        } else {
            enhancementType = "detail";
        }

        // Simulate enhancement in background thread
        executor.execute(() -> {
            // Simulate API call delay
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Generate enhanced prompt (in a real app, this would come from an API)
            String enhancedPrompt = generateEnhancedPrompt(originalPrompt, enhancementType);
            String explanation = enhancementExplanations.get(enhancementType);

            // Update UI on main thread
            runOnUiThread(() -> {
                progressEnhancement.setVisibility(View.GONE);
                editTextEnhancedPrompt.setText(enhancedPrompt);
                textEnhancementExplanation.setText(explanation);
                cardEnhancedPrompt.setVisibility(View.VISIBLE);
                cardEnhancementExplanation.setVisibility(View.VISIBLE);
            });
        });
    }

    private String generateEnhancedPrompt(String originalPrompt, String enhancementType) {
        // In a real app, this would call an API. Here we simulate enhancement
        switch (enhancementType) {
            case "detail":
                return originalPrompt + ", highly detailed, intricate, cinematic lighting, ultra HD, 8K resolution";
            case "style":
                return originalPrompt + ", in the style of Van Gogh, vibrant colors, expressive brush strokes";
            case "technical":
                return originalPrompt + ", 4K resolution, professional photography, sharp focus, studio lighting";
            default:
                return originalPrompt + ", enhanced with additional details and clarity";
        }
    }

    private void refineFurther() {
        String currentEnhancedPrompt = editTextEnhancedPrompt.getText().toString().trim();
        if (currentEnhancedPrompt.isEmpty()) {
            Toast.makeText(this, "No enhanced prompt to refine", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use the enhanced prompt as the new original
        editTextOriginalPrompt.setText(currentEnhancedPrompt);
        editTextEnhancedPrompt.setText("");
        cardEnhancedPrompt.setVisibility(View.GONE);
        cardEnhancementExplanation.setVisibility(View.GONE);
    }

    private void usePrompt() {
        String enhancedPrompt = editTextEnhancedPrompt.getText().toString().trim();
        if (enhancedPrompt.isEmpty()) {
            Toast.makeText(this, "No enhanced prompt to use", Toast.LENGTH_SHORT).show();
            return;
        }

        // Return the enhanced prompt to the calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("enhanced_prompt", enhancedPrompt);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}