package com.example.contentful_javasilver.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;
import java.util.List;

@Entity(tableName = "quiz_table")
public class QuizEntity {
    @PrimaryKey
    @NonNull
    private String qid;
    private String chapter;
    private String category;
    private String questionCategory;
    private String difficulty;
    private String code;
    private String questionText;
    
    @TypeConverters(Converters.class)
    private List<String> choices;
    
    @TypeConverters(Converters.class)
    private List<Integer> answer;
    
    private String explanation;
    private long updatedAt;

    public QuizEntity(String qid, String chapter, String category, String questionCategory,
                     String difficulty, String code, String questionText,
                     List<String> choices, List<Integer> answer, String explanation) {
        this.qid = qid;
        this.chapter = chapter;
        this.category = category;
        this.questionCategory = questionCategory;
        this.difficulty = difficulty;
        this.code = code;
        this.questionText = questionText;
        this.choices = choices;
        this.answer = answer;
        this.explanation = explanation;
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getQid() { return qid; }
    public void setQid(@NonNull String qid) { this.qid = qid; }
    public String getChapter() { return chapter; }
    public void setChapter(String chapter) { this.chapter = chapter; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getQuestionCategory() { return questionCategory; }
    public void setQuestionCategory(String questionCategory) { this.questionCategory = questionCategory; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }
    public List<Integer> getAnswer() { return answer; }
    public void setAnswer(List<Integer> answer) { this.answer = answer; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
} 