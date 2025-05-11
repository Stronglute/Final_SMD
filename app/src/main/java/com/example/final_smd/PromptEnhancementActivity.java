package com.example.final_smd;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.final_smd.utilis.*;                     // ApiClient / ApiService / DTOs
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromptEnhancementActivity extends AppCompatActivity {

    // UI
    private TextInputEditText editTextOriginalPrompt;
    private TextInputEditText editTextEnhancedPrompt;
    private RadioButton radioEnhanceDetail, radioEnhanceStyle, radioEnhanceTechnical;
    private Button btnEnhance, btnRefineFurther, btnUsePrompt;
    private MaterialCardView cardEnhancedPrompt, cardEnhancementExplanation;
    private TextView textEnhancementExplanation;
    private ProgressBar progressEnhancement;

    // Retrofit
    private ApiService api;

    // Explanation map
    private final Map<String, String> explanationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt_enhancement);

        api = ApiClient.get().create(ApiService.class);

        setUpToolbar();
        initExplanations();
        bindViews();
        setupListeners();
        importIncomingPrompt();
    }

    /* ───────────────────────────────── UI helpers ─────────────────────────── */

    private void setUpToolbar() {
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void bindViews() {
        editTextOriginalPrompt      = findViewById(R.id.edit_text_original_prompt);
        editTextEnhancedPrompt      = findViewById(R.id.edit_text_enhanced_prompt);
        radioEnhanceDetail          = findViewById(R.id.radio_enhance_detail);
        radioEnhanceStyle           = findViewById(R.id.radio_enhance_style);
        radioEnhanceTechnical       = findViewById(R.id.radio_enhance_technical);
        btnEnhance                  = findViewById(R.id.btn_enhance);
        btnRefineFurther            = findViewById(R.id.btn_refine_further);
        btnUsePrompt                = findViewById(R.id.btn_use_prompt);
        cardEnhancedPrompt          = findViewById(R.id.card_enhanced_prompt);
        cardEnhancementExplanation  = findViewById(R.id.card_enhancement_explanation);
        textEnhancementExplanation  = findViewById(R.id.text_enhancement_explanation);
        progressEnhancement         = findViewById(R.id.progress_enhancement);
    }

    private void setupListeners() {
        btnEnhance.setOnClickListener(v -> performEnhance());
        btnRefineFurther.setOnClickListener(v -> refineFurther());
        btnUsePrompt.setOnClickListener(v -> copyAndReturn());
    }

    private void initExplanations() {
        explanationMap.put("detail",
                "Added rich descriptive elements – lighting, perspective, environment.");
        explanationMap.put("style",
                "Injected stylistic cues: art‑movement references, palette, mood.");
        explanationMap.put("technical",
                "Added technical parameters for higher quality (resolution, sharp focus…).");
    }

    private void importIncomingPrompt() {
        Intent in = getIntent();
        if (in != null && in.hasExtra("prompt")) {
            editTextOriginalPrompt.setText(in.getStringExtra("prompt"));
        }
    }

    /* ───────────────────────────────── Core actions ───────────────────────── */

    private void performEnhance() {
        String prompt = editTextOriginalPrompt.getText().toString().trim();
        if (prompt.isEmpty()) { toast("Please enter a prompt first"); return; }

        // Which enhancement type?
        String type = radioEnhanceStyle.isChecked()   ? "style"
                : radioEnhanceTechnical.isChecked()? "technical"
                : "detail";

        // UI: loading state
        setLoading(true);

        // Build request
        PromptEnhanceRequest body = new PromptEnhanceRequest(prompt, null); // style=null
        api.enhancePrompt(body).enqueue(new Callback<PromptEnhanceResponse>() {
            @Override public void onResponse(Call<PromptEnhanceResponse> c,
                                             Response<PromptEnhanceResponse> r) {
                setLoading(false);
                if (r.isSuccessful() && r.body() != null) {
                    showResult(r.body().enhanced_prompt, explanationMap.get(type));
                } else {
                    toast("Enhancement failed (" + r.code() + ")");
                }
            }
            @Override public void onFailure(Call<PromptEnhanceResponse> c, Throwable t) {
                setLoading(false);
                toast("Network error: " + t.getMessage());
            }
        });
    }

    private void refineFurther() {
        String current = editTextEnhancedPrompt.getText().toString().trim();
        if (current.isEmpty()) { toast("No enhanced prompt to refine"); return; }
        // Move enhanced → original for another round
        editTextOriginalPrompt.setText(current);
        cardEnhancedPrompt.setVisibility(View.GONE);
        cardEnhancementExplanation.setVisibility(View.GONE);
        editTextEnhancedPrompt.setText("");
    }

    private void copyAndReturn() {
        String enhanced = editTextEnhancedPrompt.getText().toString().trim();
        if (enhanced.isEmpty()) { toast("Nothing to copy"); return; }

        // copy to clipboard
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("enhanced_prompt", enhanced));
        toast("Copied to clipboard");

        // also return to caller
        Intent out = new Intent();
        out.putExtra("enhanced_prompt", enhanced);
        setResult(RESULT_OK, out);
        finish();
    }

    /* ───────────────────────────────── UI helpers ─────────────────────────── */

    private void setLoading(boolean on) {
        progressEnhancement.setVisibility(on ? View.VISIBLE : View.GONE);
        btnEnhance.setEnabled(!on);
    }

    private void showResult(String prompt, String explanation) {
        editTextEnhancedPrompt.setText(prompt);
        textEnhancementExplanation.setText(explanation != null ? explanation : "");
        cardEnhancedPrompt.setVisibility(View.VISIBLE);
        cardEnhancementExplanation.setVisibility(View.VISIBLE);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /* ───────────────────────────────── Toolbar back ───────────────────────── */

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
