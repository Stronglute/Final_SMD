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
        if (filename.isEmpty()) { filenameInput.setError("Filename required"); return; }

        // pick format (you could transcode later; for now we just keep mp4)
        String format = radioMp4.isChecked() ? "mp4"
                : radioGif.isChecked() ? "gif"
                : "webm";

        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Exporting…");
        exportButton.setEnabled(false);

        executorService.execute(() -> {
            try {
                // 1) decide destination
                File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                if (!moviesDir.exists() && !moviesDir.mkdirs()) {
                    throw new Exception("Cannot access Movies folder");
                }
                File outFile = new File(moviesDir, filename + "." + format);

                // 2) copy bytes
                copyUriToFile(videoUri, outFile);

                // 3) make it visible in gallery
                MediaScannerConnection.scanFile(this,
                        new String[]{outFile.getAbsolutePath()}, null, null);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Saved to " + outFile.getAbsolutePath());
                    exportButton.setEnabled(true);
                    showShareOption(outFile);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Error: "+e.getMessage());
                    exportButton.setEnabled(true);
                });
            }
        });
    }

    /** Copies whatever the Uri points to (http/content/file) into destFile. */
    private void copyUriToFile(Uri srcUri, File destFile) throws Exception {
        try (java.io.InputStream in =
                     ("http".equals(srcUri.getScheme()) || "https".equals(srcUri.getScheme()))
                             ? new java.net.URL(srcUri.toString()).openStream()
                             : getContentResolver().openInputStream(srcUri);
             java.io.OutputStream out = new java.io.FileOutputStream(destFile)) {

            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) { out.write(buf, 0, len); }
        }
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