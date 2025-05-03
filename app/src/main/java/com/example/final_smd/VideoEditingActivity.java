package com.example.final_smd;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoEditingActivity extends AppCompatActivity {

    private VideoView videoPreview;
    private Button playPauseButton, resetButton, applyFpsButton,
            applyInterpolationButton, applyAdjustmentsButton,
            saveButton, exportButton;
    private SeekBar fpsSeekBar, smoothnessSeekBar, brightnessSeekBar,
            contrastSeekBar, saturationSeekBar;
    private TextView fpsValueText, smoothnessValueText, statusText;
    private RadioGroup interpolationRadioGroup;
    private RadioButton linearRadio, opticalFlowRadio, aiRadio;
    private ProgressBar progressBar;
    private Uri videoUri;
    private Uri editedVideoUri;
    private boolean isPlaying = false;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editing);

        // Initialize UI components
        videoPreview = findViewById(R.id.video_preview);
        playPauseButton = findViewById(R.id.btn_play_pause);
        resetButton = findViewById(R.id.btn_reset);
        applyFpsButton = findViewById(R.id.btn_apply_fps);
        applyInterpolationButton = findViewById(R.id.btn_apply_interpolation);
        applyAdjustmentsButton = findViewById(R.id.btn_apply_adjustments);
        saveButton = findViewById(R.id.btn_save);
        exportButton = findViewById(R.id.btn_export);

        fpsSeekBar = findViewById(R.id.seekbar_fps);
        smoothnessSeekBar = findViewById(R.id.seekbar_smoothness);
        brightnessSeekBar = findViewById(R.id.seekbar_brightness);
        contrastSeekBar = findViewById(R.id.seekbar_contrast);
        saturationSeekBar = findViewById(R.id.seekbar_saturation);

        fpsValueText = findViewById(R.id.text_fps_value);
        smoothnessValueText = findViewById(R.id.text_smoothness_value);
        statusText = findViewById(R.id.text_status);

        interpolationRadioGroup = findViewById(R.id.interpolation_radio_group);
        linearRadio = findViewById(R.id.radio_linear);
        opticalFlowRadio = findViewById(R.id.radio_optical_flow);
        aiRadio = findViewById(R.id.radio_ai);

        progressBar = findViewById(R.id.progress_bar);

        // Create thread pool for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Get video URI from intent
        String videoUriString = getIntent().getStringExtra("VIDEO_URI");
        if (videoUriString != null) {
            videoUri = Uri.parse(videoUriString);
            editedVideoUri = videoUri; // Initially the same
            setupVideoPreview();
        } else {
            Toast.makeText(this, "No video to edit", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup seek bars
        setupSeekBars();

        // Setup buttons
        setupButtons();
    }

    private void setupVideoPreview() {
        videoPreview.setVideoURI(videoUri);
        videoPreview.setOnPreparedListener(mp -> {
            mp.setLooping(true);
        });
        videoPreview.setOnCompletionListener(mp -> {
            isPlaying = false;
            playPauseButton.setText("Play");
        });
        videoPreview.start();
        isPlaying = true;
        playPauseButton.setText("Pause");
    }

    private void setupSeekBars() {
        // FPS seek bar
        fpsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fps = Math.max(1, progress); // Ensure at least 1 FPS
                fpsValueText.setText(fps + " FPS");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Smoothness seek bar
        smoothnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                smoothnessValueText.setText(progress + "%");
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
        // Play/Pause button
        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                videoPreview.pause();
                playPauseButton.setText("Play");
            } else {
                videoPreview.start();
                playPauseButton.setText("Pause");
            }
            isPlaying = !isPlaying;
        });

        // Reset button
        resetButton.setOnClickListener(v -> {
            videoPreview.stopPlayback();
            videoPreview.setVideoURI(videoUri);
            videoPreview.start();
            isPlaying = true;
            playPauseButton.setText("Pause");

            // Reset edited URI to original
            editedVideoUri = videoUri;

            // Reset all seek bars
            fpsSeekBar.setProgress(30);
            smoothnessSeekBar.setProgress(50);
            brightnessSeekBar.setProgress(50);
            contrastSeekBar.setProgress(50);
            saturationSeekBar.setProgress(50);

            // Reset radio buttons
            linearRadio.setChecked(true);

            statusText.setText("Video reset to original");
        });

        // Apply FPS button
        applyFpsButton.setOnClickListener(v -> applyFrameRateChanges());

        // Apply Interpolation button
        applyInterpolationButton.setOnClickListener(v -> applyInterpolation());

        // Apply Adjustments button
        applyAdjustmentsButton.setOnClickListener(v -> applyAdjustments());

        // Save button
        saveButton.setOnClickListener(v -> saveChanges());

        // Export button
        exportButton.setOnClickListener(v -> exportVideo());
    }

    private void applyFrameRateChanges() {
        int targetFps = fpsSeekBar.getProgress();

        // Show progress indicators
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Adjusting frame rate to " + targetFps + " FPS...");
        disableButtons(true);

        executorService.execute(() -> {
            try {
                // Simulate frame rate adjustment
                simulateProcessing();

                // In a real app, you would adjust the frame rate using MediaCodec or FFmpeg
                // This would involve decoding, modifying, and re-encoding the video

                // After processing, update the video preview
                File outputFile = new File(getExternalFilesDir(null), "temp_fps_adjusted.mp4");
                editedVideoUri = Uri.fromFile(outputFile);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Frame rate adjusted to " + targetFps + " FPS");
                    disableButtons(false);

                    // Update video preview
                    updateVideoPreview();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Error: " + e.getMessage());
                    disableButtons(false);
                });
            }
        });
    }

    private void applyInterpolation() {
        String interpolationType;
        if (linearRadio.isChecked()) {
            interpolationType = "Linear";
        } else if (opticalFlowRadio.isChecked()) {
            interpolationType = "Optical Flow";
        } else {
            interpolationType = "AI-Enhanced";
        }

        int smoothness = smoothnessSeekBar.getProgress();

        // Show progress indicators
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Applying " + interpolationType + " interpolation...");
        disableButtons(true);

        executorService.execute(() -> {
            try {
                // Simulate interpolation processing
                simulateProcessing();

                // In a real app, you would apply frame interpolation using a library
                // like FFmpeg with appropriate filters or a custom implementation

                // After processing, update the video preview
                File outputFile = new File(getExternalFilesDir(null), "temp_interpolated.mp4");
                editedVideoUri = Uri.fromFile(outputFile);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText(interpolationType + " interpolation applied with " + smoothness + "% smoothness");
                    disableButtons(false);

                    // Update video preview
                    updateVideoPreview();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Error: " + e.getMessage());
                    disableButtons(false);
                });
            }
        });
    }

    private void applyAdjustments() {
        int brightness = brightnessSeekBar.getProgress() - 50; // -50 to +50
        int contrast = contrastSeekBar.getProgress() - 50;     // -50 to +50
        int saturation = saturationSeekBar.getProgress() - 50; // -50 to +50

        // Show progress indicators
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Applying color adjustments...");
        disableButtons(true);

        executorService.execute(() -> {
            try {
                // Simulate adjustment processing
                simulateProcessing();

                // In a real app, you would apply color adjustments using MediaEffects
                // or a video processing library like FFmpeg with appropriate filters

                // After processing, update the video preview
                File outputFile = new File(getExternalFilesDir(null), "temp_adjusted.mp4");
                editedVideoUri = Uri.fromFile(outputFile);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Color adjustments applied");
                    disableButtons(false);

                    // Update video preview
                    updateVideoPreview();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Error: " + e.getMessage());
                    disableButtons(false);
                });
            }
        });
    }

    private void saveChanges() {
        // Show progress indicators
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Saving changes...");
        disableButtons(true);

        executorService.execute(() -> {
            try {
                // Simulate saving process
                simulateProcessing();

                // In a real app, you would finalize all changes and save the video
                // to a more permanent location
                File outputFile = new File(getExternalFilesDir(null), "edited_video.mp4");
                editedVideoUri = Uri.fromFile(outputFile);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Changes saved successfully");
                    disableButtons(false);

                    // Show toast
                    Toast.makeText(VideoEditingActivity.this,
                            "Video edited successfully",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Error: " + e.getMessage());
                    disableButtons(false);
                });
            }
        });
    }

    private void exportVideo() {
        // Launch the export activity with the edited video URI
        Intent intent = new Intent(this, VideoExportActivity.class);
        intent.putExtra("VIDEO_URI", editedVideoUri.toString());
        startActivity(intent);
    }

    private void updateVideoPreview() {
        int currentPosition = 0;
        if (videoPreview.isPlaying()) {
            currentPosition = videoPreview.getCurrentPosition();
            videoPreview.stopPlayback();
        }

        videoPreview.setVideoURI(editedVideoUri);
        videoPreview.seekTo(currentPosition);

        if (isPlaying) {
            videoPreview.start();
        }
    }

    private void simulateProcessing() throws InterruptedException {
        // Simulate video processing
        for (int i = 0; i <= 100; i += 5) {
            final int progress = i;
            runOnUiThread(() -> progressBar.setProgress(progress));
            Thread.sleep(100); // Simulating work being done
        }
    }

    private void disableButtons(boolean disable) {
        applyFpsButton.setEnabled(!disable);
        applyInterpolationButton.setEnabled(!disable);
        applyAdjustmentsButton.setEnabled(!disable);
        saveButton.setEnabled(!disable);
        exportButton.setEnabled(!disable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPreview != null && videoPreview.isPlaying()) {
            videoPreview.pause();
            isPlaying = false;
            playPauseButton.setText("Play");
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