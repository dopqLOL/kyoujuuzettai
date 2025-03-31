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

    /**
     * 全てのクイズを章番号（数値として）とQID（文字列として）でソートして取得します。
     * @return ソートされたクイズエンティティのリスト
     */
    @Query("SELECT * FROM quizzes ORDER BY CAST(chapter AS INTEGER) ASC, qid ASC")
    List<QuizEntity> getAllQuizzesSorted();

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

    // --- Methods for QuizHistory ---

    /**
     * Inserts a single quiz answer history record.
     * @param history The QuizHistory object to insert.
     */
    @Insert
    void insertHistory(QuizHistory history);

    /**
     * Retrieves all quiz history records, ordered by timestamp descending (newest first).
     * @return A LiveData list of all QuizHistory records.
     */
    @Query("SELECT * FROM quiz_history ORDER BY timestamp DESC")
    LiveData<List<QuizHistory>> getAllHistorySortedByTimestampDesc();

    /**
     * Retrieves statistics for each problem (qid) based on the quiz history.
     * Calculates the count of correct and incorrect answers for each problemId.
     * @return A LiveData list of ProblemStats objects, ordered by problemId.
     */
    @Query("SELECT problemId, " +
           "SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correctCount, " +
           "SUM(CASE WHEN isCorrect = 0 THEN 1 ELSE 0 END) as incorrectCount " +
           "FROM quiz_history GROUP BY problemId ORDER BY problemId ASC")
    LiveData<List<ProblemStats>> getProblemStatistics();

    // If needed, add methods to delete history, etc.
    // @Query("DELETE FROM quiz_history")
    // void deleteAllHistory();
}
