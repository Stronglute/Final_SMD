package com.example.final_smd.utilis;

public class GenerateImageRequest {
    String prompt;
    int    width;
    int    height;
    float  guidance_scale;
    String style;

    public GenerateImageRequest(String p, int w, int h, float g, String s) {
        prompt = p; width = w; height = h; guidance_scale = g; style = s;
    }
}
