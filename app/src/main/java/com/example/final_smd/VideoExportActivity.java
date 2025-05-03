package com.example.final_smd;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoExportActivity extends AppCompatActivity {

    private VideoView videoPreview;
    private RadioGroup formatRadioGroup;
    private RadioButton radioMp4, radioGif, radioWebm;
    private Spinner resolutionSpinner;
    private SeekBar qualitySeekBar;
    private TextView qualityValueText, statusText;
    private TextInputEditText filenameInput;
    private Button exportButton;
    private ProgressBar progressBar;
    private Uri videoUri;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_export);

        // Initialize UI components
        videoPreview = findViewById(R.id.video_preview);
        formatRadioGroup = findViewById(R.id.format_radio_group);
        radioMp4 = findViewById(R.id.radio_mp4);
        radioGif = findViewById(R.id.radio_gif);
        radioWebm = findViewById(R.id.radio_webm);
        resolutionSpinner = findViewById(R.id.spinner_resolution);
        qualitySeekBar = findViewById(R.id.seekbar_quality);
        qualityValueText = findViewById(R.id.text_quality_value);
        filenameInput = findViewById(R.id.filename_input);
        exportButton = findViewById(R.id.btn_export);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.text_status);

        // Create thread pool for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Get video URI from intent
        String videoUriString = getIntent().getStringExtra("VIDEO_URI");
        if (videoUriString != null) {
            videoUri = Uri.parse(videoUriString);
            videoPreview.setVideoURI(videoUri);
            videoPreview.start();
        } else {
            Toast.makeText(this, "No video to export", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup resolution spinner
        setupResolutionSpinner();

        // Setup quality seek bar
        qualitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qualityValueText.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Setup export button
        exportButton.setOnClickListener(v -> exportVideo());
    }

    private void setupResolutionSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.video_resolution_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resolutionSpinner.setAdapter(adapter);
    }

    private void exportVideo() {
        String filename = filenameInput.getText().toString().trim();
        if (filename.isEmpty()) {
            filenameInput.setError("Filename required");
            return;
        }

        // Get selected format
        String format;
        if (radioMp4.isChecked()) {
            format = "mp4";
        } else if (radioGif.isChecked()) {
            format = "gif";
        } else {
            format = "webm";
        }

        // Get selected resolution
        String resolution = resolutionSpinner.getSelectedItem().toString();

        // Get selected quality
        int quality = qualitySeekBar.getProgress();

        // Show progress indicators
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Exporting video...");
        exportButton.setEnabled(false);

        // Execute the export task in background
        executorService.execute(() -> {
            try {
                // Simulate export process
                simulateVideoExport();

                // Create output file
                File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                String outputFileName = filename + "." + format;
                File outputFile = new File(outputDir, outputFileName);

                // In a real app, you would perform the actual video conversion here
                // using MediaCodec, FFmpeg, or other libraries

                // For demonstration, we'll just copy the original file
                // FileUtils.copyFile(new File(videoUri.getPath()), outputFile);

                // Scan the file so it appears in the gallery
                MediaScannerConnection.scanFile(this,
                        new String[]{outputFile.getAbsolutePath()},
                        null,
                        null);

                // Update UI on main thread
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Video exported successfully to: " + outputFile.getAbsolutePath());
                    exportButton.setEnabled(true);

                    // Show share option
                    showShareOption(outputFile);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Error: " + e.getMessage());
                    exportButton.setEnabled(true);
                });
            }
        });
    }

    private void simulateVideoExport() throws InterruptedException {
        // Simulate conversion process
        for (int i = 0; i <= 100; i += 5) {
            final int progress = i;
            runOnUiThread(() -> progressBar.setProgress(progress));
            Thread.sleep(100); // Simulating work being done
        }
    }

    private void showShareOption(File videoFile) {
        Uri fileUri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".provider",
                videoFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share video via"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}