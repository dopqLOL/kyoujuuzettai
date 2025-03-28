package com.example.contentful_javasilver.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QuizEntity> quizzes);

    @Query("SELECT * FROM quizzes")
    List<QuizEntity> getAllQuizzes();

    @Query("SELECT * FROM quizzes ORDER BY RANDOM() LIMIT :count")
    LiveData<List<QuizEntity>> getRandomQuizzes(int count);

    @Query("SELECT * FROM quizzes ORDER BY RANDOM() LIMIT :count")
    List<QuizEntity> getRandomQuizzesSync(int count);

    @Query("SELECT COUNT(*) FROM quizzes")
    LiveData<Integer> getQuizCount();

    @Query("SELECT COUNT(*) FROM quizzes")
    int getQuizCountSync();
} 