package com.example.suraagh_deliverable_1.AI;

import java.util.List;

public class EmbeddingResponse {
    public EmbeddingValues embedding;

    public static class EmbeddingValues {
        public List<Double> values; // This is the vector!
    }
}
