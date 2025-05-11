package com.example.final_smd.utilis;

public class VideoTaskStatusResponse {
    // when pending/running
    public String status;      // "pending", "running", …
    String task_id;

    // when completed/failed Pi returns the big envelope
    Integer code;       // 200
    public Data data;

    public static class Data {
        public String status;          // "completed"
        public Output output;
        public static class Output { public String video_url; }
    }
}
