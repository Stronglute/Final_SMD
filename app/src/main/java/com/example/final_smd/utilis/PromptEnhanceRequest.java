package com.example.final_smd.utilis;

// === request bodies ===
public class PromptEnhanceRequest {
    String prompt;
    String style;
    int   max_words = 60;

    public PromptEnhanceRequest(String prompt, String style) {
        this.prompt = prompt;
        this.style  = style;
    }
}
