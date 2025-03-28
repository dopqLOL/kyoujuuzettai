package com.example.contentful_javasilver

import kotlinx.coroutines.*
import com.example.contentful_javasilver.data.QuizDao
import kotlin.Unit

class DatabaseHelperCoroutines {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun loadCategoriesAsync(
        chapterNumber: Int,
        quizDao: QuizDao,
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val quizzes = quizDao.getAllQuizzes()
                val categories = mutableListOf<String>()
                
                for (quiz in quizzes) {
                    val quizChapter = quiz.chapter?.replace("章", "")?.toIntOrNull()
                    if (quizChapter == null) continue
                    
                    if (quizChapter == chapterNumber && !categories.contains(quiz.category)) {
                        categories.add(quiz.category)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    onSuccess(categories)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("カテゴリーの読み込みに失敗しました")
                }
            }
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

    fun cleanup() {
        scope.cancel()
    }
} 