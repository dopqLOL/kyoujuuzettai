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

    @Query("SELECT * FROM quizzes WHERE rowid IN (SELECT rowid FROM quizzes ORDER BY RANDOM() LIMIT :count)")
    LiveData<List<QuizEntity>> getRandomQuizzes(int count);

    @Query("SELECT * FROM quizzes WHERE rowid IN (SELECT rowid FROM quizzes ORDER BY RANDOM() LIMIT :count)")
    List<QuizEntity> getRandomQuizzesSync(int count);

    @Query("SELECT COUNT(*) FROM quizzes")
    LiveData<Integer> getQuizCount();

    @Query("SELECT COUNT(*) FROM quizzes")
    int getQuizCountSync();

    @Query("SELECT * FROM quizzes WHERE category = :category ORDER BY qid ASC")
    LiveData<List<QuizEntity>> getQuizzesByCategory(String category);

    @Query("SELECT * FROM quizzes WHERE category = :category ORDER BY RANDOM()") // カテゴリで同期的に取得し、ランダムに並び替え
    List<QuizEntity> getQuizzesByCategorySync(String category);

    @Query("SELECT * FROM quizzes WHERE category = :category ORDER BY RANDOM() LIMIT :count") // カテゴリでフィルタし、ランダムに指定件数取得
    List<QuizEntity> getRandomQuizzesByCategorySync(String category, int count);

    @Query("SELECT * FROM quizzes WHERE qid = :qid")
    LiveData<List<QuizEntity>> getQuizzesByQidLive(String qid);

    @Query("SELECT * FROM quizzes WHERE chapter = :chapter AND category = :category")
    LiveData<List<QuizEntity>> getQuizzesByChapterAndCategory(String chapter, String category);

    @Query("SELECT * FROM quizzes WHERE qid = :qid")
    List<QuizEntity> getQuizzesByQid(String qid);

    @Query("DELETE FROM quizzes")
    void deleteAll();
}
