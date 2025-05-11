package com.example.final_smd.utilis;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("enhance_prompt/")
    Call<PromptEnhanceResponse> enhancePrompt(@Body PromptEnhanceRequest body);

    @POST("generate_image_flux/")
    Call<GenerateImageResponse> generateImage(@Body GenerateImageRequest body);

    @GET("image_task/{task_id}")
    Call<TaskStatusResponse> getTaskStatus(@Path("task_id") String taskId);
}

