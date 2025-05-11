package com.example.final_smd;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_smd.utilis.*;          // ApiClient / ApiService / DTOs
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoGenerationActivity extends AppCompatActivity {

    private TextInputEditText promptInput;
    private Button generateButton, editButton, exportButton;
    private ProgressBar progressBar;
    private TextView statusText, previewPlaceholder;
    private VideoView videoPreview;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ApiService api;
    private Uri generatedVideoUri;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_video_generation);

        api = ApiClient.get().create(ApiService.class);

        promptInput        = findViewById(R.id.prompt_input);
        generateButton     = findViewById(R.id.btn_generate);
        editButton         = findViewById(R.id.btn_edit_video);
        exportButton       = findViewById(R.id.btn_export_video);
        progressBar        = findViewById(R.id.progress_bar);
        statusText         = findViewById(R.id.text_status);
        previewPlaceholder = findViewById(R.id.text_preview_placeholder);
        videoPreview       = findViewById(R.id.video_preview);

        editButton.setEnabled(false);    // disabled until video ready
        exportButton.setEnabled(false);

        generateButton.setOnClickListener(v -> startGeneration());
        editButton.setOnClickListener(v -> launchEditor());
        exportButton.setOnClickListener(v -> launchExporter());
    }

    /* ───────────────────────────── generation flow ────────────────────────── */

    private void startGeneration() {
        String prompt = promptInput.getText().toString().trim();
        if (prompt.isEmpty()) { promptInput.setError("Enter a prompt first"); return; }

        setUiLoading(true, "Submitting job…");

        api.generateVideo(new GenerateVideoRequest(prompt))
                .enqueue(new Callback<GenerateVideoResponse>() {
                    @Override public void onResponse(Call<GenerateVideoResponse> c,
                                                     Response<GenerateVideoResponse> r) {
                        if (r.isSuccessful() && r.body()!=null) {
                            pollTask(r.body().task_id);
                        } else {
                            setUiLoading(false, "Job submission failed ("+r.code()+")");
                        }
                    }
                    @Override public void onFailure(Call<GenerateVideoResponse> c, Throwable t) {
                        setUiLoading(false, "Network error: "+t.getMessage());
                    }
                });
    }

    /** Polls every 30 s until Pi marks the task “completed” or “failed”. */
    private void pollTask(String taskId) {
        executor.execute(() -> {
            try {
                while (true) {
                    Response<VideoTaskStatusResponse> r =
                            api.getVideoTaskStatus(taskId).execute();

                    if (!r.isSuccessful() || r.body()==null) {
                        runOnUiThread(() ->
                                setUiLoading(false, "Status error: "+r.code()));
                        return;
                    }

                    VideoTaskStatusResponse body = r.body();
                    String status;
                    String videoUrl = null;

                    if (body.data!=null && body.data.status!=null) {
                        status = body.data.status.toLowerCase();
                        if (status.equals("completed") || status.equals("success")) {
                            videoUrl = body.data.output.video_url;
                        }
                    } else {
                        status = body.status.toLowerCase(); // pending/running
                    }

                    switch (status) {
                        case "completed":
                        case "success":
                            String finalUrl = videoUrl;
                            runOnUiThread(() -> onVideoReady(finalUrl));
                            return;

                        case "failed":
                        case "error":
                            runOnUiThread(() ->
                                    setUiLoading(false,"Generation failed ❌"));
                            return;

                        default: // pending / running
                            runOnUiThread(() ->
                                    statusText.setText("Processing… ("+status+")"));
                            Thread.sleep(50_000);        // 30‑second interval
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        setUiLoading(false, "Polling error: "+e.getMessage()));
            }
        });
    }

    /* ───────────────────────────── UI helpers ─────────────────────────────── */

    private void setUiLoading(boolean loading, String msg) {
        progressBar.setIndeterminate(loading);
        progressBar.setVisibility(loading? View.VISIBLE: View.GONE);
        statusText.setText(msg);
        generateButton.setEnabled(!loading);
    }

    private void onVideoReady(String url) {
        setUiLoading(false, "Video ready! ▶");
        previewPlaceholder.setVisibility(View.GONE);
        videoPreview.setVisibility(View.VISIBLE);

        generatedVideoUri = Uri.parse(url);

        /* ──  A. Attach playback controls  ─────────────────────────────────── */
        MediaController mc = new MediaController(this);
        mc.setAnchorView(videoPreview);
        videoPreview.setMediaController(mc);

        videoPreview.setVideoURI(generatedVideoUri);

        /* ──  B. Loop playback once video is prepared  ─────────────────────── */
        videoPreview.setOnPreparedListener(mp -> {
            mp.setLooping(true);   // infinite loop
            videoPreview.start();  // auto‑play
        });

        editButton.setEnabled(true);
        exportButton.setEnabled(true);
    }


    /* ───────────────────────────── navigation ─────────────────────────────── */

    private void launchEditor() {
        if (generatedVideoUri==null) return;
        Intent i = new Intent(this, VideoEditingActivity.class);
        i.putExtra("VIDEO_URI", generatedVideoUri.toString());
        startActivity(i);
    }
    private void launchExporter() {
        if (generatedVideoUri==null) return;
        Intent i = new Intent(this, VideoExportActivity.class);
        i.putExtra("VIDEO_URI", generatedVideoUri.toString());
        startActivity(i);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
