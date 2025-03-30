package com.example.contentful_javasilver

import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.CDAClient
import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import com.example.contentful_javasilver.data.QuizDatabase
import com.example.contentful_javasilver.data.QuizEntity
import com.example.contentful_javasilver.data.QuizDao
import kotlin.Unit
import android.util.Log

class AsyncHelperCoroutines(private val api: ContentfulGetApi?) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // エラーメッセージの定数
    companion object {
        private const val ERROR_NETWORK = "ネットワークエラーが発生しました。インターネット接続を確認してください。"
        private const val ERROR_TIMEOUT = "通信がタイムアウトしました。もう一度お試しください。"
        private const val ERROR_SERVER = "サーバーエラーが発生しました。しばらく時間をおいて再度お試しください。"
        private const val ERROR_UNKNOWN = "予期せぬエラーが発生しました。"
    }

    // エラーハンドリング用の関数
    private fun handleError(e: Exception): String {
        return when (e) {
            is IOException -> ERROR_NETWORK
            is SocketTimeoutException -> ERROR_TIMEOUT
            is UnknownHostException -> ERROR_NETWORK
            else -> ERROR_UNKNOWN
        }
    }

    // 🔹 コールバック方式（Java から簡単に呼び出せる）
    fun fetchEntriesAsync(contentType: String, callback: (List<CDAEntry>) -> Unit, errorCallback: (String) -> Unit) {
        requireNotNull(api) { "API instance is required for this operation" }
        scope.launch {
            try {
                val result = api?.fetchEntries(contentType)?.items()?.map { it as CDAEntry } ?: emptyList()
                withContext(Dispatchers.Main) {
                    callback(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    errorCallback(handleError(e))
                }
            }
        }
    }

    // 🔹 CompletableFuture 方式（Java からも扱いやすい）
    fun fetchEntriesFuture(contentType: String): CompletableFuture<List<CDAEntry>> {
        requireNotNull(api) { "API instance is required for this operation" }
        val future = CompletableFuture<List<CDAEntry>>()
        scope.launch {
            try {
                val result = api?.fetchEntries(contentType)?.items()?.map { it as CDAEntry } ?: emptyList()
                future.complete(result)
            } catch (e: Exception) {
                e.printStackTrace()
                future.completeExceptionally(e)
            }
        }
        return future
    }

    // 🔹 特定のエントリを非同期取得（コールバック）
    fun fetchEntryByIdAsync(entryId: String, callback: (CDAEntry?) -> Unit, errorCallback: (String) -> Unit) {
        requireNotNull(api) { "API instance is required for this operation" }
        scope.launch {
            try {
                val result = api?.fetchEntryById(entryId)
                withContext(Dispatchers.Main) {
                    callback(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    errorCallback(handleError(e))
                    callback(null)
                }
            }
        }
    }

    // 🔹 CompletableFuture 方式で特定のエントリを取得
    fun fetchEntryByIdFuture(entryId: String): CompletableFuture<CDAEntry?> {
        requireNotNull(api) { "API instance is required for this operation" }
        val future = CompletableFuture<CDAEntry?>()
        scope.launch {
            try {
                val result = api?.fetchEntryById(entryId)
                future.complete(result)
            } catch (e: Exception) {
                e.printStackTrace()
                future.completeExceptionally(e)
            }
        }
        return future
    }

    // 🔹 データベース操作用のメソッド
    fun insertQuizEntitiesAsync(db: QuizDatabase, entities: List<QuizEntity>, onSuccess: Function0<Unit>, onError: Function1<String, Unit>) {
        scope.launch {
            try {
                db.quizDao().insertAll(entities)
                withContext(Dispatchers.Main) {
                    onSuccess.invoke()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError.invoke("データベースの更新に失敗しました")
                }
            }
        }
    }

    fun getRandomQuizzesAsync(db: QuizDatabase, count: Int, onSuccess: Function1<List<QuizEntity>, Unit>, onError: Function1<String, Unit>) {
        scope.launch {
            try {
                val quizzes = db.quizDao().getRandomQuizzesSync(count)
                withContext(Dispatchers.Main) {
                    onSuccess.invoke(quizzes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError.invoke("クイズの取得に失敗しました")
                }
            }
        }
    }

    fun cleanup() {
        scope.cancel()
    }

    fun loadCategoriesAsync(
        chapterNumber: Int,
        quizDao: QuizDao,
        onSuccess: Function1<List<String>, Unit>,
        onError: Function1<String, Unit>
    ) {
        scope.launch {
            try {
                val quizzes = quizDao.getAllQuizzes()
                val categories = mutableListOf<String>()
                for (quiz in quizzes) {
                    if (quiz.chapter == chapterNumber.toString() && !categories.contains(quiz.category)) {
                        categories.add(quiz.category)
                    }
                }
                withContext(Dispatchers.Main) {
                    onSuccess.invoke(categories)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError.invoke("カテゴリーの読み込みに失敗しました")
                }
            }
        }
    }

    fun getQuizCountForCategoryAsync(
        category: String,
        quizDao: QuizDao,
        onSuccess: Function1<Int, Unit>,
        onError: Function1<String, Unit>
    ) {
        scope.launch {
            try {
                val quizzes = quizDao.getAllQuizzes()
                val count = quizzes.count { quiz -> quiz.category == category }
                withContext(Dispatchers.Main) {
                    onSuccess.invoke(count)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError.invoke("問題数の取得に失敗しました")
                }
            }
        }
    }

    /**
     * ContentfulからすべてのデータをFetchするメソッド
     * 進捗状況をレポートする機能付き
     */
    fun fetchAllEntriesAsync(
        contentType: String,
        onProgress: (Int, String) -> Unit,
        onSuccess: (List<CDAEntry>) -> Unit,
        onError: (String) -> Unit
    ) {
        requireNotNull(api) { "API instance is required for this operation" }
        scope.launch {
            try {
                withContext(Dispatchers.Main) {
                    onProgress(0, "Contentfulに接続中...")
                }
                
                // 一度に大量のデータを取得（最大1000件）
                val client = api.getClient() ?: throw IllegalStateException("Contentful Client is null")
                
                withContext(Dispatchers.Main) {
                    onProgress(20, "データをダウンロード中...")
                }
                
                // クエリを構築して実行
                val query = client.fetch(CDAEntry::class.java)
                    .withContentType(contentType)
                    .where("limit", "1000")  // limitをパラメータとして設定
                
                val response = query.all()
                val entries = response.items().filterIsInstance<CDAEntry>()
                
                withContext(Dispatchers.Main) {
                    onProgress(80, "ダウンロード完了: ${entries.size}件のデータ")
                    onSuccess(entries)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = handleError(e)
                Log.e("AsyncHelper", "Fetch all entries error: $errorMsg", e)
                withContext(Dispatchers.Main) {
                    onError(errorMsg)
                }
            }
        }
    }

}
