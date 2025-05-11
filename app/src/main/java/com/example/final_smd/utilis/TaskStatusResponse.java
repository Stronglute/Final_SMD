package com.example.final_smd.utilis;

public class TaskStatusResponse {
    // present only when completed
    Integer code;
    String  message;
    public Data    data;

    // present during queued/pending
    public String  status;   // "pending", "running" etc.
    String  task_id;

    public static class Data {             // nullable!
        public String status;
        String task_id;
        public Output output;
        public static class Output { public String image_url; }
    }
}


