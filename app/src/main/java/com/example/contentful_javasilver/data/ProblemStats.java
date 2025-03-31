package com.example.contentful_javasilver.data;

import androidx.annotation.NonNull;

/**
 * Represents the statistics for a single problem (qid).
 */
public class ProblemStats {

    @NonNull
    public String problemId; // The ID of the problem (qid)

    public int correctCount; // Number of times answered correctly

    public int incorrectCount; // Number of times answered incorrectly

    // Room needs a constructor (even if empty) or public fields.
    // We use public fields here for simplicity as Room can directly map columns to them.
    // If you needed more complex logic or encapsulation, you'd use a constructor and getters.
    public ProblemStats(@NonNull String problemId, int correctCount, int incorrectCount) {
        this.problemId = problemId;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
    }

    // Optional: Method to calculate accuracy
    public double getAccuracy() {
        int total = correctCount + incorrectCount;
        if (total == 0) {
            return 0.0; // Avoid division by zero
        }
        return (double) correctCount / total * 100.0;
    }
}
