package com.example.final_smd;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ImageGenerationActivity extends AppCompatActivity {

    private EditText promptEditText;
    private Button generateButton;
    private ImageView generatedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_generation);

        promptEditText = findViewById(R.id.promptEditText);
        generateButton = findViewById(R.id.generateButton);
        generatedImageView = findViewById(R.id.generatedImageView);

        generateButton.setOnClickListener(v -> {
            String prompt = promptEditText.getText().toString();
            if (!prompt.isEmpty()) {
                generateImage(prompt);
            } else {
                Toast.makeText(this, "Please enter a prompt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateImage(String prompt) {
        // Call your backend API to generate the image
        // Example: Send the prompt to backend and get image URL in response
        String imageUrl = "https://example.com/generated_image.jpg"; // Placeholder for backend response
        Picasso.get().load(imageUrl).into(generatedImageView);
    }
}
