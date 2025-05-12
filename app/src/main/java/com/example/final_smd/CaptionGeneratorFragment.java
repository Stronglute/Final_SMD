// CaptionGeneratorFragment.java
package com.example.final_smd;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptionGeneratorFragment extends Fragment {

    private TextInputEditText transcriptEditText;
    private AutoCompleteTextView formatAutoCompleteTextView;
    private CheckBox timestampCheckBox, emojiCheckBox;
    private Button generateCaptionsButton;
    private MaterialCardView captionsOutputCardView;
    private RecyclerView captionsRecyclerView;
    private Button exportCaptionsButton, copyCaptionsButton;
    private ProgressBar captionProgressBar;

    private ExecutorService executorService;
    private List<String> captionsList;
    private CaptionsAdapter captionsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_caption_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        transcriptEditText          = view.findViewById(R.id.transcriptEditText);
        formatAutoCompleteTextView  = view.findViewById(R.id.formatAutoCompleteTextView);
        timestampCheckBox           = view.findViewById(R.id.timestampCheckBox);
        emojiCheckBox               = view.findViewById(R.id.emojiCheckBox);
        generateCaptionsButton      = view.findViewById(R.id.generateCaptionsButton);
        captionsOutputCardView      = view.findViewById(R.id.captionsOutputCardView);
        captionsRecyclerView        = view.findViewById(R.id.captionsRecyclerView);
        exportCaptionsButton        = view.findViewById(R.id.exportCaptionsButton);
        copyCaptionsButton          = view.findViewById(R.id.copyCaptionsButton);
        captionProgressBar          = view.findViewById(R.id.captionProgressBar);

        setupFormatDropdown();
        setupButtons();

        executorService = Executors.newSingleThreadExecutor();
        captionsList    = new ArrayList<>();
        captionsAdapter = new CaptionsAdapter(captionsList);

        captionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        captionsRecyclerView.setAdapter(captionsAdapter);
    }

    private void setupFormatDropdown() {
        String[] formats = {"SRT", "VTT", "Plain Text"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                formats
        );
        formatAutoCompleteTextView.setAdapter(adapter);
    }

    private void setupButtons() {


        generateCaptionsButton.setOnClickListener(v -> {
            if (validateInputs()) {
                generateCaptions();
            }
        });

        copyCaptionsButton.setOnClickListener(v -> {
            if (!captionsList.isEmpty()) {
                String all = String.join("\n", captionsList);
                ClipboardManager clipboard =
                        (ClipboardManager) requireContext()
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Captions", all);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(),
                        "Captions copied to clipboard",
                        Toast.LENGTH_SHORT).show();
            }
        });

        exportCaptionsButton.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Export feature not implemented yet",
                        Toast.LENGTH_SHORT).show()
        );
    }

    private boolean validateInputs() {
        boolean valid = true;
        if (transcriptEditText.getText().toString().trim().isEmpty()) {
            transcriptEditText.setError("Transcript is required");
            valid = false;
        }
        if (formatAutoCompleteTextView.getText().toString().trim().isEmpty()) {
            formatAutoCompleteTextView.setError("Select a format");
            valid = false;
        }
        return valid;
    }

    private void generateCaptions() {
        captionProgressBar.setVisibility(View.VISIBLE);
        generateCaptionsButton.setEnabled(false);
        captionsList.clear();
        captionsAdapter.notifyDataSetChanged();

        String transcript = transcriptEditText.getText().toString().trim();
        boolean wantTimestamps = timestampCheckBox.isChecked();
        boolean wantEmojis     = emojiCheckBox.isChecked();
        String format         = formatAutoCompleteTextView.getText().toString().trim();

        executorService.execute(() -> {
            // Simulate work
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

            // Naive split by sentences for demo
            String[] sentences = transcript.split("(?<=[.!?])\\s+");
            int counter = 1;
            for (String s : sentences) {
                StringBuilder cap = new StringBuilder();
                if (wantTimestamps) {
                    // Dummy timestamp
                    cap.append(String.format("[%02d:%02d:%02d] ", 0, 0, counter * 5));
                }
                cap.append(s.trim());
                if (wantEmojis) {
                    cap.append(" 😊");
                }
                captionsList.add(cap.toString());
                counter++;
            }

            requireActivity().runOnUiThread(() -> {
                captionProgressBar.setVisibility(View.GONE);
                generateCaptionsButton.setEnabled(true);
                captionsOutputCardView.setVisibility(View.VISIBLE);
                captionsAdapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onDestroyView() {
        executorService.shutdown();
        super.onDestroyView();
    }

    // --- RecyclerView Adapter for Captions ---
    private static class CaptionsAdapter
            extends RecyclerView.Adapter<CaptionsAdapter.ViewHolder> {

        private final List<String> items;

        CaptionsAdapter(List<String> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType
        ) {
            TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(
                @NonNull ViewHolder holder, int position
        ) {
            holder.captionView.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView captionView;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                captionView = (TextView) itemView;
            }
        }
    }
}
