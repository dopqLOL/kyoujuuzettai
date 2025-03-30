package com.example.contentful_javasilver

import android.util.Log
import kotlinx.coroutines.*
import com.example.contentful_javasilver.data.QuizDao
import kotlin.Unit

class DatabaseHelperCoroutines {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "DatabaseHelper"

    fun loadCategoriesAsync(
        chapterNumber: Int,
        quizDao: QuizDao,
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                Log.d(TAG, "Loading categories for chapter: $chapterNumber")
                val quizzes = quizDao.getAllQuizzes()
                Log.d(TAG, "Total quizzes in database: ${quizzes.size}")
                
                // まずはデータベース内のすべての章番号形式を確認
                val chapterFormats = quizzes.mapNotNull { it.chapter }.distinct()
                Log.d(TAG, "Chapter formats in database: $chapterFormats")
                
                // この章に該当する問題を特定
                val matchingQuizzes = quizzes.filter { quiz ->
                    // null/空チェック
                    if (quiz.chapter.isNullOrBlank() || quiz.category.isNullOrBlank()) {
                        Log.d(TAG, "Skipping quiz with null/empty chapter or category: qid=${quiz.qid}")
                        return@filter false
                    }
                    
                    // 章番号のパターンマッチング
                    val isMatchingChapter = when {
                        // 完全一致: "1", "2", etc.
                        quiz.chapter == chapterNumber.toString() -> true
                        
                        // "X章" パターン: "1章", "2章", etc.
                        quiz.chapter == "${chapterNumber}章" -> true
                        
                        // 数値のみを抽出して比較
                        else -> {
                            val chapterNum = extractNumberFromString(quiz.chapter)
                            chapterNum == chapterNumber
                        }
                    }
                    
                    if (isMatchingChapter) {
                        Log.d(TAG, "Found matching quiz: qid=${quiz.qid}, chapter=${quiz.chapter}, category=${quiz.category}")
                    }
                    
                    isMatchingChapter
                }
                
                Log.d(TAG, "Found ${matchingQuizzes.size} quizzes for chapter $chapterNumber")
                
                // カテゴリの抽出（非nullのみ）
                val categories = matchingQuizzes
                    .mapNotNull { it.category }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                
                Log.d(TAG, "Extracted ${categories.size} distinct categories for chapter $chapterNumber: $categories")
                
                withContext(Dispatchers.Main) {
                    if (categories.isEmpty()) {
                        Log.w(TAG, "No categories found for chapter $chapterNumber")
                    }
                    onSuccess(categories)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories", e)
                withContext(Dispatchers.Main) {
                    onError("カテゴリーの読み込みに失敗しました: ${e.message}")
                }
            }
        }
    }

    /**
     * 文字列から数値を抽出するヘルパーメソッド
     */
    private fun extractNumberFromString(str: String?): Int {
        if (str.isNullOrBlank()) return -1
        
        return try {
            // 1. 数字のみの場合
            if (str.matches(Regex("^\\d+$"))) {
                return str.toInt()
            }
            
            // 2. "X章" パターンの場合
            if (str.contains("章")) {
                val number = str.replace("章", "").trim()
                if (number.matches(Regex("^\\d+$"))) {
                    return number.toInt()
                }
            }
            
            // 3. その他のケース: 数字以外の文字を除去
            val numberOnly = str.replace(Regex("[^0-9]"), "")
            if (numberOnly.isNotEmpty()) {
                numberOnly.toInt()
            } else {
                -1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting number from $str", e)
            -1
        }
    }

    fun getQuizCountForCategoryAsync(
        category: String,
        quizDao: QuizDao,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val quizzes = quizDao.getAllQuizzes()
                val count = quizzes.count { quiz -> quiz.category == category }
                withContext(Dispatchers.Main) {
                    onSuccess(count)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError("問題数の取得に失敗しました")
                }
            }
        }
    }

    fun loadQuestionCategoriesAsync(
        category: String,
        quizDao: QuizDao,
        onSuccess: (List<QuestionCategoryItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val quizzes = quizDao.getAllQuizzes()
                val questionCategories = quizzes
                    .filter { it.category == category }
                    .map { QuestionCategoryItem(it.qid, it.questionCategory) }
                    .distinctBy { it.questionCategory }
                    .sortedBy { it.qid }

                withContext(Dispatchers.Main) {
                    onSuccess(questionCategories)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("問題カテゴリーの読み込みに失敗しました")
                }
            }
        }
    }

    data class QuestionCategoryItem(
        val qid: String,
        val questionCategory: String
    )

    fun cleanup() {
        scope.cancel()
    }
} 