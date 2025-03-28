package com.example.contentful_javasilver.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.contentful_javasilver.data.local.entity.QuizEntity;
import java.util.List;

@Dao
public interface QuizDao {
    @Query("SELECT * FROM quizzes")
    List<QuizEntity> getAllQuizzes();

    @Query("SELECT * FROM quizzes WHERE qid = :qid")
    QuizEntity getQuizById(String qid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuiz(QuizEntity quiz);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuizzes(List<QuizEntity> quizzes);

    @Update
    void updateQuiz(QuizEntity quiz);

    @Query("DELETE FROM quizzes")
    void deleteAllQuizzes();

    @Query("DELETE FROM quizzes WHERE qid = :qid")
    void deleteQuizById(String qid);
} 