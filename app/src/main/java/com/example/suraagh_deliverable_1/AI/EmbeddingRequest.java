package com.example.suraagh_deliverable_1.AI;

public class EmbeddingRequest {
    // REMOVE THIS LINE:
    // public String model = "models/text-embedding-004";

    public Content content;

    public EmbeddingRequest(String text) {
        this.content = new Content();
        this.content.parts = new Part[]{ new Part(text) };
    }

    public static class Content {
        public Part[] parts;
    }

    public static class Part {
        public String text;
        public Part(String text) { this.text = text; }
    }
}