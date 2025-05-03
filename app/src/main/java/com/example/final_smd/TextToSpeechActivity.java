package com.example.final_smd;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TextToSpeechActivity extends AppCompatActivity {

    private EditText inputTextEditText;
    private Spinner voiceSpinner;
    private SeekBar speedSeekBar;
    private SeekBar pitchSeekBar;
    private TextView speedValueTextView;
    private TextView pitchValueTextView;
    private Button previewButton;
    private Button generateButton;
    private Button saveButton;
    private MaterialCardView outputCardView;
    private ProgressBar progressBar;
    private ImageButton playPauseButton;
    private SeekBar audioSeekBar;
    private TextView durationTextView;

    private TextToSpeech textToSpeech;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable updateSeekBarRunnable;

    private List<Voice> availableVoices;
    private float speechRate = 1.0f;
    private float speechPitch = 1.0f;
    private String generatedAudioPath;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);

        // Initialize UI components
        inputTextEditText = findViewById(R.id.inputTextEditText);
        voiceSpinner = findViewById(R.id.voiceSpinner);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        pitchSeekBar = findViewById(R.id.pitchSeekBar);
        speedValueTextView = findViewById(R.id.speedValueTextView);
        pitchValueTextView = findViewById(R.id.pitchValueTextView);
        previewButton = findViewById(R.id.previewButton);
        generateButton = findViewById(R.id.generateButton);
        saveButton = findViewById(R.id.saveButton);
        outputCardView = findViewById(R.id.outputCardView);
        progressBar = findViewById(R.id.progressBar);
        playPauseButton = findViewById(R.id.playPauseButton);
        audioSeekBar = findViewById(R.id.audioSeekBar);
        durationTextView = findViewById(R.id.durationTextView);

        // Initialize TextToSpeech
        initializeTextToSpeech();

        // Setup SeekBars
        setupSeekBars();

        // Setup Buttons
        setupButtons();

        // Initialize MediaPlayer
        handler = new Handler();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
                } else {
                    // Populate available voices
                    populateVoices();
                }
            } else {
                Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                runOnUiThread(() -> {
                    if (utteranceId.equals("PREVIEW")) {
                        // Handle preview start
                    }
                });
            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> {
                    if (utteranceId.equals("GENERATE")) {
                        progressBar.setVisibility(View.GONE);
                        outputCardView.setVisibility(View.VISIBLE);
                        setupMediaPlayer();
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(TextToSpeechActivity.this, "Error generating speech", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void populateVoices() {
        Set<Voice> voices = textToSpeech.getVoices();
        if (voices != null && !voices.isEmpty()) {
            availableVoices = new ArrayList<>();
            List<String> voiceNames = new ArrayList<>();

            for (Voice voice : voices) {
                // Filter to show only English voices
                if (voice.getLocale().getLanguage().equals("en")) {
                    availableVoices.add(voice);
                    voiceNames.add(voice.getName());
                }
            }

            if (!availableVoices.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, voiceNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                voiceSpinner.setAdapter(adapter);

                voiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        textToSpeech.setVoice(availableVoices.get(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });
            }
        }
    }

    private void setupSeekBars() {
        // Speed SeekBar
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speechRate = 0.5f + (progress / 100f) * 1.5f; // Range from 0.5 to 2.0
                speedValueTextView.setText(String.format("%.1fx", speechRate));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Pitch SeekBar
        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speechPitch = 0.5f + (progress / 100f) * 1.5f; // Range from 0.5 to 2.0
                pitchValueTextView.setText(String.format("%.1f", speechPitch));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Audio SeekBar
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    updateDurationText(progress, mediaPlayer.getDuration());
                }
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
        // Preview Button
        previewButton.setOnClickListener(v -> {
            String text = inputTextEditText.getText().toString().trim();
            if (!text.isEmpty()) {
                stopPlayback();
                textToSpeech.setSpeechRate(speechRate);
                textToSpeech.setPitch(speechPitch);

                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "PREVIEW");
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
            } else {
                Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
            }
        });

        // Generate Button
        generateButton.setOnClickListener(v -> {
            String text = inputTextEditText.getText().toString().trim();
            if (!text.isEmpty()) {
                generateSpeech(text);
            } else {
                Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
            }
        });

        // Play/Pause Button
        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (isPlaying) {
                    mediaPlayer.pause();
                    playPauseButton.setImageResource(R.drawable.ic_play);
                    handler.removeCallbacks(updateSeekBarRunnable);
                } else {
                    mediaPlayer.start();
                    playPauseButton.setImageResource(R.drawable.ic_pause);
                    updateSeekBar();
                }
                isPlaying = !isPlaying;
            }
        });

        // Save Button
        saveButton.setOnClickListener(v -> {
            if (generatedAudioPath != null) {
                File sourceFile = new File(generatedAudioPath);
                File destDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC), "VoiceApp");

                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                String fileName = "speech_" + System.currentTimeMillis() + ".wav";
                File destFile = new File(destDir, fileName);

                try {
                    copyFile(sourceFile, destFile);
                    Toast.makeText(this, "Saved to Music/VoiceApp/" + fileName, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to save audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void generateSpeech(String text) {
        progressBar.setVisibility(View.VISIBLE);
        stopPlayback();

        // Create output file
        File outputDir = getExternalCacheDir();
        File outputFile;
        try {
            outputFile = File.createTempFile("speech_", ".wav", outputDir);
            generatedAudioPath = outputFile.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(this, "Cannot create output file", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        textToSpeech.setSpeechRate(speechRate);
        textToSpeech.setPitch(speechPitch);

        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "GENERATE");

        // Synthesize to file
        int result = textToSpeech.synthesizeToFile(text, params, String.valueOf(outputFile));
        if (result != TextToSpeech.SUCCESS) {
            Toast.makeText(this, "Error generating speech file", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupMediaPlayer() {
        if (generatedAudioPath != null) {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(generatedAudioPath);
                mediaPlayer.prepare();

                mediaPlayer.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    playPauseButton.setImageResource(R.drawable.ic_play);
                    handler.removeCallbacks(updateSeekBarRunnable);
                });

                audioSeekBar.setMax(mediaPlayer.getDuration());
                updateDurationText(0, mediaPlayer.getDuration());

            } catch (IOException e) {
                Toast.makeText(this, "Error playing audio: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            audioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            updateDurationText(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());

            if (mediaPlayer.isPlaying()) {
                updateSeekBarRunnable = this::updateSeekBar;
                handler.postDelayed(updateSeekBarRunnable, 100);
            }
        }
    }

    private void updateDurationText(int currentPosition, int duration) {
        String currentTime = formatTime(currentPosition);
        String totalTime = formatTime(duration);
        durationTextView.setText(String.format("%s / %s", currentTime, totalTime));
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void stopPlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            playPauseButton.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            java.io.FileInputStream in = new java.io.FileInputStream(source);
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        stopPlayback();
        super.onDestroy();
    }
}