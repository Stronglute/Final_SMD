package com.example.final_smd;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoGenerationActivity extends AppCompatActivity {

    private TextInputEditText promptInput;
    private Spinner lengthSpinner, styleSpinner, qualitySpinner;
    private Button generateButton, editButton, exportButton;
    private ProgressBar progressBar;
    private TextView statusText, previewPlaceholder;
    private VideoView videoPreview;
    private Uri generatedVideoUri;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_generation);

        // Initialize UI components
        promptInput = findViewById(R.id.prompt_input);
        lengthSpinner = findViewById(R.id.spinner_length);
        styleSpinner = findViewById(R.id.spinner_style);
        qualitySpinner = findViewById(R.id.spinner_quality);
        generateButton = findViewById(R.id.btn_generate);
        editButton = findViewById(R.id.btn_edit_video);
        exportButton = findViewById(R.id.btn_export_video);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.text_status);
        previewPlaceholder = findViewById(R.id.text_preview_placeholder);
        videoPreview = findViewById(R.id.video_preview);

        // Setup spinners
        setupSpinners();

        // Create thread pool for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Set click listeners
        generateButton.setOnClickListener(v -> generateVideo());
        editButton.setOnClickListener(v -> editVideo());
        exportButton.setOnClickListener(v -> exportVideo());
    }

    private void setupSpinners() {
        // Video length options
        ArrayAdapter<CharSequence> lengthAdapter = ArrayAdapter.createFromResource(this,
                R.array.video_length_options, android.R.layout.simple_spinner_item);
        lengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lengthSpinner.setAdapter(lengthAdapter);

        // Video style options
        ArrayAdapter<CharSequence> styleAdapter = ArrayAdapter.createFromResource(this,
                R.array.video_style_options, android.R.layout.simple_spinner_item);
        styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        styleSpinner.setAdapter(styleAdapter);

        // Video quality options
        ArrayAdapter<CharSequence> qualityAdapter = ArrayAdapter.createFromResource(this,
                R.array.video_quality_options, android.R.layout.simple_spinner_item);
        qualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(qualityAdapter);
    }

    private void generateVideo() {
        String prompt = promptInput.getText().toString().trim();
        if (prompt.isEmpty()) {
            promptInput.setError("Please enter a prompt");
            return;
        }

        // Get selected options
        String length = lengthSpinner.getSelectedItem().toString();
        String style = styleSpinner.getSelectedItem().toString();
        String quality = qualitySpinner.getSelectedItem().toString();

        // Show progress indicators
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Generating video...");
        generateButton.setEnabled(false);

        // Execute the generation task in background
        executorService.execute(() -> {
            // This would be where you connect to the Stable Video Diffusion API
            // For now, we'll simulate the process
            try {
                simulateVideoGeneration();

                // Get a uri to the generated video (this would be from your API response)
                File videoFile = new File(getExternalFilesDir(null), "generated_video.mp4");
                generatedVideoUri = Uri.fromFile(videoFile);

                // Update UI on main thread
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Video generated successfully!");
                    generateButton.setEnabled(true);

                    // Show video preview
                    previewPlaceholder.setVisibility(View.GONE);
                    videoPreview.setVisibility(View.VISIBLE);
                    videoPreview.setVideoURI(generatedVideoUri);
                    videoPreview.start();

                    // Enable edit and export buttons
                    editButton.setEnabled(true);
                    exportButton.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Error: " + e.getMessage());
                    generateButton.setEnabled(true);
                });
            }
        });
    }

    private void simulateVideoGeneration() throws InterruptedException {
        // Simulate API call and processing time
        for (int i = 0; i <= 100; i += 10) {
            final int progress = i;
            runOnUiThread(() -> progressBar.setProgress(progress));
            Thread.sleep(500); // Simulating work being done
        }
    }

    private void editVideo() {
        if (generatedVideoUri != null) {
            Intent intent = new Intent(this, VideoEditingActivity.class);
            intent.putExtra("VIDEO_URI", generatedVideoUri.toString());
            startActivity(intent);
        }
    }

    private void exportVideo() {
        if (generatedVideoUri != null) {
            Intent intent = new Intent(this, VideoExportActivity.class);
            intent.putExtra("VIDEO_URI", generatedVideoUri.toString());
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}