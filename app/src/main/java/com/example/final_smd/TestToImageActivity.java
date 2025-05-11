package com.example.final_smd;

import android.app.DownloadManager;
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

import com.example.final_smd.utilis.ApiService;
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
import com.bumptech.glide.Glide;
import com.example.final_smd.utilis.*;   // ApiClient & ApiService
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TestToImageActivity extends AppCompatActivity {
    private ApiService api;              // add field
    private String generatedImageUrl;    // keep URL for download

    private TextInputEditText editTextPrompt;
    private Spinner spinnerResolution;
    private Spinner spinnerStyle;
    private SeekBar seekBarGuidanceScale;
    private TextView textGuidanceScaleValue;
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
        api = ApiClient.get().create(ApiService.class);
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
    }

    private void setupButtons() {
        btnEnhancePrompt.setOnClickListener(v -> enhancePrompt());
        btnGenerateImage.setOnClickListener(v -> generateImage());
        btnRegenerate.setOnClickListener(v -> generateImage());
        btnSaveImage.setOnClickListener(v -> saveImage());
    }

    private String getPromptText() {
        return editTextPrompt.getText() == null ? "" : editTextPrompt.getText().toString();
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }

    private void showLoading(boolean loading) {
        progressImageGeneration.setVisibility(loading ? View.VISIBLE : View.GONE);
        cardImagePreview.setVisibility(View.VISIBLE);
        imagePreview.setVisibility(loading ? View.GONE : View.VISIBLE);
        btnGenerateImage.setEnabled(!loading);
        btnRegenerate.setEnabled(!loading);
        btnSaveImage.setEnabled(!loading && generatedImageUrl != null);
    }

    private int[] parseResolution(String res) {
        // e.g. "1024x1024" → [1024,1024]
        String[] parts = res.toLowerCase().split("x");
        return new int[]{ Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim()) };
    }

    private void pollTask(String taskId) {
        executor.execute(() -> {
            try {
                while (true) {
                    Response<TaskStatusResponse> r = api.getTaskStatus(taskId).execute();

                    if (!r.isSuccessful() || r.body() == null) {
                        runOnUiThread(() -> {
                            toast("Task check failed: " + r.code());
                            showLoading(false);
                        });
                        return;
                    }

                    TaskStatusResponse body = r.body();

                    // get status from whichever branch is present
                    String status;
                    if (body.data != null && body.data.status != null) {
                        status = body.data.status.toLowerCase();
                    } else {
                        status = body.status.toLowerCase();
                    }

                    switch (status) {
                        case "completed":
                        case "success":
                            generatedImageUrl = body.data.output.image_url;
                            runOnUiThread(() -> loadImageIntoPreview(generatedImageUrl));
                            return;

                        case "failed":
                        case "error":
                            runOnUiThread(() -> {
                                toast("Image generation failed");
                                showLoading(false);
                            });
                            return;

                        default:
                            // pending / running – keep polling
                            Thread.sleep(10000);
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    toast("Polling error: " + e.getMessage());
                    showLoading(false);
                });
            }
        });
    }

    private void loadImageIntoPreview(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_image)  // optional
                .into(imagePreview);

        showLoading(false);
        btnRegenerate.setEnabled(true);
        btnSaveImage.setEnabled(true);
        toast("Image ready!");
    }

    private void enhancePrompt() {
        String original = getPromptText();
        if (original.isEmpty()) {
            toast("Please enter a prompt first"); return;
        }

        String style = selectedStyle.equals("None") ? null : selectedStyle;

        btnEnhancePrompt.setEnabled(false);

        api.enhancePrompt(new PromptEnhanceRequest(original, style))
                .enqueue(new Callback<PromptEnhanceResponse>() {
                    @Override public void onResponse(Call<PromptEnhanceResponse> c,
                                                     Response<PromptEnhanceResponse> r) {
                        btnEnhancePrompt.setEnabled(true);
                        if (r.isSuccessful() && r.body() != null) {
                            editTextPrompt.setText(r.body().enhanced_prompt);
                        } else {
                            toast("Enhance failed: " + r.code());
                        }
                    }
                    @Override public void onFailure(Call<PromptEnhanceResponse> c, Throwable t) {
                        btnEnhancePrompt.setEnabled(true);
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    private void generateImage() {
        String prompt = getPromptText();
        if (prompt.isEmpty()) { toast("Enter a prompt"); return; }

        int[] wh = parseResolution(selectedResolution); // helper below
        showLoading(true);

        api.generateImage(new GenerateImageRequest(
                        prompt, wh[0], wh[1], guidanceScale,
                        selectedStyle.equals("None") ? null : selectedStyle))
                .enqueue(new Callback<GenerateImageResponse>() {
                    @Override public void onResponse(Call<GenerateImageResponse> c,
                                                     Response<GenerateImageResponse> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            pollTask(r.body().task_id);
                        } else {
                            toast("Generation start failed"); showLoading(false);
                        }
                    }
                    @Override public void onFailure(Call<GenerateImageResponse> c, Throwable t) {
                        toast("Network error: " + t.getMessage()); showLoading(false);
                    }
                });
    }

    private void saveImage() {
        if (generatedImageUrl == null) { toast("Generate an image first"); return; }

        // Use Android's DownloadManager (API >= 9) for simplicity
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(generatedImageUrl);
        DownloadManager.Request req = new DownloadManager.Request(uri);
        req.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_PICTURES, "AIGenApp/" + uri.getLastPathSegment());

        dm.enqueue(req);
        toast("Download started…");
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