package com.example.final_smd;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScriptGeneratorFragment extends Fragment {

    private TextInputEditText topicEditText;
    private TextInputEditText audienceEditText;
    private TextInputEditText lengthEditText;
    private AutoCompleteTextView toneAutoCompleteTextView;
    private TextInputEditText keyPointsEditText;
    private Button generateScriptButton;
    private MaterialCardView scriptOutputCardView;
    private TextView generatedScriptTextView;
    private Button editScriptButton;
    private Button copyScriptButton;
    private Button convertToSpeechButton;
    private ProgressBar scriptProgressBar;

    private ExecutorService executorService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_script_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        topicEditText = view.findViewById(R.id.topicEditText);
        audienceEditText = view.findViewById(R.id.audienceEditText);
        lengthEditText = view.findViewById(R.id.lengthEditText);
        toneAutoCompleteTextView = view.findViewById(R.id.toneAutoCompleteTextView);
        keyPointsEditText = view.findViewById(R.id.keyPointsEditText);
        generateScriptButton = view.findViewById(R.id.generateScriptButton);
        scriptOutputCardView = view.findViewById(R.id.scriptOutputCardView);
        generatedScriptTextView = view.findViewById(R.id.generatedScriptTextView);
        editScriptButton = view.findViewById(R.id.editScriptButton);
        copyScriptButton = view.findViewById(R.id.copyScriptButton);
        convertToSpeechButton = view.findViewById(R.id.convertToSpeechButton);
        scriptProgressBar = view.findViewById(R.id.scriptProgressBar);

        // Setup tone dropdown
        setupToneDropdown();

        // Setup buttons
        setupButtons();

        // Initialize executor service for background tasks
        executorService = Executors.newSingleThreadExecutor();
    }

    private void setupToneDropdown() {
        String[] tones = {"Professional", "Casual", "Friendly", "Informative", "Humorous", "Serious", "Inspirational"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, tones);
        toneAutoCompleteTextView.setAdapter(adapter);
    }

    private void setupButtons() {
        generateScriptButton.setOnClickListener(v -> {
            if (validateInputs()) {
                generateScript();
            }
        });

        editScriptButton.setOnClickListener(v -> {
            // Make the script text editable
            generatedScriptTextView.setEnabled(true);
            Toast.makeText(requireContext(), "You can now edit the script", Toast.LENGTH_SHORT).show();
        });

        copyScriptButton.setOnClickListener(v -> {
            String scriptText = generatedScriptTextView.getText().toString();
            if (!scriptText.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Script", scriptText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "Script copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        convertToSpeechButton.setOnClickListener(v -> {
            String scriptText = generatedScriptTextView.getText().toString();
            if (!scriptText.isEmpty()) {
                // Navigate to Text-to-Speech activity with the script
                Intent intent = new Intent(requireContext(), TextToSpeechActivity.class);
                intent.putExtra("SCRIPT_TEXT", scriptText);
                startActivity(intent);
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (topicEditText.getText().toString().trim().isEmpty()) {
            topicEditText.setError("Topic is required");
            isValid = false;
        }

        if (audienceEditText.getText().toString().trim().isEmpty()) {
            audienceEditText.setError("Target audience is required");
            isValid = false;
        }

        if (lengthEditText.getText().toString().trim().isEmpty()) {
            lengthEditText.setError("Video length is required");
            isValid = false;
        } else {
            try {
                double length = Double.parseDouble(lengthEditText.getText().toString().trim());
                if (length <= 0 || length > 30) {
                    lengthEditText.setError("Length must be between 0.1 and 30 minutes");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                lengthEditText.setError("Please enter a valid number");
                isValid = false;
            }
        }

        if (toneAutoCompleteTextView.getText().toString().trim().isEmpty()) {
            toneAutoCompleteTextView.setError("Please select a tone");
            isValid = false;
        }

        return isValid;
    }

    private void generateScript() {
        scriptProgressBar.setVisibility(View.VISIBLE);
        generateScriptButton.setEnabled(false);

        String topic = topicEditText.getText().toString().trim();
        String audience = audienceEditText.getText().toString().trim();
        String lengthStr = lengthEditText.getText().toString().trim();
        String tone = toneAutoCompleteTextView.getText().toString().trim();
        String keyPoints = keyPointsEditText.getText().toString().trim();

        executorService.execute(() -> {
            // In a real app, this would call an API or more complex script generation logic
            // For demonstration, we'll create a simple script based on the inputs

            // Simulate network delay
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            double minutes = Double.parseDouble(lengthStr);
            int wordCount = (int) (minutes * 150); // Approximate 150 words per minute

            final String generatedScript = generateSampleScript(topic, audience, tone, keyPoints, wordCount);

            requireActivity().runOnUiThread(() -> {
                scriptProgressBar.setVisibility(View.GONE);
                generateScriptButton.setEnabled(true);

                generatedScriptTextView.setText(generatedScript);
                scriptOutputCardView.setVisibility(View.VISIBLE);
            });
        });
    }

    private String generateSampleScript(String topic, String audience, String tone, String keyPoints, int wordCount) {
        StringBuilder script = new StringBuilder();

        // Introduction
        script.append("## ")
                .append(topic)
                .append(" - Video Script\n\n");

        script.append("### Introduction\n\n");

        // Greeting based on tone
        if (tone.equalsIgnoreCase("Casual") || tone.equalsIgnoreCase("Friendly")) {
            script.append("Hey everyone! Welcome back to the channel. ");
        } else if (tone.equalsIgnoreCase("Professional") || tone.equalsIgnoreCase("Serious")) {
            script.append("Hello and welcome. Today we'll be discussing an important topic. ");
        } else if (tone.equalsIgnoreCase("Humorous")) {
            script.append("Well hello there beautiful people! Buckle up because we're about to dive into ");
        } else {
            script.append("Welcome to this video about ");
        }

        script.append("Today we're talking all about ")
                .append(topic)
                .append(". ");

        script.append("This video is specifically designed for ")
                .append(audience)
                .append(".\n\n");

        // Main content - process key points if provided
        script.append("### Main Content\n\n");

        if (!keyPoints.isEmpty()) {
            String[] points = keyPoints.split(",");
            for (String point : points) {
                script.append("#### ").append(point.trim()).append("\n\n");

                // Add dummy content for each point
                script.append("When we consider ").append(point.trim()).append(", we need to understand its importance in the context of ")
                        .append(topic).append(". ");

                // Add a few more sentences to make it look realistic
                script.append("There are several aspects to consider here. First, remember that your audience of ")
                        .append(audience).append(" will be particularly interested in this. ");

                script.append("Let's break this down further into actionable insights you can implement right away.\n\n");
            }
        } else {
            // Generic content if no key points provided
            script.append("There are several key aspects of ").append(topic).append(" that we need to discuss today. ");
            script.append("First, let's examine the core concepts and why they matter to ").append(audience).append(". ");
            script.append("Understanding these fundamentals will help you grasp the more advanced ideas later on.\n\n");

            script.append("#### Key Insights\n\n");
            script.append("As we explore ").append(topic).append(", keep in mind these important takeaways that will benefit you as ")
                    .append(audience).append(".\n\n");
        }

        // Conclusion
        script.append("### Conclusion\n\n");
        script.append("To summarize what we've covered about ").append(topic).append(": ");

        if (!keyPoints.isEmpty()) {
            String[] points = keyPoints.split(",");
            for (int i = 0; i < points.length; i++) {
                if (i > 0) {
                    script.append(", ");
                    if (i == points.length - 1) {
                        script.append("and ");
                    }
                }
                script.append(points[i].trim());
            }
            script.append(". ");
        } else {
            script.append("we've discussed the main concepts, practical applications, and key benefits. ");
        }

        script.append("I hope you found this information valuable.\n\n");

        // Call to action
        script.append("### Call to Action\n\n");

        if (tone.equalsIgnoreCase("Casual") || tone.equalsIgnoreCase("Friendly") || tone.equalsIgnoreCase("Humorous")) {
            script.append("If you enjoyed this video, don't forget to smash that like button and subscribe for more content like this! ");
        } else {
            script.append("If you found this information helpful, please consider subscribing to the channel for more videos on related topics. ");
        }

        script.append("Leave a comment below with your thoughts or questions about ").append(topic).append(". ");
        script.append("Thanks for watching, and I'll see you in the next video!");

        return script.toString();
    }

    @Override
    public void onDestroy() {
        executorService.shutdown();
        super.onDestroy();
    }
}