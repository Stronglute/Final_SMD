package com.example.final_smd;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TestToImageActivity extends AppCompatActivity {

    private TextInputEditText editTextPrompt;
    private Spinner spinnerResolution;
    private Spinner spinnerStyle;
    private SeekBar seekBarGuidanceScale;
    private TextView textGuidanceScaleValue;
    private SeekBar seekBarSteps;
    private TextView textStepsValue;
    private Button btnEnhancePrompt;
    private Button btnGenerateImage;
    private MaterialCardView cardImagePreview;
    private ImageView imagePreview;
    private ProgressBar progressImageGeneration;
    private Button btnRegenerate;
    private Button btnSaveImage;

    // Settings values
    private String selectedResolution;
    private String selectedStyle;
    private float guidanceScale = 7.0f;
    private int steps = 20;

    // Dummy bitmap for development (in a real app, this would come from the AI model)
    private Bitmap generatedBitmap = null;

    // Executor for background tasks
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_image);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI components
        initializeViews();
        setupSpinners();
        setupSeekBars();
        setupButtons();
    }

    private void initializeViews() {
        editTextPrompt = findViewById(R.id.edit_text_prompt);
        spinnerResolution = findViewById(R.id.spinner_resolution);
        spinnerStyle = findViewById(R.id.spinner_style);
        seekBarGuidanceScale = findViewById(R.id.seekbar_guidance_scale);
        textGuidanceScaleValue = findViewById(R.id.text_guidance_scale_value);
        seekBarSteps = findViewById(R.id.seekbar_steps);
        textStepsValue = findViewById(R.id.text_steps_value);
        btnEnhancePrompt = findViewById(R.id.btn_enhance_prompt);
        btnGenerateImage = findViewById(R.id.btn_generate_image);
        cardImagePreview = findViewById(R.id.card_image_preview);
        imagePreview = findViewById(R.id.image_preview);
        progressImageGeneration = findViewById(R.id.progress_image_generation);
        btnRegenerate = findViewById(R.id.btn_regenerate);
        btnSaveImage = findViewById(R.id.btn_save_image);
    }

    private void setupSpinners() {
        // Resolution spinner
        ArrayAdapter<CharSequence> resolutionAdapter = ArrayAdapter.createFromResource(
                this, R.array.resolution_options, android.R.layout.simple_spinner_item);
        resolutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResolution.setAdapter(resolutionAdapter);
        spinnerResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedResolution = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedResolution = parent.getItemAtPosition(0).toString();
            }
        });

        // Style spinner
        ArrayAdapter<CharSequence> styleAdapter = ArrayAdapter.createFromResource(
                this, R.array.style_options, android.R.layout.simple_spinner_item);
        styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStyle.setAdapter(styleAdapter);
        spinnerStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStyle = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStyle = parent.getItemAtPosition(0).toString();
            }
        });
    }

    private void setupSeekBars() {
        // Guidance scale seek bar
        seekBarGuidanceScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                guidanceScale = progress / 2.0f;
                textGuidanceScaleValue.setText(String.valueOf(guidanceScale));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Steps seek bar
        seekBarSteps.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                steps = progress;
                textStepsValue.setText(String.valueOf(steps));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setupButtons() {
        btnEnhancePrompt.setOnClickListener(v -> enhancePrompt());
        btnGenerateImage.setOnClickListener(v -> generateImage());
        btnRegenerate.setOnClickListener(v -> generateImage());
        btnSaveImage.setOnClickListener(v -> saveImage());
    }

    private void enhancePrompt() {
        // In a real app, this would communicate with the LLM API to enhance the prompt
        // For demonstration, we'll just modify the prompt with a sample enhancement

        String originalPrompt = editTextPrompt.getText() != null ?
                editTextPrompt.getText().toString() : "";

        if (originalPrompt.isEmpty()) {
            Toast.makeText(this, "Please enter a prompt first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate prompt enhancement (in production, this would call your LLM API)
        String enhancedPrompt = originalPrompt + ", cinematic lighting, highly detailed, 8k resolution, photorealistic, professional photography";
        editTextPrompt.setText(enhancedPrompt);

        Toast.makeText(this, "Prompt enhanced with details", Toast.LENGTH_SHORT).show();
    }

    private void generateImage() {
        String prompt = editTextPrompt.getText() != null ?
                editTextPrompt.getText().toString() : "";

        if (prompt.isEmpty()) {
            Toast.makeText(this, "Please enter a prompt first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        progressImageGeneration.setVisibility(View.VISIBLE);
        cardImagePreview.setVisibility(View.VISIBLE);
        imagePreview.setVisibility(View.GONE);
        btnRegenerate.setEnabled(false);
        btnSaveImage.setEnabled(false);
        btnGenerateImage.setEnabled(false);

        // Simulate API call with delay (in production, this would call your image generation API)
        executor.execute(() -> {
            try {
                // Simulate network delay
                Thread.sleep(3000);

                // In a real app, this would be the result from the AI service
                // For demo, we'll load a sample image from resources
                generatedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample_generated_image);

                // Update UI on main thread
                runOnUiThread(() -> {
                    imagePreview.setImageBitmap(generatedBitmap);
                    imagePreview.setVisibility(View.VISIBLE);
                    progressImageGeneration.setVisibility(View.GONE);
                    btnRegenerate.setEnabled(true);
                    btnSaveImage.setEnabled(true);
                    btnGenerateImage.setEnabled(true);

                    Toast.makeText(TestToImageActivity.this,
                            "Image generated successfully", Toast.LENGTH_SHORT).show();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressImageGeneration.setVisibility(View.GONE);
                    btnGenerateImage.setEnabled(true);
                    Toast.makeText(TestToImageActivity.this,
                            "Error generating image", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveImage() {
        if (generatedBitmap == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // For Android 10 (API level 29) and above, use MediaStore
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            saveImageUsingMediaStore();
        } else {
            saveImageToExternalStorage();
        }
    }

    private void saveImageUsingMediaStore() {
        String fileName = "AIGenImage_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AIGenApp");

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (imageUri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(imageUri)) {
                if (out != null) {
                    generatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageToExternalStorage() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "AIGenApp");

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "AIGenImage_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".jpg";
        File file = new File(directory, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            generatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "Image saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
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