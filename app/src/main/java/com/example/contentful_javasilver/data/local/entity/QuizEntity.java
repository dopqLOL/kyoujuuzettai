package com.example.contentful_javasilver.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;
import com.example.contentful_javasilver.data.local.Converters;
import java.util.List;

@Entity(tableName = "quizzes")
@TypeConverters(Converters.class)
public class QuizEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "qid")
    public String qid;

    @ColumnInfo(name = "chapter")
    public String chapter;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "question_category")
    public String questionCategory;

    @ColumnInfo(name = "difficulty")
    public String difficulty;

    @ColumnInfo(name = "question_text")
    public String questionText;

    @ColumnInfo(name = "choices")
    public List<String> choices;

    @ColumnInfo(name = "answer")
    public List<String> answer;

    @ColumnInfo(name = "explanation")
    public String explanation;

    public QuizEntity(
        @NonNull String qid,
        String chapter,
        String category,
        String questionCategory,
        String difficulty,
        String questionText,
        List<String> choices,
        List<String> answer,
        String explanation
    ) {
        this.qid = qid;
        this.chapter = chapter;
        this.category = category;
        this.questionCategory = questionCategory;
        this.difficulty = difficulty;
        this.questionText = questionText;
        this.choices = choices;
        this.answer = answer;
        this.explanation = explanation;
    }
} 