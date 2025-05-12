package com.example.final_smd;

import static android.content.Intent.getIntent;

import android.content.Context;
import android.content.Intent;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.media3.session.MediaController;

import java.io.*;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoEditingActivity extends AppCompatActivity {
    private static final int REQ_PICK_VIDEO = 1001;

    /* ── UI references (unchanged IDs; one extra seek bar) ─────────────── */
    private VideoView videoPreview;
    private SeekBar timelineSeek;
    private Button playPauseButton, resetButton, applyFpsButton,
            applyInterpolationButton, applyAdjustmentsButton,
            saveButton, exportButton;
    private SeekBar fpsSeekBar, smoothnessSeekBar, brightnessSeekBar,
            contrastSeekBar, saturationSeekBar;
    private TextView fpsValueText, smoothnessValueText, statusText;
    private RadioButton linearRadio, opticalFlowRadio, aiRadio;
    private ProgressBar progressBar;

    /* ── state ─────────────────────────────────────────────────────────── */
    private Uri originalVideoUri;
    private Uri editedVideoUri;
    private boolean isPlaying = false;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler ui = new Handler(Looper.getMainLooper());

    /* ------------------- onCreate ------------------------------------------------ */

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_video_editing);




        bindViews();
        setupTimelineSeek();
        String uriStr = getIntent().getStringExtra("VIDEO_URI");
        if (uriStr != null) {
            originalVideoUri = Uri.parse(uriStr);
            editedVideoUri   = originalVideoUri;
            setupVideoPreview();
        } else {
            // No URI → prompt the user to pick one
            pickVideoFromGallery();
        }

        setupVideoPreview();
        setupSeekBars();
        setupButtons();


    }
    private void pickVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, REQ_PICK_VIDEO);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // persist permission for future access
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                originalVideoUri = uri;
                editedVideoUri   = uri;
                setupVideoPreview();
                statusText.setText("Loaded video from gallery");
            }
        }
    }

    /* ------------------- Video preview & controller ----------------------------- */

    private void setupVideoPreview() {
        MediaController mc = new MediaController(this);
        mc.setAnchorView(videoPreview);
        videoPreview.setMediaController(mc);

        videoPreview.setVideoURI(originalVideoUri);
        videoPreview.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            timelineSeek.setMax(videoPreview.getDuration());
            startTimelineUpdater();
        });
        videoPreview.start();
        isPlaying = true;
        playPauseButton.setText("Pause");
    }

    /* Update timeline every 500 ms while playing */
    private void startTimelineUpdater() {
        ui.postDelayed(new Runnable() {
            @Override public void run() {
                if (videoPreview != null && isPlaying) {
                    timelineSeek.setProgress(videoPreview.getCurrentPosition());
                    ui.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    private void setupTimelineSeek() {
        timelineSeek = findViewById(R.id.seekbar_timeline);
        timelineSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s,int p,boolean f){}
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {
                if (videoPreview != null) videoPreview.seekTo(s.getProgress());
            }
        });
    }

    /* ------------------- Seek bars & buttons (mostly unchanged) ------------------ */

    private void bindViews() {
        videoPreview          = findViewById(R.id.video_preview);
        playPauseButton       = findViewById(R.id.btn_play_pause);
        resetButton           = findViewById(R.id.btn_reset);
        applyFpsButton        = findViewById(R.id.btn_apply_fps);
        applyInterpolationButton = findViewById(R.id.btn_apply_interpolation);
        applyAdjustmentsButton   = findViewById(R.id.btn_apply_adjustments);
        saveButton            = findViewById(R.id.btn_save);
        exportButton          = findViewById(R.id.btn_export);

        fpsSeekBar     = findViewById(R.id.seekbar_fps);
        smoothnessSeekBar  = findViewById(R.id.seekbar_smoothness);
        brightnessSeekBar  = findViewById(R.id.seekbar_brightness);
        contrastSeekBar    = findViewById(R.id.seekbar_contrast);
        saturationSeekBar  = findViewById(R.id.seekbar_saturation);

        fpsValueText   = findViewById(R.id.text_fps_value);
        smoothnessValueText = findViewById(R.id.text_smoothness_value);
        statusText     = findViewById(R.id.text_status);

        linearRadio    = findViewById(R.id.radio_linear);
        opticalFlowRadio = findViewById(R.id.radio_optical_flow);
        aiRadio        = findViewById(R.id.radio_ai);

        progressBar    = findViewById(R.id.progress_bar);
    }

    private void setupSeekBars() {
        fpsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s,int p,boolean f){ fpsValueText.setText(Math.max(1,p)+" FPS"); }
            @Override public void onStartTrackingTouch(SeekBar s){}
            @Override public void onStopTrackingTouch(SeekBar s){}
        });
        smoothnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s,int p,boolean f){ smoothnessValueText.setText(p+"%"); }
            @Override public void onStartTrackingTouch(SeekBar s){}
            @Override public void onStopTrackingTouch(SeekBar s){}
        });
    }

    private void setupButtons() {
        Button uploadButton = findViewById(R.id.btn_upload);
        uploadButton.setOnClickListener(v -> pickVideoFromGallery());

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) { videoPreview.pause(); playPauseButton.setText("Play"); }
            else           { videoPreview.start(); playPauseButton.setText("Pause"); }
            isPlaying = !isPlaying;
        });

        resetButton.setOnClickListener(v -> resetAll());

        applyFpsButton.setOnClickListener(v -> simulateEdit("Adjusting FPS…", "temp_fps.mp4"));
        applyInterpolationButton.setOnClickListener(v -> simulateEdit("Applying interpolation…", "temp_interp.mp4"));
        applyAdjustmentsButton.setOnClickListener(v -> simulateEdit("Applying color adjustments…", "temp_color.mp4"));

        saveButton.setOnClickListener(v -> simulateEdit("Saving…", "edited_final.mp4"));
        exportButton.setOnClickListener(v -> {
            Intent i = new Intent(this, VideoExportActivity.class);
            i.putExtra("VIDEO_URI", editedVideoUri.toString());
            startActivity(i);
        });
    }

    /* ------------------- Simulate processing but COPY actual bytes ----------- */

    private void simulateEdit(String doing, String tempFileName) {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText(doing);
        disableButtons(true);

        executor.submit(() -> {
            try {
                Thread.sleep(1500); // fake processing

                File outFile = new File(getExternalFilesDir(null), tempFileName);
                copyUriToFile(originalVideoUri, outFile); // ← real bytes
                editedVideoUri = Uri.fromFile(outFile);

                ui.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Done ✓");
                    disableButtons(false);
                    updateVideoPreview();
                });
            } catch (Exception e) {
                ui.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusText.setText("Error: "+e.getMessage());
                    disableButtons(false);
                });
            }
        });
    }

    /* ------------------- Utils ------------------------------------------------ */

    private void copyUriToFile(Uri src, File dest) throws Exception {
        try (InputStream in = openStreamFromUri(src);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192]; int len;
            while ((len = in.read(buf))!=-1) out.write(buf,0,len);
        }
    }
    private InputStream openStreamFromUri(Uri uri) throws Exception {
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
            return new URL(uri.toString()).openStream();
        return getContentResolver().openInputStream(uri);
    }

    private void updateVideoPreview() {
        int pos = videoPreview.getCurrentPosition();
        videoPreview.setVideoURI(editedVideoUri);
        videoPreview.seekTo(pos);
        if (isPlaying) videoPreview.start();
    }

    private void resetAll() {
        videoPreview.stopPlayback();
        editedVideoUri = originalVideoUri;
        setupVideoPreview();
        statusText.setText("Reset");
        fpsSeekBar.setProgress(30);
        smoothnessSeekBar.setProgress(50);
        brightnessSeekBar.setProgress(50);
        contrastSeekBar.setProgress(50);
        saturationSeekBar.setProgress(50);
        linearRadio.setChecked(true);
    }

    private void disableButtons(boolean d) {
        applyFpsButton.setEnabled(!d);
        applyInterpolationButton.setEnabled(!d);
        applyAdjustmentsButton.setEnabled(!d);
        saveButton.setEnabled(!d);
        exportButton.setEnabled(!d);
    }
    private void toast(String t){ Toast.makeText(this,t,Toast.LENGTH_SHORT).show(); }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();

    }
}