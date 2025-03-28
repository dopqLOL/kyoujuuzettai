package com.example.contentful_javasilver.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface QuizDao {
    @Query("SELECT * FROM quiz_table")
    List<QuizEntity> getAll();

    @Query("SELECT * FROM quiz_table WHERE qid = :qid")
    QuizEntity getByQid(String qid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<QuizEntity> quizzes);

    @Query("DELETE FROM quiz_table")
    void deleteAll();
} 