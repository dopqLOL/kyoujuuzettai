package com.example.contentful_javasilver.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "quiz_history")
public class QuizHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "problemId")
    public String problemId; // Matches QuizEntity ID type

    @ColumnInfo(name = "isCorrect")
    public boolean isCorrect;

    @ColumnInfo(name = "timestamp")
    public long timestamp; // Store time as milliseconds since epoch

    // Constructor
    public QuizHistory(@NonNull String problemId, boolean isCorrect, long timestamp) {
        this.problemId = problemId;
        this.isCorrect = isCorrect;
        this.timestamp = timestamp;
    }
}
