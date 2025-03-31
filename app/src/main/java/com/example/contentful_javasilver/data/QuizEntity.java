package com.example.contentful_javasilver.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;
import java.util.List;
import androidx.room.Index;

@Entity(tableName = "quizzes", 
    indices = {
        @Index(value = {"category"}),
        @Index(value = {"chapter"}),
        @Index(value = {"questionCategory"})
    }
)
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

    // Type Converters for Room
    public static class Converters {
        @androidx.room.TypeConverter
        public static String fromStringList(List<String> value) {
            return value == null ? null : String.join(",", value);
        }

        @androidx.room.TypeConverter
        public static List<String> toStringList(String value) {
            return value == null ? null : java.util.Arrays.asList(value.split(","));
        }

        @androidx.room.TypeConverter
        public static String fromIntegerList(List<Integer> value) {
            if (value == null) return null;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < value.size(); i++) {
                sb.append(value.get(i));
                if (i < value.size() - 1) sb.append(",");
            }
            return sb.toString();
        }

        @androidx.room.TypeConverter
        public static List<Integer> toIntegerList(String value) {
            if (value == null) return null;
            List<Integer> list = new java.util.ArrayList<>();
            String[] parts = value.split(",");
            for (String part : parts) {
                list.add(Integer.parseInt(part.trim()));
            }
            return list;
        }
    }

    @Override
    public String toString() {
        return "QuizEntity{" +
                "qid='" + qid + '\'' +
                ", chapter='" + chapter + '\'' +
                ", category='" + category + '\'' +
                ", questionCategory='" + questionCategory + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizEntity that = (QuizEntity) o;
        // Compare all relevant fields for content equality
        return java.util.Objects.equals(qid, that.qid) &&
               java.util.Objects.equals(chapter, that.chapter) &&
               java.util.Objects.equals(category, that.category) &&
               java.util.Objects.equals(questionCategory, that.questionCategory) &&
               java.util.Objects.equals(difficulty, that.difficulty) &&
               java.util.Objects.equals(code, that.code) &&
               java.util.Objects.equals(questionText, that.questionText) &&
               java.util.Objects.equals(choices, that.choices) &&
               java.util.Objects.equals(answer, that.answer) &&
               java.util.Objects.equals(explanation, that.explanation);
        // updatedAt might change, so it's often excluded from equals/hashCode
    }

    @Override
    public int hashCode() {
        // Generate hash code based on the same fields used in equals
        return java.util.Objects.hash(qid, chapter, category, questionCategory, difficulty, code, questionText, choices, answer, explanation);
    }
}
